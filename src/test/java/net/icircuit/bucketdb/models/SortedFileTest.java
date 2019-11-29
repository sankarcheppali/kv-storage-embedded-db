package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static net.icircuit.bucketdb.Util.deleteFolder;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SortedFileTest {

    public static Path bucketPath = Paths.get("../db-sortedfile");
    public static List<DataRecordWrapper> dataRecordList = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build())
    ));


    @BeforeClass
    public static void setup() throws IOException {
        if(!bucketPath.toFile().exists())
            Files.createDirectory(bucketPath);
    }
    @Test
    public void persistAndRead() throws IOException {
        Path sortedFilePath = SortedFile.createFile(bucketPath);
        SortedFile.persist(sortedFilePath, dataRecordList);
        assertTrue("Failed to create new sorted file",sortedFilePath.toFile().exists());
        SortedFile sortedFile = new SortedFile(sortedFilePath);
        assertThat("start key did not match",sortedFile.startKey(),is(dataRecordList.get(0).getDataRecord().getRKey()));
        assertThat("end key did not match",sortedFile.endKey(),is(dataRecordList.get(dataRecordList.size()-1).getDataRecord().getRKey()));
        assertTrue("key should be present",sortedFile.isInclusiveOf("key2"));
        assertTrue("key should not be present",!sortedFile.isInclusiveOf("key4"));
        assertTrue("key should not be present",!sortedFile.isInclusiveOf("key0"));
    }


    @Test
    public void getRecord() throws IOException {
        Path sortedFilePath = SortedFile.createFile(bucketPath);
        SortedFile.persist(sortedFilePath, dataRecordList);
        SortedFile sortedFile = new SortedFile(sortedFilePath);
        Optional<DataRecordWrapper> optionalDataRecordWrapper = sortedFile.getRecord("key2");
        assertTrue("record should be present",optionalDataRecordWrapper.isPresent());
        assertThat("value should match",optionalDataRecordWrapper.get().getDataRecord().getRValue(),is("value2"));
        optionalDataRecordWrapper = sortedFile.getRecord("key4");
        assertTrue("record should not be present",!optionalDataRecordWrapper.isPresent());
    }

    @Test
    public void readAll() throws IOException {
        Path sortedFilePath = SortedFile.createFile(bucketPath);
        SortedFile.persist(sortedFilePath, dataRecordList);
        SortedFile sortedFile = new SortedFile(sortedFilePath);
        Collection<DataRecordWrapper>  records= sortedFile.readAll();
        assertArrayEquals(records.toArray(),dataRecordList.toArray());
    }


    @Test
    public void listAndDelete() throws IOException {
        SortedFile.createFile(bucketPath);
        List<Path> sortefPathList= SortedFile.list(bucketPath);
        int sizeBeforeDelete = sortefPathList.size();
        sortefPathList.forEach(path -> path.toFile().delete());
        int sizeAfterDelete = SortedFile.list(bucketPath).size();
        assertTrue("listing failed",sizeBeforeDelete > 0);
        assertThat("deletion failed",sizeAfterDelete,is(0));
    }

    @Before
    @After
    public void delete() throws IOException {
        List<Path> sortefPathList= SortedFile.list(bucketPath);
        sortefPathList.forEach(path -> path.toFile().delete());
    }

    @AfterClass
    public static void cleanup() throws IOException {
        deleteFolder(bucketPath);
    }

}