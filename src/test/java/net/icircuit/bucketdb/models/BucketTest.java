package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
public class BucketTest {
    public static Path dbPath = Paths.get("../db-bucket-test");
    public static Collection<DataRecordWrapper> dataRecordList1 = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key4").setRValue("value4").build())
    )).stream().collect(CustomeCollectors.toTreeSet());
    public static Collection<DataRecordWrapper> dataRecordList2 = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value11").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key12").setRValue("value12").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").setTimestamp(new Date().getTime()).setTtl(-1).build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key14").setRValue("value14").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key5").setRValue("value5").build())
    )).stream().collect(CustomeCollectors.toTreeSet());
    public static Collection<DataRecordWrapper> dataRecordList3 = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key31").setRValue("value11").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key32").setRValue("value12").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key34").setRValue("value14").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key5").setRValue("value35").build())
    )).stream().collect(CustomeCollectors.toTreeSet());

    @BeforeClass
    public static void setup() throws IOException {
        if(!dbPath.toFile().exists())
            Files.createDirectory(dbPath);
    }
    @Test
    public void manifestFiles() {
        Bucket bucket = Bucket.create(dbPath);
        bucket.createSortedFile(dataRecordList1);
        bucket.createSortedFile(dataRecordList2);
        assertTrue("failed to create manifest file",bucket.listManifestFiles().size()==3);
        Bucket bucket2 = new Bucket(bucket.getBucketFolderPath());
        assertTrue("failed to delete old manifest file",bucket.listManifestFiles().size()==1);
        assertThat("bucket is not loading latest manifest file ",bucket.getManifestFile(),is(bucket2.getManifestFile()));
        //check if both sorted files are loaded
        assertThat("missing key",bucket2.getStartKey(),is("key1"));
        assertThat("missing key",bucket2.getEndKey(),is("key5"));
    }

    @Test
    public void getRecord() {
        Bucket bucket = Bucket.create(dbPath);
        //key should not be present
        Optional<DataRecordWrapper> optionalDataRecordWrapper = bucket.getRecord("key3");
        assertThat("key should not be present",optionalDataRecordWrapper.isPresent(),is(false));

        bucket.createSortedFile(dataRecordList1);
        optionalDataRecordWrapper = bucket.getRecord("key3");
        assertThat("key should be present",optionalDataRecordWrapper.isPresent(),is(true));

        bucket.createSortedFile(dataRecordList2);
        optionalDataRecordWrapper = bucket.getRecord("key3");
        //key should not be present
        assertThat("key should not be present",optionalDataRecordWrapper.isPresent(),is(false));

        assertThat("key sholud have updated value",
                bucket.getRecord("key1").get().getDataRecord().getRValue(),is("value11"));
    }

    @Test
    public void compaction(){
        Bucket bucket = Bucket.create(dbPath);
        bucket.createSortedFile(dataRecordList1);
        assertThat("compaction should not be required",bucket.readyForCompaction(),is(false));
        bucket.createSortedFile(dataRecordList2);
        bucket.createSortedFile(dataRecordList3);
        bucket.createSortedFile(dataRecordList3);
        bucket.createSortedFile(dataRecordList3);
        bucket.createSortedFile(dataRecordList3);
        bucket.createSortedFile(dataRecordList3);
        assertThat("compaction should be required",bucket.readyForCompaction(),is(true));
        bucket.runCompaction();
        assertThat("compaction should not be required",bucket.readyForCompaction(),is(false));
        assertThat("key should have updated value",
                bucket.getRecord("key5").get().getDataRecord().getRValue(),is("value35"));
    }

    @Before
    @After
    public  void delete() throws IOException {
        Bucket.list(dbPath).forEach(path -> {
            try {
                Files.list(path).forEach(path1 -> path1.toFile().delete());
                path.toFile().delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}