package net.icircuit.bucketdb.models;

import com.google.protobuf.InvalidProtocolBufferException;
import net.icircuit.bucketdb.models.proto.BlockFooterProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.proto.RecordIndexBlockProto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

//thread safe
public class DataBlockReader {
    private ByteBuffer byteBuffer;
    private RecordIndexBlock recordIndexBlock;
    public DataBlockReader(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }


    public DataRecordWrapper readDataRecord(RecordIndexBlock.IndexRecord indexRecord){
        return readDataRecord((int)indexRecord.getOffset(),(int)indexRecord.getLength());
    }
    public DataRecordWrapper readDataRecord(int recordOffset, int recordLength){
        ByteBuffer recordDataBuffer = slice(recordOffset,recordLength);
        try {
            return new DataRecordWrapper(DataRecord.parseFrom(recordDataBuffer));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public Collection<DataRecordWrapper> readAll() {
        return readRecordIndexBlock().getIndexList()
                .stream()
                .map(indexRecord -> readDataRecord(indexRecord))
                .collect(Collectors.toList());
    }
    public RecordIndexBlock readRecordIndexBlock() {
        try{
            if(recordIndexBlock == null){
                BlockFooter blockFooter = readDataBlockFooter();
                int indexBlockOffset = (int) blockFooter.getRibOffset();
                ByteBuffer indexBlockBuffer = slice(indexBlockOffset,blockFooter.getRibLength());
                recordIndexBlock =RecordIndexBlock.parseFrom(indexBlockBuffer);
            }
            return recordIndexBlock;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public BlockFooter readDataBlockFooter(){
        try{
            int footerLength = slice(length()-4,4).getInt();
            int footerOffset = length() - footerLength - 4;
            ByteBuffer footerBuffer = slice(footerOffset,footerLength);
            return BlockFooter.parseFrom(footerBuffer);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private synchronized ByteBuffer slice(long offset,long length){
        ByteBuffer buffer = byteBuffer.duplicate();
        buffer.position((int)offset);
        buffer.limit((int)(offset + length));
        return buffer;
    }
    private int length(){
        return byteBuffer.limit();
    }
}
