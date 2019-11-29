package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.exceptions.EmptyCollectionException;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Write memtable to buckets
 * determine which record goes to which bucket
 */
public class Bucketizer {

    private Collection<DataRecordWrapper> dataRecordWrappers;
    public Bucketizer(Collection<DataRecordWrapper> dataRecordWrappers) {
        this.dataRecordWrappers = dataRecordWrappers;
    }
    List<BucketSplit> bucketize(Collection<Bucket> buckets){
        List<BucketSplit> bucketSplitList = new ArrayList<>();
        if(buckets==null || buckets.size()==0){
            bucketSplitList.add(new BucketSplit(null,dataRecordWrappers.stream().collect(Collectors.toList())));
            return bucketSplitList;
        }
        Map<Bucket,Collection<DataRecordWrapper>> map = new HashMap<>();
        Iterator<Bucket> bucketIterator = buckets.iterator();
        Bucket runningBucket = bucketIterator.next();
        for(DataRecordWrapper dataRecordWrapper:dataRecordWrappers){
            DataRecord dataRecord = dataRecordWrapper.getDataRecord();
            if(bucketIterator.hasNext() && runningBucket.getEndKey().compareTo(dataRecord.getRKey()) < 0  ){
                runningBucket = bucketIterator.next();
            }
            if(!map.containsKey(runningBucket)){
                map.put(runningBucket,new TreeSet<DataRecordWrapper>()); // should we take tree set ?
            }
            map.get(runningBucket).add(dataRecordWrapper);
        }
        for(Map.Entry<Bucket,Collection<DataRecordWrapper>> entry:map.entrySet()){
            bucketSplitList.add(new BucketSplit(entry.getKey(),entry.getValue()));
        }
        return bucketSplitList;
    }
}
