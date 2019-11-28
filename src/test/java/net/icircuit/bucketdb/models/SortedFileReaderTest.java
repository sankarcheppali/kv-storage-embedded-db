package net.icircuit.bucketdb.models;
import net.icircuit.bucketdb.models.proto.DataIndexBlockProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.proto.FileFooterProto;
import net.icircuit.bucketdb.models.proto.RecordIndexBlockProto.*;
import net.icircuit.bucketdb.models.proto.BlockFooterProto.*;
import net.icircuit.bucketdb.models.proto.DataRecordProto.*;

import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.junit.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SortedFileReaderTest {

    ByteBuffer sortedFileBuffer;
    SortedFileReader sortedFileReader;
    static Path filePath = Paths.get("./sf-read-test.bdb");;
    @BeforeClass
    public static void prepareDBFile() throws IOException{
        List<DataRecordWrapper> dataRecordList = new ArrayList<>(Arrays.asList(
                new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
                new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
                new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build())
        ));
        if(filePath.toFile().exists()){
            Files.delete(filePath);
        }
        SortedFileWriter sortedFileWriter = new SortedFileWriter(dataRecordList,filePath);
        sortedFileWriter.pressist();
    }

    @Before
    public void prepareTest() throws IOException{
        //create mapped buffer
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(),"r");
            FileChannel fc = randomAccessFile.getChannel();) {
            sortedFileReader  = new SortedFileReader(fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size()),(int)fc.size());
        }
    }
    @Test
    public void readFileFooter() throws IOException{
        //verify that footer
        FileFooterProto.FileFooter fileFooter = sortedFileReader.readFileFooter();
        assertTrue("offset is not is equal to total size of data blocks",fileFooter.getDibOffset()==84);
        assertTrue("length is not not equal to data index block size",fileFooter.getDibLength()==16);
    }

    @Test
    public void readDataIndexBlock() throws  IOException{
        DataIndexBlock dataIndexBlock = sortedFileReader.readDataIndexBlock();
        assertThat("data block count is not matching",dataIndexBlock.getIndexCount(),is(1));
        DataIndexBlock.IndexRecord indexRecord = dataIndexBlock.getIndex(0);
        assertThat("start key is not matching",indexRecord.getDbStartKey(),is("key1"));
        assertThat("end key is not matching",indexRecord.getDbEndKey(),is("key3"));
        assertThat("start off set is not 0",indexRecord.getOffset(),is(0L));
        assertThat("length is not matching",indexRecord.getLength(),is(84L));
    }

    @Test
    public void readRecordIndexBlock() throws IOException{
        DataIndexBlock dataIndexBlock = sortedFileReader.readDataIndexBlock();
        DataIndexBlock.IndexRecord indexRecord = dataIndexBlock.getIndex(0);
        DataBlockReader dataBlockReader = sortedFileReader.dataBlockReader(indexRecord.getOffset(),indexRecord.getLength());
        RecordIndexBlock recordIndexBlock = dataBlockReader.readRecordIndexBlock();
        //should have three entries
        assertThat("number of entries are not matching",recordIndexBlock.getIndexCount(),is(3));
    }

    @Test
    public void readDataRecord() throws IOException{
        DataIndexBlock dataIndexBlock = sortedFileReader.readDataIndexBlock();
        DataIndexBlock.IndexRecord dataBlockIndexRecord = dataIndexBlock.getIndex(0);
        DataBlockReader dataBlockReader = sortedFileReader.dataBlockReader(dataBlockIndexRecord.getOffset(),dataBlockIndexRecord.getLength());
        RecordIndexBlock recordIndexBlock = dataBlockReader.readRecordIndexBlock();
        RecordIndexBlock.IndexRecord dataRecordIndex = recordIndexBlock.getIndex(recordIndexBlock.getIndexCount()-1);
        DataRecord dataRecord =  dataBlockReader.readDataRecord(dataRecordIndex).getDataRecord();
        assertThat("key is not matching",dataRecord.getRKey(),is("key3"));
        assertThat("value is not matching",dataRecord.getRValue(),is("value3"));
    }

    @AfterClass
    public static void tearDownTest() throws IOException {
        //remove file
    }


}