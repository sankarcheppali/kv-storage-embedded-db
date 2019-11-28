package net.icircuit.bucketdb.models.wrappers;

import net.icircuit.bucketdb.models.proto.DataRecordProto.*;

import java.util.Date;

public class DataRecordWrapper {
    DataRecord dataRecord;
    public DataRecordWrapper(DataRecord dataRecord) {
        this.dataRecord = dataRecord;
    }

    public DataRecord getDataRecord() {
        return dataRecord;
    }

    @java.lang.Override
    public int hashCode() {
        return dataRecord.getRKey().hashCode();
    }
    @java.lang.Override
    public boolean equals(final Object that){
        if(this == that)return true;
        if(that instanceof DataRecordWrapper){
            return dataRecord.getRKey().equals(((DataRecordWrapper) that).getDataRecord().getRKey());
        }
        return false;
    }
    // checks if record is expired or deleted
    public boolean isValid(){
        long currentTime = new Date().getTime();
        //check if ttl 0, ttl is not set
        if(dataRecord.getTtl() == 0 ) return  true;
        if(dataRecord.getTtl()+dataRecord.getTimestamp() < currentTime) return false;
        return true;
    }
}
