package net.icircuit.bucketdb.models;

import javafx.util.Pair;
import net.icircuit.bucketdb.FileNameComparator;
import net.icircuit.bucketdb.models.proto.ManifestProto;
import net.icircuit.bucketdb.models.proto.ManifestProto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class Manifest {
    private final static Logger LOGGER = Logger.getLogger(SortedFileReader.class.getName());
    private String filePrefix="MANIFEST";
    private Path dbPathFolder;
    private Path manifestFile;
    private List<Path> bucketPathList;
    private List<Bucket> bucketList;
    private MemTable memTable;
    private DBReader dbReader;
    private Thread houseKeepingThread;

    public Manifest(Path dbPathFolder) throws IOException {
        this.dbPathFolder = dbPathFolder;
        //if db path is not present, create it
        if(!Files.exists(dbPathFolder)){
            Files.createDirectories(dbPathFolder);
        }
        List<Path> manifestFileList = Files.list(dbPathFolder)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith(filePrefix))
                .collect(Collectors.toList());
        //sort oldest to latest
        manifestFileList.sort(new FileNameComparator());
        //find a valid manifest file - based on crc
        Pair<Path,DBManifestFile> validManifestPair= readValidManifest(manifestFileList);
        if(validManifestPair != null){
            manifestFile = validManifestPair.getKey();
            DBManifest dbManifest = validManifestPair.getValue().getDbManifest();
            //load buckets and remove buckets not present in the manifest
            bucketPathList = dbManifest.getBucketRecordList().stream()
                    .map(bucketRecord -> Paths.get(dbPathFolder.toString(),bucketRecord.getBucketName()))
                    .collect(Collectors.toList());
            bucketList = bucketPathList.stream().map(Bucket::new).collect(Collectors.toList());
            cleanUpDB();
        }else {
            manifestFile = createManifestFile();
            bucketPathList = new ArrayList<>();
            bucketList = new ArrayList<>();
        }
        //remove all invalid manifest files
        manifestFileList.stream().filter(path -> path!=manifestFile).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        memTable = new MemTable(dbPathFolder);
        dbReader = new DBReader(memTable,bucketList);
        startHouseKeepingThread();
    }
    public DBWriter getDBWriter(){
        return new DBWriter(memTable);
    }
    public DBReader getDbReader() {
        return dbReader;
    }

    private Path createManifestFile() throws IOException {
        String fileName = filePrefix+"-"+new Date().getTime()+"-"+Config.getUniq();
        Path path = Paths.get(dbPathFolder.toString(),fileName);
        Files.createFile(path);
        return path;
    }
    //remove unwanted buckets
    public void cleanUpDB(){
        try{
            List<Path> bucketsOnDisk = Bucket.list(dbPathFolder);
            bucketsOnDisk.stream().filter(path -> !bucketPathList.contains(path))
                    .forEach(path -> {
                        try {
                            Files.list(path).forEach(path1 -> path1.toFile().delete());
                            Files.deleteIfExists(path);
                            LOGGER.info("deleted bucket "+path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    /**
     * this method should be called every time a bucket operation is performed
     * when new bucket is created or deleted
     */
    public void saveManifest() {
        //write current sorted file list to manifest
        List<DBManifest.BucketRecord> bucketRecordList= bucketPathList.stream()
                .map(Path::getFileName)
                .map(fileName-> DBManifest.BucketRecord.newBuilder().setBucketName(fileName.toString()).build())
                .collect(Collectors.toList());
        ManifestProto.DBManifest dbManifest =  ManifestProto.DBManifest.newBuilder()
                .addAllBucketRecord(bucketRecordList).build();
        ManifestProto.DBManifestFile dbManifestFile= ManifestProto.DBManifestFile.newBuilder()
                .setDbManifest(dbManifest)
                .setDbManifestCRC(manifestCrc(dbManifest))
                .build();
        try(OutputStream os= Files.newOutputStream(createManifestFile())){
            dbManifestFile.writeTo(os);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void startHouseKeepingThread(){
        houseKeepingThread= new Thread(() -> {
           while (true){
               try {
                   houseKeeping();
                   Thread.sleep(Config.HOUSE_KEEPING_TASK_FREQ);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
        });
        houseKeepingThread.start();
    }
    private synchronized  void houseKeeping() throws IOException {
        LOGGER.info("housekeeping task started");
        Date start = new Date();
        if(memTable.isReadyForSpill()){
            memTableHouseKeeping();
        }
        bucketHouseKeeping();
        Date end = new Date();
        dbReader.setBuckets(bucketList);
        LOGGER.info("housekeeping task completed,took "+(end.getTime() - start.getTime()));
    }
    private synchronized void memTableHouseKeeping() throws IOException {
        Pair<WHLog, Map<String, DataRecordWrapper>> whLogMapPair= memTable.whlogReadyForSpill();
        Bucketizer bucketizer = new Bucketizer(whLogMapPair.getValue().values());
        List<BucketSplit> bucketSplitList = bucketizer.bucketize(bucketList);
        bucketSplitList.forEach(bucketSplit -> {
            if(bucketSplit.getBucket()!=null){
                bucketSplit.getBucket().createSortedFile(bucketSplit.getDataRecordWrapperList());
            }else{
                //create bucket and add records
                Bucket bucket = Bucket.create(dbPathFolder);
                bucket.createSortedFile(bucketSplit.getDataRecordWrapperList());
                addBucket(bucket);
            }
        });
        memTable.bucketizationCompleted(whLogMapPair.getKey());
    }
    private synchronized void bucketHouseKeeping(){
        //check for splits
        List<Bucket> bucketsReadySplit = bucketList.stream().filter(Bucket::readyForSplit).collect(Collectors.toList());
        bucketsReadySplit.sort(Bucket::compareTo);
        bucketsReadySplit.stream().flatMap(bucket -> bucket.split().stream())
                .forEach(dataRecordWrappers -> {
                    //create bucket and add records
                    Bucket bucket = Bucket.create(dbPathFolder);
                    bucket.createSortedFile(dataRecordWrappers);
                    addBucket(bucket);
                });
        //remove buckets from in memory reference
        bucketsReadySplit.forEach(this::removeBucket);
        //check for compactions
        List<Bucket> bucketsReadyForCompaction = bucketList.stream().filter(Bucket::readyForCompaction).collect(Collectors.toList());
        bucketsReadyForCompaction.forEach(Bucket::runCompaction);
    }

    //remove bucket form in-memroy list
    private synchronized void removeBucket(Bucket bucket){
        bucketList.remove(bucket);
        bucketList.sort(Bucket::compareTo);
        //dbReader.setBuckets(bucketList);
        bucketPathList.remove(bucket.getBucketFolderPath());
        LOGGER.info("removing bucket "+bucket);
        saveManifest();
    }
    private synchronized void addBucket(Bucket bucket){
        bucketList.add(bucket);
        bucketList.sort(Bucket::compareTo);
        //dbReader.setBuckets(bucketList);
        bucketPathList.add(bucket.getBucketFolderPath());
        LOGGER.info("adding bucket "+bucket);
        saveManifest();
    }
    public long size(){
        return bucketList.stream().map(Bucket::size).reduce(0L, (a, b) -> a + b);
    }
    private Pair<Path, DBManifestFile> readValidManifest(List<Path> manifestFileList){
       List<Pair<Path,DBManifestFile>> dbManifestFileList =manifestFileList.stream().map(path -> {
            try {
                return new Pair<>(path,DBManifestFile.parseFrom(Files.newInputStream(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter((Objects::nonNull))
               .filter(pair -> isValidManifestFile(pair.getValue()))
               .collect(Collectors.toList());
       if(dbManifestFileList.size() == 0)return null;
       return dbManifestFileList.get(dbManifestFileList.size()-1);
    }

    private boolean isValidManifestFile(DBManifestFile dbManifestFile){
        long actualCRC = dbManifestFile.getDbManifestCRC();
        long expectedCRC = manifestCrc(dbManifestFile.getDbManifest());
        return expectedCRC == actualCRC;
    }

    private long manifestCrc(ManifestProto.DBManifest manifest){
        CRC32 crcCalculator = new CRC32();
        crcCalculator.update(manifest.toByteArray());
        return crcCalculator.getValue();
    }

}
