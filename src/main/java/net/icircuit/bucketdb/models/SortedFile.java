package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataIndexBlockProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.proto.FileFooterProto.*;
import net.icircuit.bucketdb.models.proto.RecordIndexBlockProto;
import net.icircuit.bucketdb.models.wrappers.DataBlockIndexRecordWrapper;
import net.icircuit.bucketdb.models.wrappers.DataRecordIndexBlockWrapper;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import net.icircuit.bucketdb.models.wrappers.KeyRange;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages reading,writing sorted files to disk
 */
class SortedFile {
    private static String filePrefix="SF";
    private Path sortedFilePath;
    private DataIndexBlock dataIndexBlock;
    private List<DataBlockIndexRecordWrapper> dataBlockIndexRecordWrapperList;
    private FileFooter fileFooter;
    private SortedFileReader sortedFileReader;
    private long fileSize;
    public SortedFile(Path sortedFilePath)  {
        this.sortedFilePath = sortedFilePath;
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(sortedFilePath.toFile(),"r");
            FileChannel fc = randomAccessFile.getChannel()) {
            fileSize = fc.size();
            sortedFileReader  = new SortedFileReader(fc.map(FileChannel.MapMode.READ_ONLY,0,fileSize),(int)fc.size());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        fileFooter= sortedFileReader.readFileFooter();
        dataIndexBlock = sortedFileReader.readDataIndexBlock();
        dataBlockIndexRecordWrapperList = dataIndexBlock.getIndexList().stream().map(DataBlockIndexRecordWrapper::new).collect(Collectors.toList());
    }
    public static void persist(Path sortedFilePath, Collection<DataRecordWrapper> dataRecordList) throws IOException {
        SortedFileWriter sortedFileWriter = new SortedFileWriter(dataRecordList,sortedFilePath);
        sortedFileWriter.pressist();
    }

    public static Path createFile(Path bucketFolderPath) throws IOException {
        Path path = Paths.get(bucketFolderPath.toString(),filePrefix+"-"+new Date().getTime()+"-"+Config.getUniq()+".bdb");
        Files.createFile(path);
        return path;
    }
    public static List<Path> list(Path bucketFolderPath) throws IOException{
        return Files.list(bucketFolderPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith(filePrefix))
                .collect(Collectors.toList());
    }

    public String startKey(){
        return dataIndexBlock.getIndex(0).getDbStartKey();
    }
    public String endKey(){
        return dataIndexBlock.getIndex(dataIndexBlock.getIndexCount()-1).getDbEndKey();
    }
    public boolean isInclusiveOf(String key){
        String startKey = startKey();
        String endKey = endKey();
        if(startKey.compareTo(key) <=0 && endKey.compareTo(key)>=0){
            return true;
        }
        return false;
    }
    public Optional<DataRecordWrapper> getRecord(String key){
        if(!isInclusiveOf(key)) return Optional.empty();
        //find the data block
        int index = Collections.binarySearch(dataBlockIndexRecordWrapperList, new KeyRange() {
            @Override
            public String getStartKey() {
                return key;
            }

            @Override
            public String getEndKey() {
                return key;
            }
        });
        if(index == -1) return Optional.empty();
        DataBlockIndexRecordWrapper dataBlockIndexRecordWrapper = dataBlockIndexRecordWrapperList.get(index);
        DataBlockReader dataBlockReader = sortedFileReader.dataBlockReader(dataBlockIndexRecordWrapper.getIndexRecord());
        DataRecordIndexBlockWrapper dataRecordIndexBlockWrapper = new DataRecordIndexBlockWrapper(dataBlockReader.readRecordIndexBlock());
        Optional<RecordIndexBlockProto.RecordIndexBlock.IndexRecord> optionalIndexRecord = dataRecordIndexBlockWrapper.search(key);
        return optionalIndexRecord
                .map(indexRecord -> dataBlockReader.readDataRecord(indexRecord));
                //.filter(DataRecordWrapper::isValid); // don't filter here, we can only filter at bucket level,other wise old records present in older sorted file might be returned to client
    }
    public Path getSortedFilePath() {
        return sortedFilePath;
    }
    public Collection<DataRecordWrapper> readAll(){
        return sortedFileReader.readAll();
    }
    public long size(){
        return fileSize;
    }
    @Override
    public String toString(){
        return getSortedFilePath()+":"+startKey()+"-"+endKey();
    }
}
