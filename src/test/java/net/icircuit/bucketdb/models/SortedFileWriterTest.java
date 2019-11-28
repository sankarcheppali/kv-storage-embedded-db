package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SortedFileWriterTest {

    List<DataRecordWrapper> dataRecordList;
    SortedFileWriter sortedFileWriter;
    Path filePath;
    @Before
    public void prepareTest(){
        filePath = Paths.get("./sf-write-test.bdb");
        dataRecordList = new ArrayList<>(Arrays.asList(
                new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
                new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
                new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build())
        ));
        sortedFileWriter = new SortedFileWriter(dataRecordList,filePath);
    }

    @Test
    public void pressist() throws IOException {
        sortedFileWriter.pressist();
        //check file is present
        assertTrue("Sorted file not created",Files.exists(filePath));
        //check file size is more than 0 bytes
        assertTrue("DB File is empty",Files.size(filePath)>0);
    }

    @After
    public void tearDownTest() throws IOException {
        //remove file
         Files.delete(filePath);
    }

}