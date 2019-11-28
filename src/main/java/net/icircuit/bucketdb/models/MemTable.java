package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All the writes will come to mem table first, once the memtable reaches serializedSize S, SwitchOver will happen
 *
 */


public class MemTable {
    private Path dbFolderPath;
    private List<Map<String,DataRecord>> tableMapList;
    private List<WHLog> whLogList;

    public MemTable(Path dbFolderPath) throws IOException {
        this.dbFolderPath = dbFolderPath;
        // load whlogs present in the folder
        whLogList = WHLog.loadWHLFiles(dbFolderPath);
        if(whLogList.isEmpty()){
            whLogList = new ArrayList<>();
            whLogList.add(WHLog.create(dbFolderPath));
        }
        // build mem tables from the log files
        tableMapList = new ArrayList<>();
        for (WHLog whLog : whLogList) {
            Map<String, DataRecord> hydrate = hydrate(whLog);
            tableMapList.add(hydrate);
        }
    }

    public static Map<String,DataRecord> hydrate(WHLog whLog) throws IOException {
        return whLog.readAll().stream().collect(Collectors.toMap(DataRecord::getRKey, Function.identity()));
    }

    public boolean isReadyForSpil(){
        if(whLogList.size() > 1){
            return  true;
        }
        if(getCurrentWHLog().size() > Config.MAX_WHL_SIZE){
            return true;
        }
        return  false;
    }
    public Bucketizer createBucketizer() throws IOException {
        if(whLogList.size()==1){
            //create new whlog and table
            WHLog whLog = WHLog.create(dbFolderPath);
            Map<String,DataRecord> table = new TreeMap<>();
            tableMapList.add(table);
            whLogList.add(whLog);
        }
        WHLog oldestWHLog = whLogList.get(0);
        Map<String,DataRecord> oldestTable = tableMapList.get(0);
        return new Bucketizer(oldestWHLog,oldestTable.values());
    }
    // delete the whlog and associated table
    public void bucketizationCompleted(Bucketizer bucketizer) throws IOException {
        WHLog whLog = bucketizer.getWhLog();
        int index = whLogList.indexOf(whLog);
        whLogList.remove(index);
        tableMapList.remove(index);
        whLog.delete();
    }

    public Map<String,DataRecord> getCurrentTable(){
        return tableMapList.get(tableMapList.size()-1);
    }
    public WHLog getCurrentWHLog(){
        return whLogList.get(whLogList.size()-1);
    }
    public synchronized void addRecord(DataRecord dataRecord) throws IOException {
        //write the mutation to whl
        getCurrentWHLog().add(dataRecord);
        getCurrentTable().put(dataRecord.getRKey(),dataRecord);
    }
    public void deleteRecord(DataRecord dataRecord) throws IOException{
        dataRecord = DataRecord.newBuilder(dataRecord)
                .setTtl(-1)
                .build();
        addRecord(dataRecord);
    }
    //negative ttl indicates deleted record
    public void deleteRecord(String key) throws IOException{
        //write the mutation to whl
        DataRecord dataRecord = DataRecord.newBuilder()
                .setRKey(key)
                .setTtl(-1)
                .build();
        addRecord(dataRecord);
    }
    public Optional<DataRecord> getRecord(String key){
        for(Map<String,DataRecord> table:tableMapList){
            if(table.containsKey(key)){
                return Optional.of(table.get(key));
            }
        }
        return Optional.empty();
    }

}

