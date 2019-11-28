package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataIndexBlockProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.proto.FileFooterProto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Manages reading,writing sorted files to disk
 */
public class SortedFile {
    private static String filePrefix="SF";
    private Path sortedFilePath;
    private DataIndexBlock dataIndexBlock;
    private FileFooter fileFooter;
    private SortedFileReader sortedFileReader;
    private long fileSize;
    public SortedFile(Path sortedFilePath) throws IOException {
        this.sortedFilePath = sortedFilePath;
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(sortedFilePath.toFile(),"r");
            FileChannel fc = randomAccessFile.getChannel();) {
            fileSize = fc.size();
            sortedFileReader  = new SortedFileReader(fc.map(FileChannel.MapMode.READ_ONLY,0,fileSize),(int)fc.size());
        }
        dataIndexBlock = sortedFileReader.readDataIndexBlock();
    }
    public static void perssist(Path sortedFilePath, List<DataRecordWrapper> dataRecordList) throws IOException {
        SortedFileWriter sortedFileWriter = new SortedFileWriter(dataRecordList,sortedFilePath);
        sortedFileWriter.pressist();
    }

    public static Path createFile(Path bucketFolderPath) throws IOException {
        Path path = Paths.get(bucketFolderPath.toString(),filePrefix+new Date().getTime()+"-"+Config.getUniq()+".ddb");
        Files.createFile(path);
        return path;
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
    public Path getSortedFilePath() {
        return sortedFilePath;
    }
    public Collection<DataRecordWrapper> readAll(){
        return sortedFileReader.readAll();
    }
    public long size(){
        return fileSize;
    }
}
