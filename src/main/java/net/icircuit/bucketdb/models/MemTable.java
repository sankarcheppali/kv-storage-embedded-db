package net.icircuit.bucketdb.models;

import javafx.util.Pair;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All the writes will come to mem table first, once the memtable reaches serializedSize S, SwitchOver will happen
 */


public class MemTable {
    private Path dbFolderPath;
    private List<Map<String, DataRecordWrapper>> tableMapList;
    private List<WHLog> whLogList;

    public MemTable(Path dbFolderPath) throws IOException {
        this.dbFolderPath = dbFolderPath;
        // load whlogs present in the folder
        whLogList = WHLog.loadWHLFiles(dbFolderPath);
        if (whLogList.isEmpty()) {
            whLogList = new ArrayList<>();
            whLogList.add(WHLog.create(dbFolderPath));
        }
        // build mem tables from the log files
        tableMapList = new ArrayList<>();
        for (WHLog whLog : whLogList) {
            Map<String, DataRecordWrapper> hydrate = hydrate(whLog);
            tableMapList.add(hydrate);
        }
    }

    public static Map<String, DataRecordWrapper> hydrate(WHLog whLog) throws IOException {
        return whLog.readAll().stream().collect(Collectors.toMap(DataRecord::getRKey, DataRecordWrapper::new));
    }

    public boolean isReadyForSpill() {
        if (whLogList.size() > 1) {
            return true;
        }
        if (getCurrentWHLog().size() > Config.MAX_WHL_SIZE) {
            return true;
        }
        return false;
    }

    public Pair<WHLog,Map<String,DataRecordWrapper>> whlogReadyForSpill() throws IOException {
        if (whLogList.size() == 1) {
            //create new whlog and table
            WHLog whLog = WHLog.create(dbFolderPath);
            Map<String, DataRecordWrapper> table = new TreeMap<>();
            tableMapList.add(table);
            whLogList.add(whLog);
        }
        WHLog oldestWHLog = whLogList.get(0);
        Map<String, DataRecordWrapper> oldestTable = tableMapList.get(0);
        return new Pair<>(oldestWHLog,oldestTable);
        //return new Bucketizer(oldestTable.values());
    }

    // delete the whlog and associated table
    public void bucketizationCompleted(WHLog whLog) throws IOException {
        int index = whLogList.indexOf(whLog);
        whLogList.remove(index);
        tableMapList.remove(index);
        whLog.delete();
    }

    public Map<String, DataRecordWrapper> getCurrentTable() {
        return tableMapList.get(tableMapList.size() - 1);
    }

    public WHLog getCurrentWHLog() {
        return whLogList.get(whLogList.size() - 1);
    }

    public synchronized void addRecord(DataRecordWrapper dataRecordWrapper) throws IOException {
        //write the mutation to whl
        getCurrentWHLog().add(dataRecordWrapper.getDataRecord());
        getCurrentTable().put(dataRecordWrapper.getDataRecord().getRKey(), dataRecordWrapper);
    }

    public  void deleteRecord(DataRecordWrapper dataRecordWrapper) throws IOException {
        dataRecordWrapper = new DataRecordWrapper(DataRecord.newBuilder(dataRecordWrapper.getDataRecord())
                .setTtl(-1)
                .build());
        addRecord(dataRecordWrapper);
    }

    //negative ttl indicates deleted record
    public void deleteRecord(String key) throws IOException {
        //write the mutation to whl
        DataRecord dataRecord = DataRecord.newBuilder()
                .setRKey(key)
                .setTtl(-1)
                .build();
        addRecord(new DataRecordWrapper(dataRecord));
    }

    public Optional<DataRecordWrapper> getRecord(String key) {
        for (Map<String, DataRecordWrapper> table : tableMapList) {
            if (table.containsKey(key)) {
                return Optional.of(table.get(key));
            }
        }
        return Optional.empty();
    }

}

