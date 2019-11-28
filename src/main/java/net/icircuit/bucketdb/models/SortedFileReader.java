package net.icircuit.bucketdb.models;
import net.icircuit.bucketdb.models.proto.BlockFooterProto;
import net.icircuit.bucketdb.models.proto.DataIndexBlockProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.proto.FileFooterProto.*;
import net.icircuit.bucketdb.models.proto.RecordIndexBlockProto.*;
import net.icircuit.bucketdb.models.proto.BlockFooterProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;
import net.icircuit.bucketdb.models.proto.RecordIndexBlockProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;

import javax.imageio.IIOException;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

//thread safe
public class SortedFileReader {
    private final static Logger LOGGER = Logger.getLogger(SortedFileReader.class.getName());
    private ByteBuffer sortedFileBuffer;
    private int size;
    private FileFooter fileFooter;
    private DataIndexBlock dataIndexBlock;
    public SortedFileReader(ByteBuffer sortedFileBuffer,int size) {
        this.sortedFileBuffer = sortedFileBuffer;
        this.size = size;
    }
    public Collection<DataRecordWrapper> readAll(){
        DataIndexBlock dataIndexBlock = readDataIndexBlock();
        return dataIndexBlock.getIndexList().stream()
                .map(this::dataBlockReader)
                .flatMap(dataBlockReader -> dataBlockReader.readAll().stream())
                .collect(CustomeCollectors.toLinkedHashSet());
    }

    public DataIndexBlock readDataIndexBlock(){
        if(dataIndexBlock == null){
            try{
                FileFooter fileFooter = readFileFooter();
                ByteBuffer dataIndexBlockBuffer = slice(fileFooter.getDibOffset(),fileFooter.getDibLength());
                dataIndexBlock = DataIndexBlock.parseFrom(dataIndexBlockBuffer);
            }
            catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return dataIndexBlock;
    }

    public FileFooter readFileFooter() {
        if(fileFooter ==null){
            try{
                int  footerLength= slice(size-4,4).getInt();
                ByteBuffer footerBuffer = slice(size-footerLength-4,footerLength);
                fileFooter = FileFooter.parseFrom(footerBuffer);
            }catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return  fileFooter;
    }


    public DataBlockReader dataBlockReader(DataIndexBlock.IndexRecord indexRecord){
        return dataBlockReader(indexRecord.getOffset(),indexRecord.getLength());
    }

    public DataBlockReader dataBlockReader(long dataBlockOffset,long dataBlockLength){
        ByteBuffer blockBuffer = slice(dataBlockOffset,dataBlockLength);
        return new DataBlockReader(blockBuffer.slice());
    }

    public RecordIndexBlock readRecordIndexBlock(long dataBlockOffset,long dataBlockLength) throws IOException{
        BlockFooter blockFooter = readDataBlockFooter((int)dataBlockOffset,(int)dataBlockLength);
        int indexBlockOffset = (int)dataBlockOffset + (int) blockFooter.getRibOffset();
        ByteBuffer indexBlockBuffer = slice(indexBlockOffset,blockFooter.getRibLength());
        return RecordIndexBlock.parseFrom(indexBlockBuffer);
    }
    public BlockFooter readDataBlockFooter(int dataBlockOffset,int dataBlockLength) throws IOException{
        int footerLength = slice(dataBlockLength-4,4).getInt();
        int footerOffset = dataBlockOffset + dataBlockLength - footerLength - 4;
        ByteBuffer footerBuffer = slice(footerOffset,footerLength);
        return BlockFooter.parseFrom(footerBuffer);
    }
    private synchronized ByteBuffer slice(int offset,int length){
        ByteBuffer buffer = sortedFileBuffer.duplicate();
        buffer.position(offset);
        buffer.limit(offset + length);
        return buffer;
    }

    private synchronized ByteBuffer slice(long offset,long length){
        ByteBuffer buffer = sortedFileBuffer.duplicate();
        buffer.position((int)offset);
        buffer.limit((int)(offset + length));
        return buffer;
    }

}
