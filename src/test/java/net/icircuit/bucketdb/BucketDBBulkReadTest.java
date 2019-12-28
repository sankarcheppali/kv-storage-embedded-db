package net.icircuit.bucketdb;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import static net.icircuit.bucketdb.Util.deleteFolder;
import static org.junit.Assert.*;

public class BucketDBBulkReadTest {
    public static String dbPath = "../db-bread";
    public BucketDB bucketDB;

    @Before
    public void setup() throws IOException {
        cleanup();
        bucketDB = BucketDB.getInstance(dbPath);
        for(int i=0;i<1000000;i++){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("name","name"+i);
            jsonObject.put("address","address"+i);
            bucketDB.put("key"+i,jsonObject);
        }
    }
    @Test
    public void bulkRead(){
        Date start = new Date();
        for(int i=0;i<1000000;i++){
            Optional<JSONObject> optional = bucketDB.get("key"+i);
            assertTrue("missing key :"+"key"+i,optional.isPresent());
        }
        Date end = new Date();
        System.out.println("Read time "+(end.getTime()-start.getTime()));
    }
    @After
    public void cleanup() throws IOException {
        if(Paths.get(dbPath).toFile().exists()){
            deleteFolder(dbPath);
        }
    }
}
