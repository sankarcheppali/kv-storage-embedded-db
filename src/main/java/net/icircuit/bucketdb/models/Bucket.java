package net.icircuit.bucketdb.models;

import javafx.util.Pair;
import net.icircuit.bucketdb.FileNameComparator;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.proto.ManifestProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * Instantiate bucket with bucket path, if path is not present it will be created
 * Bucket is made of sorted files
 */
public class Bucket implements Comparable<Bucket>{
    private String filePrefix="BKTMANIFEST";
    private Path bucketFolderPath;
    private List<Path> sortedFilePathList;
    private List<SortedFile> sortedFileList;
    private TreeSet<String> sortedFileTerminationKeys;
    private Path manifestFile;
    public Bucket(Path bucketFolderPath) throws IOException {
        this.bucketFolderPath = bucketFolderPath;
        //if bucket path is not present, create it
        if(!Files.exists(bucketFolderPath)){
            Files.createDirectories(bucketFolderPath);
        }
        List<Path> manifestFileList = Files.list(bucketFolderPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().startsWith(filePrefix))
                .collect(Collectors.toList());
        //sort oldest to latest
        manifestFileList.sort(new FileNameComparator());
        //find a valid manifest file - based on crc
        Pair<Path,ManifestProto.BucketManifestFile> validManifestPair= readValidManifest(manifestFileList);
        if(validManifestPair != null){
            manifestFile = validManifestPair.getKey();
            ManifestProto.BucketManifest bukBucketManifest = validManifestPair.getValue().getBucketManifest();
            //TODO: load sorted files and remove files not referenced in manifest
        }else {
            manifestFile = createManifest(bucketFolderPath);
        }
        //remove all invalid manifest files
        manifestFileList.stream().filter(path -> path!=manifestFile).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //get sorted file path from bucket manifest - the files has to be loaded in the creation order (oldest first)
        this.sortedFilePathList = sortedFilePathList;
        sortedFileList = new ArrayList<>();
        for (Path sortedFilePath : sortedFilePathList) {
            sortedFileList.add(new SortedFile(sortedFilePath));
        }
        buildSortedFileTerminationKeys();
    }


    public void createSortedFile(List<DataRecordWrapper> dataRecordWrapperList) throws IOException {
        Path sortedFilePath = SortedFile.createFile(bucketFolderPath);
        SortedFile.perssist(sortedFilePath, dataRecordWrapperList);
        addSortedFile(sortedFilePath);
    }

    private void addSortedFile(Path sortedFilePath) throws IOException {
        sortedFilePathList.add(sortedFilePath);
        SortedFile sortedFile = new SortedFile(sortedFilePath);
        sortedFileList.add(sortedFile);
        buildSortedFileTerminationKeys();
        saveManifest();
    }

    private void buildSortedFileTerminationKeys() {
        TreeSet<String> set = new TreeSet<>();
        for (SortedFile sortedFile : sortedFileList) {
            sortedFileTerminationKeys.add(sortedFile.startKey());
            sortedFileTerminationKeys.add(sortedFile.endKey());
        }
        sortedFileTerminationKeys = set;
    }

    //when a merge is completed, we need remove the old files from in-memory data structures
    public void removeSortedFile(SortedFile sortedFile) {
        sortedFileList.remove(sortedFile);
        sortedFilePathList.remove(sortedFile.getSortedFilePath());
        //rebuild the termination keys
        buildSortedFileTerminationKeys();
        saveManifest();
    }

    public String getStartKey() {
        if (sortedFileTerminationKeys.size() == 0) return "";//it is posible for bucket to exsist with out a sorted file
        return sortedFileTerminationKeys.first();
    }

    public String getEndKey() {
        if (sortedFileTerminationKeys.size() == 0) return "";// should we return null or empty string ?
        return sortedFileTerminationKeys.last();
    }

    public void saveManifest() {
        //TODO: write current sorted file list to manifest
    }

    public boolean requiresSplit() {
        return sortedFileList.stream().map(SortedFile::size).reduce(0L, (a, b) -> a + b) > Config.MAZ_SIZE_FOR_BUCKET
                ? true : false;
    }

    public boolean requiresCompaction() {
        if (sortedFilePathList.size() > Config.MAX_FILES_IN_BUCKET) return true;
        if (sortedFileList.size() > 1) {
            return sortedFileList.subList(1, sortedFileList.size()).stream().anyMatch(sortedFile -> sortedFile.size() > Config.MAX_SIZE_FOR_MINOR_FILE);
        }
        return false;
    }

    //combine all minor files,if the combines size is more than Config.MAX_SIZE_FOR_MINOR_FILE, merge with major file or
    //create a new minor file
    public void runCompaction() {

    }

    //read all records from disk, partition into separate lists
    public List<List<DataRecordWrapper>> split() {
        List<DataRecordWrapper> dataRecordWrapperList = sortedFileList.stream()
                .flatMap(sortedFile -> sortedFile.readAll().stream())
                .collect(CustomeCollectors.toLinkedHashSet())
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
    //Bucket is compared based on the last key that bucket holds,
    @Override
    public int compareTo(Bucket o) {
        return this.getEndKey().compareTo(o.getEndKey());
    }

    private Path createManifest(Path bucketFolderPath) throws IOException{
        String fileName = filePrefix+"-"+new Date().getTime();
        Path path = Paths.get(bucketFolderPath.toString(),fileName);
        Files.createFile(path);
        return path;
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
        CRC32 crcCalculator = new CRC32();
        long actualCRC = bucketManifestFile.getBucketManifestCRC();
        crcCalculator.update(bucketManifestFile.getBucketManifest().toByteArray());
        long expectedCRC = crcCalculator.getValue();
        return expectedCRC == actualCRC;
    }
}
