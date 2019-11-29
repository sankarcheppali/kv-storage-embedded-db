package net.icircuit.bucketdb;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;


public class BucketDBBulkWriteTest {
    public static String dbPath = "../db-bwrite";
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
    public void bulkWrite(){
        for(int i=0;i<1000000;i++){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("name","name"+i);
            jsonObject.put("address","address"+i);
            bucketDB.put("key"+i,jsonObject);
        }
        assertTrue("size of the db shold be more than 100MB",bucketDB.size()>100-0000-000);
    }
    @After
    public void cleanup() throws IOException {
        if(Paths.get(dbPath).toFile().exists()){
            Files.list(Paths.get(dbPath)).forEach(path -> path.toFile().delete());
            Paths.get(dbPath).toFile().delete();
        }
    }
}
