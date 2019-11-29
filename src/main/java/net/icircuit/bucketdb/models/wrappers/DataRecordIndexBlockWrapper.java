package net.icircuit.bucketdb.models.wrappers;

import net.icircuit.bucketdb.models.proto.DataIndexBlockProto;
import net.icircuit.bucketdb.models.proto.RecordIndexBlockProto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataRecordIndexBlockWrapper {
    private RecordIndexBlockProto.RecordIndexBlock indexBlock;
    private List<String> keys;
    public DataRecordIndexBlockWrapper(RecordIndexBlockProto.RecordIndexBlock indexBlock) {
        this.indexBlock = indexBlock;
        keys = indexBlock.getIndexList().stream().map(indexRecord -> indexRecord.getRKey()).collect(Collectors.toList());
    }

    public Optional<RecordIndexBlockProto.RecordIndexBlock.IndexRecord> search(String key){
         int index= Collections.binarySearch(keys,key);
         if(index < 0){
             return Optional.empty();
         }
         return Optional.of(indexBlock.getIndexList().get(index));
    }
    public RecordIndexBlockProto.RecordIndexBlock getIndexBlock() {
        return indexBlock;
    }
}
