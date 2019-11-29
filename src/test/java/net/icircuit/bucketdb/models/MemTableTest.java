package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
public class MemTableTest {
    public static Path dbPath = Paths.get("../db");
    public static Collection<DataRecordWrapper> dataRecordList1 = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key4").setRValue("value4").build())
    ));
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
}