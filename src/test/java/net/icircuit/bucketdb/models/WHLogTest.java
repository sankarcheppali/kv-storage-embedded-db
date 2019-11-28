package net.icircuit.bucketdb.models;

import net.icircuit.bucketdb.models.proto.DataRecordProto;
import net.icircuit.bucketdb.models.wrappers.DataRecordWrapper;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class WHLogTest {

    public static Path dbPath = Paths.get("./");
    public static List<DataRecordWrapper> dataRecordList = new ArrayList<>(Arrays.asList(
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key1").setRValue("value1").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key2").setRValue("value2").build()),
            new DataRecordWrapper(DataRecordProto.DataRecord.newBuilder().setRKey("key3").setRValue("value3").build())
            ));

    @Test
    public void createAndDelete() throws IOException {
        //create whl file and see if it is actually present on the disk
        int lengthBeforeCreating = WHLog.loadWHLFiles(dbPath).size();
        WHLog whLog =WHLog.create(dbPath);
        assertTrue("WHL file is not created",whLog.whlFilePath.toFile().exists());
        int lengthAfterCreating = WHLog.loadWHLFiles(dbPath).size();
        assertTrue("failed to list whl files",lengthAfterCreating > lengthBeforeCreating);
        whLog.delete();
        assertTrue("WHL count did not reduce",WHLog.loadWHLFiles(dbPath).size()==lengthBeforeCreating);
    }
    @Test
    public void addAndReadAll() throws IOException {
         WHLog whLog = WHLog.create(dbPath);
         dataRecordList.forEach(dataRecordWrapper -> {
             try {
                 whLog.add(dataRecordWrapper.getDataRecord());
             } catch (IOException e) {
                 e.printStackTrace();
             }
         });
         Collection<DataRecordProto.DataRecord> records= whLog.readAll();
         assertThat("data records length did not match ",records.size(),is(dataRecordList.size()));
         assertArrayEquals(records.toArray(),dataRecordList.stream().map(DataRecordWrapper::getDataRecord).toArray());
         whLog.delete();
    }


}