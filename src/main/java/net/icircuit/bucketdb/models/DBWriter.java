package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class DBWriter {
    MemTable memTable;
    public DBWriter(MemTable memTable) {
        this.memTable = memTable;
    }
    public void add(String key, JSONObject value) throws IOException {
        DataRecord dataRecord = DataRecord.newBuilder()
                .setRKey(key)
                .setRValue(value.toString())
                .build();
        memTable.addRecord(dataRecord);
    }
    public void add(String key,JSONObject value,long ttl) throws IOException{
        DataRecord dataRecord = DataRecord.newBuilder()
                .setRKey(key)
                .setRValue(value.toString())
                .setTtl(ttl)
                .build();
        memTable.addRecord(dataRecord);
    }
    public void delete(String key) throws IOException {
        memTable.deleteRecord(key);
    }

}
