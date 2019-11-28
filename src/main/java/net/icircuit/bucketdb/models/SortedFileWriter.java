package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.*;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * take Sorted list of records and create sorted file
 */

public class  SortedFileWriter{
    private final static Logger LOGGER = Logger.getLogger(SortedFileWriter.class.getName());

    private List<DataRecordWrapper> records;
    private List<DataBlock> dataBlockList;
    private DataIndexBlockProto.DataIndexBlock dataIndexBlock;
    private FileFooterProto.FileFooter fileFooter;
    private Path filePath;

    public SortedFileWriter(Collection<DataRecordWrapper> records, Path filePath) {
        this.records = new ArrayList<>(records);
        this.filePath = filePath;
    }

    public void pressist() throws IOException {
        build();
        //create file
        if(!filePath.toFile().exists()){
            Files.createFile(filePath);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile(), false);
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        //write datablocks
        for (DataBlock dataBlock : dataBlockList) {
            dataBlock.writeTo(dataOutputStream);
        }
        //write data index block
        dataIndexBlock.writeTo(dataOutputStream);
        //write footer
        fileFooter.writeTo(dataOutputStream);
        //write 4 byte footer length
        dataOutputStream.writeInt(fileFooter.getSerializedSize());
        //close the stream
        dataOutputStream.close();
    }

    private int numberOfBlocks(){
        return (int) Math.ceil((records.size()*1.0)/Config.RECORDS_PER_BLOCK);
    }
    private void build() {
        int numberOfBlocks = numberOfBlocks();
        dataBlockList = new ArrayList<>();
        //split the records into datablocks
        for (int i = 0; i < numberOfBlocks; i++) {
            int startIndex = i * Config.RECORDS_PER_BLOCK;
            int endIndex = records.size()<= startIndex + Config.RECORDS_PER_BLOCK ? records.size() : startIndex + Config.RECORDS_PER_BLOCK;
            DataBlock dataBlock = new DataBlock(records.subList(startIndex, endIndex));
            LOGGER.info("Datablock created - "+dataBlock);
            dataBlockList.add(dataBlock);
        }
        //create data index block
        List<DataIndexBlockProto.DataIndexBlock.IndexRecord> indexRecordList = new ArrayList<>();
        int offset = 0;
        for (DataBlock dataBlock : dataBlockList) {
            indexRecordList.add(createIndexRecord(dataBlock, offset));
            offset = offset + dataBlock.getSerializedSize();
        }
        dataIndexBlock = DataIndexBlockProto.DataIndexBlock.newBuilder()
                .addAllIndex(indexRecordList)
                .build();
        fileFooter = FileFooterProto.FileFooter.newBuilder()
                .setDibOffset(offset) // data index block offset
                .setDibLength(dataIndexBlock.getSerializedSize())
                .build();
    }


    private DataIndexBlockProto.DataIndexBlock.IndexRecord createIndexRecord(DataBlock dataBlock, int offset) {
        return DataIndexBlockProto.DataIndexBlock.IndexRecord.newBuilder()
                .setDbStartKey(dataBlock.startKey())
                .setDbEndKey(dataBlock.endKey())
                .setOffset(offset)
                .setLength(dataBlock.getSerializedSize())
                .build();
    }


    class DataBlock {
        private List<DataRecordWrapper> records;
        private RecordIndexBlockProto.RecordIndexBlock recordIndexBlock;
        BlockFooterProto.BlockFooter blockFooter;
        int serializedSize = 0;

        public DataBlock(List<DataRecordWrapper> records) {
            this.records = records;
        }

        public String startKey() {
            assert !records.isEmpty();
            return records.get(0).getDataRecord().getRKey();
        }

        public String endKey() {
            assert !records.isEmpty();
            return records.get(records.size() - 1).getDataRecord().getRKey();
        }

        //loop over all the records and prepare record index block
        private void build() {
            if (recordIndexBlock != null && blockFooter != null) return;
            int offset = 0;
            List<RecordIndexBlockProto.RecordIndexBlock.IndexRecord> indexRecordList = new ArrayList<>();
            for (DataRecordWrapper dataRecordWrapper : records) {
                DataRecordProto.DataRecord dataRecord = dataRecordWrapper.getDataRecord();
                indexRecordList.add(createIndexRecord(dataRecord, offset));
                offset = offset + dataRecord.getSerializedSize();
            }
            recordIndexBlock = RecordIndexBlockProto.RecordIndexBlock.newBuilder()
                    .addAllIndex(indexRecordList)
                    .build();
            blockFooter = BlockFooterProto.BlockFooter.newBuilder()
                    .setRibOffset(offset)
                    .setRibLength(recordIndexBlock.getSerializedSize())
                    .build();
            serializedSize = offset +recordIndexBlock.getSerializedSize() + blockFooter.getSerializedSize() + 4;
        }

        void writeTo(DataOutputStream os) throws IOException {
            //first write records,then record index block, then footer
            build();
            for (DataRecordWrapper dataRecordWrapper : records) {
                dataRecordWrapper.getDataRecord().writeTo(os);
            }
            recordIndexBlock.writeTo(os);
            blockFooter.writeTo(os);
            //write the length of the blockfooter
            int footerSize = blockFooter.getSerializedSize();
            os.writeInt(footerSize);
            //one full datablock has been written to output stream
        }

        int getSerializedSize() {
            build();
            return serializedSize;
        }

        private RecordIndexBlockProto.RecordIndexBlock.IndexRecord createIndexRecord(DataRecordProto.DataRecord dataRecord, long offset) {
            return RecordIndexBlockProto.RecordIndexBlock.IndexRecord.newBuilder()
                    .setRKey(dataRecord.getRKey())
                    .setOffset(offset)
                    .setLength(dataRecord.getSerializedSize())
                    .build();
        }

        public String toString(){
            return startKey()+":"+endKey()+",number of records:"+records.size()+", serialized size:"+getSerializedSize();
        }
    }
}
