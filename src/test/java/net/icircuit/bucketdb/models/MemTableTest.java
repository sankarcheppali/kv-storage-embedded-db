package net.icircuit.bucketdb.models;


import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import net.icircuit.bucketdb.models.wrappers.Pair;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static net.icircuit.bucketdb.Util.deleteFolder;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
public class MemTableTest {
    public static Path dbPath = Paths.get("../db-memtable");
    public static Collection<DataRecordWrapper> dataRecordList1 = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key4").setRValue("value4").build())
    ));


    @BeforeClass
    public static void setup() throws IOException {
        if(!dbPath.toFile().exists())
            Files.createDirectory(dbPath);
    }
    @Test
    public void saveAndRead() throws IOException {
        final MemTable memTable1 = new MemTable(dbPath);
        //add records
        dataRecordList1.stream().forEach(dataRecordWrapper -> {
            try {
                memTable1.addRecord(dataRecordWrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //load from whl log and see if the records are present
        final MemTable memTable2 = new MemTable(dbPath);
        Optional<DataRecordWrapper>  dataRecordWrapper= memTable2.getRecord("key2");
        assertTrue("data record should be present",dataRecordWrapper.isPresent());
        assertThat("value should match",dataRecordWrapper.get().getKey(),is("key2"));
        memTable2.deleteRecord("key2");

        final MemTable memTable3 = new MemTable(dbPath);
        Optional<DataRecordWrapper>  dataRecordWrapper2= memTable2.getRecord("key2");
        assertTrue("data record should be present",dataRecordWrapper2.isPresent());
        assertTrue("data record should be present but invalid",!dataRecordWrapper2.get().isValid());
    }
    @Test
    public void testSpill() throws IOException {
        final MemTable memTable = new MemTable(dbPath);
        //add enough records for spil
        WHLog whLogBeforeSpil = memTable.getCurrentWHLog();
        assertTrue("memtable should not be ready for spill",!memTable.isReadyForSpill());
        for(int i=0;i<80000;i++){
            dataRecordList1.stream().forEach(dataRecordWrapper -> {
                try {
                    memTable.addRecord(dataRecordWrapper);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        assertTrue("memtable should be ready for spill",memTable.isReadyForSpill());
        Pair<WHLog, Map<String, DataRecordWrapper>> whLogMapPair= memTable.whlogReadyForSpill();
        Bucketizer bucketizer = new Bucketizer(whLogMapPair.getValue().values());
        List<BucketSplit> bucketSplitList = bucketizer.bucketize(null);
        assertThat("there should be only one bucket",bucketSplitList.size(),is(1));
        assertThat("number of records should be equal to input",bucketSplitList.get(0).getDataRecordWrapperList().size(),is(dataRecordList1.size()));
        memTable.bucketizationCompleted(whLogMapPair.getKey());
        WHLog whLogAfterSpill = memTable.getCurrentWHLog();
        assertTrue("memtable should have new whllog",!whLogAfterSpill.equals(whLogBeforeSpil));
    }


    @Before
    @After
    public void delete() throws IOException {
        WHLog.loadWHLFiles(dbPath).forEach(whLog -> {
            try {
                whLog.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @AfterClass
    public static void cleanup() throws IOException {
        deleteFolder(dbPath);
    }
}