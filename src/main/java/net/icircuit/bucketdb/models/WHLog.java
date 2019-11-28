package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * File format every record is prefixed with length of the record (4 byte (signed) integer)
 *
 */
public class WHLog {
    private final static Logger LOGGER = Logger.getLogger(WHLog.class.getName());
    private static String filePrefix = "WHL";
    Path whlFilePath;
    public WHLog(Path whlFilePath) {
        this.whlFilePath = whlFilePath;
    }
    public static WHLog create(Path dbPath) throws IOException {
        return  new WHLog(createWHLFile(dbPath));
    }
    private static Path createWHLFile(Path dst) throws IOException {
        Path path = Paths.get(dst.toString(),filePrefix+"-"+new Date().getTime()+"-"+Config.getUniq()+".log");
        Files.createFile(path);
        LOGGER.info("created new whl file "+path);
        return path;
    }
    public static List<WHLog> loadWHLFiles(Path dbPath) throws IOException{
        return Files.list(dbPath).filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith(filePrefix))
                .map(WHLog::new)
                .collect(Collectors.toList());
    }
    public synchronized void add(DataRecord dataRecord) throws IOException {
       try(OutputStream os = Files.newOutputStream(whlFilePath, StandardOpenOption.APPEND);
           DataOutputStream dos = new DataOutputStream(os)){
           dos.writeInt(dataRecord.getSerializedSize());
           dataRecord.writeTo(dos);
       }
    }
    public long size(){
        return  whlFilePath.toFile().length();
    }
    public void delete() throws IOException {
        Files.deleteIfExists(whlFilePath);
    }

    public Collection<DataRecord> readAll() throws IOException{
        try(InputStream is = Files.newInputStream(whlFilePath,StandardOpenOption.READ);
            DataInputStream dis = new DataInputStream(is)){
            long fileLength = whlFilePath.toFile().length();
            long offset = 0;
            // maintains ascending order of the key
            TreeMap<String,DataRecord> recordMap = new TreeMap<>();
            while(offset < fileLength){
                int recordLength = dis.readInt();
                byte[] dataRecordData = new  byte[recordLength];
                dis.read(dataRecordData,0,recordLength);
                DataRecord dataRecord = DataRecord.parseFrom(dataRecordData);
                recordMap.put(dataRecord.getRKey(),dataRecord);
                offset += 4 + recordLength ;
            }
            return recordMap.values();
        }
    }
}
