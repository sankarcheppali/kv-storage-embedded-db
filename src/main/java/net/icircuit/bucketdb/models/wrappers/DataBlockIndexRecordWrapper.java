package net.icircuit.bucketdb.models.wrappers;

import net.icircuit.bucketdb.models.proto.DataIndexBlockProto;

public class DataBlockIndexRecordWrapper implements KeyRange,Comparable<KeyRange> {
    DataIndexBlockProto.DataIndexBlock.IndexRecord indexRecord;

    public DataBlockIndexRecordWrapper(DataIndexBlockProto.DataIndexBlock.IndexRecord indexRecord) {
        this.indexRecord = indexRecord;
    }
    public DataIndexBlockProto.DataIndexBlock.IndexRecord getIndexRecord() {
        return indexRecord;
    }

    @Override
    public String getStartKey() {
        return indexRecord.getDbStartKey();
    }

    @Override
    public String getEndKey() {
        return indexRecord.getDbEndKey();
    }

    /**
     * Semantics of this override does not exactly match with the compareTo
     * return equal if the given key range fits in this keyrange
     * only works if we are searching for a key in collection of ranges,doesn't work with over lapping key ranges
     * @param o
     * @return
     */
    @Override
    public int compareTo(KeyRange o) {
        if(getStartKey().compareTo(o.getStartKey()) <=0 && getEndKey().compareTo(o.getEndKey())>=0){
            return 0;
        }
        if(getStartKey().compareTo(o.getStartKey())>0){ // that is the startKey is outside of this range
            return 1;
        }
        if(getEndKey().compareTo(o.getEndKey())<0){
            return -1;
        }
        return 0;
    }
}
