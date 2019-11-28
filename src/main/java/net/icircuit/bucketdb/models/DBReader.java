package net.icircuit.bucketdb.models;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import net.icircuit.bucketdb.models.wrappers.KeyRange;
import org.json.JSONObject;

import java.util.*;
import java.util.logging.Logger;

public class DBReader {
    private final static Logger LOGGER = Logger.getLogger(DBReader.class.getName());
    private MemTable memTable;
    private List<Bucket> buckets;
    public DBReader(MemTable memTable, List<Bucket> buckets) {
        this.memTable = memTable;
        this.buckets = new ArrayList<>(buckets); // list might be mutated else where, we need to have our own copy

    }
    public Optional<JSONObject> get(String key){
        //first see if the key is present in memeory
        Optional<DataRecordWrapper> optionalDataRecordWrapper =memTable.getRecord(key);
        if(optionalDataRecordWrapper.isPresent()){
            return optionalDataRecordWrapper.filter(DataRecordWrapper::isValid).map(dataRecordWrapper ->
                    new JSONObject(dataRecordWrapper.getDataRecord().getRValue()));
        }
        //find the bucket that might contain target key
        int index = Collections.binarySearch(buckets, new KeyRange() {
            @Override
            public String getStartKey() {
                return key;
            }

            @Override
            public String getEndKey() {
                return key;
            }
        }, (o1, o2) -> {
            if(o1.getStartKey().compareTo(o2.getStartKey()) <=0 && o1.getEndKey().compareTo(o2.getEndKey())>=0){
                return 0;
            }
            if(o1.getStartKey().compareTo(o2.getStartKey())>0){ // that is the startKey is outside of this range
                return 1;
            }
            if(o1.getEndKey().compareTo(o2.getEndKey())<0){
                return -1;
            }
            return 0;
        });
        if(index==-1) return Optional.empty();
        return buckets.get(index).getRecord(key).map(dataRecordWrapper -> new JSONObject(dataRecordWrapper.getDataRecord().getRValue()));
    }
    public Collection<Bucket> getBucketList() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = new ArrayList<>(buckets);
    }

    public MemTable getMemTable() {
        return memTable;
    }
}
