package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
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
                .setTimestamp(new Date().getTime())
                .build();
        memTable.addRecord(new DataRecordWrapper(dataRecord));
    }
    public void add(String key,JSONObject value,long ttl) throws IOException{
        DataRecord dataRecord = DataRecord.newBuilder()
                .setRKey(key)
                .setRValue(value.toString())
                .setTtl(ttl)
                .setTimestamp(new Date().getTime())
                .build();
        memTable.addRecord(new DataRecordWrapper(dataRecord));
    }
    public void delete(String key) throws IOException {
        memTable.deleteRecord(key);
    }

}
