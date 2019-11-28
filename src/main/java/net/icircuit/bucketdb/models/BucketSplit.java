package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.util.Collection;
import java.util.List;

public class BucketSplit {
    private Bucket bucket; // bucket value can be null, in which case new bucket needs to be created to save this data records
    private Collection<DataRecordWrapper> dataRecordWrapperList;

    public BucketSplit(Bucket bucket, Collection<DataRecordWrapper> dataRecordWrapperList) {
        this.bucket = bucket;
        this.dataRecordWrapperList = dataRecordWrapperList;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public Collection<DataRecordWrapper> getDataRecordWrapperList() {
        return dataRecordWrapperList;
    }
}
