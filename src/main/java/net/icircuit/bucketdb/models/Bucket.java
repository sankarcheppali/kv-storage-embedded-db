package net.icircuit.bucketdb.models;

import javafx.util.Pair;
import net.icircuit.bucketdb.FileNameComparator;
import net.icircuit.bucketdb.models.proto.ManifestProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import net.icircuit.bucketdb.models.wrappers.KeyRange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * Instantiate bucket with bucket path, if path is not present it will be created
 * Bucket is made of sorted files
 */
public class Bucket implements Comparable<Bucket>,KeyRange {
    private final static Logger LOGGER = Logger.getLogger(Bucket.class.getName());
    private static String manifestFilePrefix ="BKTMANIFEST";
    private static String bktNamePrefix="bkt";
    private Path bucketFolderPath;
    private List<Path> sortedFilePathList;
    private List<SortedFile> sortedFileList;
    private TreeSet<String> sortedFileTerminationKeys;
    private Path manifestFile;

    public Bucket(Path bucketFolderPath){
        this.bucketFolderPath = bucketFolderPath;
        List<Path> manifestFileList = listManifestFiles();
        //sort oldest to latest
        manifestFileList.sort(new FileNameComparator());
        //find a valid manifest file - based on crc
        Pair<Path,ManifestProto.BucketManifestFile> validManifestPair= readValidManifest(manifestFileList);
        if(validManifestPair != null){
            manifestFile = validManifestPair.getKey();
            ManifestProto.BucketManifest bukBucketManifest = validManifestPair.getValue().getBucketManifest();
            //load sorted files and remove files not referenced in manifest
            sortedFilePathList = bukBucketManifest.getSortedFileRecordList().stream()
                    .map(sortedFileRecord -> Paths.get(bucketFolderPath.toString(),sortedFileRecord.getSortedFileName()))
                    .collect(Collectors.toList());
            sortedFileList = sortedFilePathList.stream().map(SortedFile::new).collect(Collectors.toList());
            cleanUpBucket();
        }else{
            // sorted file list is empty
            sortedFilePathList = new ArrayList<>();
            sortedFileList = new ArrayList<>();
            manifestFile = createManifestFile();
        }
        //remove all invalid manifest files
        manifestFileList.stream().filter(path -> path!=manifestFile).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        buildSortedFileTerminationKeys();
    }

    public List<Path> listManifestFiles(){
        try {
            return Files.list(bucketFolderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(manifestFilePrefix))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static List<Path> list(Path dbPathFolder) throws IOException{
        return Files.list(dbPathFolder)
                .filter(Files::isDirectory)
                .filter(path -> path.getFileName().toString().startsWith(bktNamePrefix))
                .collect(Collectors.toList());
    }

    //remove sorted files that are not reference in manifest
    public void cleanUpBucket(){
        try{
            List<Path> sortedFilesOnDisk = SortedFile.list(bucketFolderPath);
            sortedFilesOnDisk.stream().filter(path -> !sortedFilePathList.contains(path))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            LOGGER.info("deleted sorted file "+path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static Bucket create(Path dbPath){
        try {
            return new Bucket(createBucketFolder(dbPath));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private static  Path createBucketFolder(Path dbPath) throws IOException {
        Path path = Paths.get(dbPath.toString(), bktNamePrefix+"-"+new Date().getTime()+"-"+Config.getUniq());
        Files.createDirectory(path);
        return path;
    }
    public Path createSortedFile(Collection<DataRecordWrapper> dataRecordWrapperList){
        try{
            Path sortedFilePath = SortedFile.createFile(bucketFolderPath);
            SortedFile.persist(sortedFilePath, dataRecordWrapperList);
            addSortedFile(sortedFilePath);
            LOGGER.info("created sorted file "+sortedFilePath);
            return sortedFilePath;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private synchronized void addSortedFile(Path sortedFilePath) {
        sortedFilePathList.add(sortedFilePath);
        SortedFile sortedFile = new SortedFile(sortedFilePath);
        sortedFileList.add(sortedFile);
        buildSortedFileTerminationKeys();
        saveManifest();
    }

    private void buildSortedFileTerminationKeys() {
        TreeSet<String> set = new TreeSet<>();
        for (SortedFile sortedFile : sortedFileList) {
            set.add(sortedFile.startKey());
            set.add(sortedFile.endKey());
        }
        sortedFileTerminationKeys = set;
    }

    //when a merge is completed, we need remove the old files from in-memory data structures
    public synchronized void removeSortedFile(SortedFile sortedFile){
        sortedFileList.remove(sortedFile);
        sortedFilePathList.remove(sortedFile.getSortedFilePath());
        //rebuild the termination keys
        buildSortedFileTerminationKeys();
        saveManifest();
    }

    public String getStartKey() {
        if (sortedFileTerminationKeys.size() == 0) return "";//it is possible for bucket to exist with out a sorted file
        return sortedFileTerminationKeys.first();
    }

    public String getEndKey() {
        if (sortedFileTerminationKeys.size() == 0) return "";// should we return null or empty string ?
        return sortedFileTerminationKeys.last();
    }


    public void saveManifest(){
        //write current sorted file list to manifest
        List<ManifestProto.BucketManifest.SortedFileRecord> sortefFileRecords= sortedFilePathList.stream()
                .map(Path::getFileName)
                .map(fileName-> ManifestProto.BucketManifest.SortedFileRecord.newBuilder().setSortedFileName(fileName.toString()).build())
                .collect(Collectors.toList());
        ManifestProto.BucketManifest bucketManifest =  ManifestProto.BucketManifest.newBuilder()
                .addAllSortedFileRecord(sortefFileRecords).build();
        ManifestProto.BucketManifestFile bucketManifestFile= ManifestProto.BucketManifestFile.newBuilder()
                .setBucketManifest(bucketManifest)
                .setBucketManifestCRC(manifestCrc(bucketManifest))
                .build();
        Path tmpManifestFile = createManifestFile();
        try(OutputStream os= Files.newOutputStream(tmpManifestFile)){
            bucketManifestFile.writeTo(os);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        manifestFile = tmpManifestFile;
    }

    public boolean readyForSplit() {
        return sortedFileList.stream().map(SortedFile::size).reduce(0L, (a, b) -> a + b) > Config.MAX_SIZE_FOR_BUCKET;
    }

    public boolean readyForCompaction() {
        return requiresMajorCompaction() || requiresMinorCompaction();
    }

    private boolean requiresMinorCompaction(){
        return sortedFilePathList.size() > Config.MAX_FILES_IN_BUCKET;
    }
    private boolean requiresMajorCompaction(){
        return sortedFileList.subList(1, sortedFileList.size()).stream().anyMatch(sortedFile -> sortedFile.size() > Config.MAX_SIZE_FOR_MINOR_FILE);
    }
    //combine all minor files,if the combines size is more than Config.MAX_SIZE_FOR_MINOR_FILE, merge with major file or
    //create a new minor file
    public void runCompaction() {
        if(sortedFilePathList.size() <= 1)return; // compaction is required only if there are more than one file
        if(requiresMajorCompaction()){
            runCompaction(new ArrayList<>(sortedFileList));
        }else if(requiresMinorCompaction()){
            runCompaction(new ArrayList<>(sortedFileList.subList(1,sortedFileList.size()))); // original list will be mutated);
        }
    }
    private void runCompaction(List<SortedFile> compactionTargets) {
        Collection<DataRecordWrapper> dataRecordWrappers = sortedFileList.subList(1, sortedFileList.size()).stream()
                .flatMap(sortedFile -> sortedFile.readAll().stream())
                .collect(CustomeCollectors.toTreeSet());
        Path newSrotedFile = createSortedFile(dataRecordWrappers);
        addSortedFile(newSrotedFile);
        compactionTargets.forEach(this::removeSortedFile);
    }

    //read all records from disk, partition into separate lists
    public List<List<DataRecordWrapper>> split() {
        List<DataRecordWrapper> dataRecordWrapperList = sortedFileList.stream()
                .flatMap(sortedFile -> sortedFile.readAll().stream())
                .collect(CustomeCollectors.toTreeSet())
                .stream()
                .filter(DataRecordWrapper::isValid) // remove expired items
                .collect(Collectors.toList());
        int numberOfRecords = dataRecordWrapperList.size();
        return new ArrayList<>(Arrays.asList(
                dataRecordWrapperList.subList(0, numberOfRecords / 2),
                dataRecordWrapperList.subList(numberOfRecords / 2, numberOfRecords)
        ));
    }

    public boolean isInclusiveOf(String key){
        String startKey = getStartKey();
        String endKey = getEndKey();
        if(startKey.compareTo(key) <=0 && endKey.compareTo(key)>=0){
            return true;
        }
        return false;
    }

    /**
     * Bucket can have sorted files with overlapping key ranges
     * we need to find all the sorted files that might have target key and search until we find record or run out off
     * sorted files
     * @param key
     * @return
     */
    public Optional<DataRecordWrapper> getRecord(String key){
        if(!isInclusiveOf(key))return Optional.empty();
        for(int i=sortedFileList.size()-1;i>=0;i--){
            SortedFile sortedFile = sortedFileList.get(i);
            if(sortedFile.isInclusiveOf(key) ){
                Optional<DataRecordWrapper> dataRecordWrapper = sortedFile.getRecord(key);
                if(dataRecordWrapper.isPresent()){
                    return dataRecordWrapper.get().isValid() ? dataRecordWrapper : Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    //Bucket is compared based on the last key that bucket holds,
    @Override
    public int compareTo(Bucket o) {
        return this.getEndKey().compareTo(o.getEndKey());
    }

    private Path createManifestFile() {
       try{
           String fileName = manifestFilePrefix +"-"+new Date().getTime()+"-"+Config.getUniq();
           Path path = Paths.get(bucketFolderPath.toString(),fileName);
           Files.createFile(path);
           return path;
       }catch (Exception e){
           e.printStackTrace();
           throw new RuntimeException(e);
       }
    }

    private Pair<Path, ManifestProto.BucketManifestFile> readValidManifest(List<Path> manifestFileList) {
        List<Pair<Path, ManifestProto.BucketManifestFile>> bucketManifestFileList =manifestFileList.stream().map(path -> {
            try {
                return new Pair<>(path, ManifestProto.BucketManifestFile.parseFrom(Files.newInputStream(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter((Objects::nonNull))
                .filter(pair -> isValidManifestFile(pair.getValue()))
                .collect(Collectors.toList());
        if(bucketManifestFileList.size() == 0)return null;
        return bucketManifestFileList.get(bucketManifestFileList.size()-1);
    }


    private boolean isValidManifestFile(ManifestProto.BucketManifestFile bucketManifestFile){
        long actualCRC = bucketManifestFile.getBucketManifestCRC();
        long expectedCRC = manifestCrc(bucketManifestFile.getBucketManifest());
        return expectedCRC == actualCRC;
    }
    private long manifestCrc(ManifestProto.BucketManifest manifest){
        CRC32 crcCalculator = new CRC32();
        crcCalculator.update(manifest.toByteArray());
        return crcCalculator.getValue();
    }

    public Path getBucketFolderPath() {
        return bucketFolderPath;
    }

    public Path getManifestFile() {
        return manifestFile;
    }
}
