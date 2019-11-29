package net.icircuit.bucketdb;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import static net.icircuit.bucketdb.Util.deleteFolder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BucketDBRemoveTest {
    public static String dbPath = "../db-rread";
    public BucketDB bucketDB;

    @Before
    public void setup() throws IOException, InterruptedException {
        cleanup();
        bucketDB = BucketDB.getInstance(dbPath);
        for(int i=0;i<1000000;i++){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("name","name"+i);
            jsonObject.put("address","address"+i);
            bucketDB.put("key"+i,jsonObject);
            if(i == 700000 ){
                bucketDB.remove("key10");//for remove test
                bucketDB.put("key41",jsonObject,1);//ttl expiry test
                bucketDB.put("key71",jsonObject);//update test
            }
        }
    }
    @Test
    public void removeAndUpdateTest(){
        Optional<JSONObject> optional1= bucketDB.get("key10");
        assertTrue("key should not be present",!optional1.isPresent());
        Optional<JSONObject> optional2= bucketDB.get("key41");
        assertTrue("key should not be present",!optional2.isPresent());
        Optional<JSONObject> optional3= bucketDB.get("key71");
        assertTrue("updated key should  be present",optional3.isPresent());
        assertThat("update value should be fetched",optional3.get().getString("name"),is("name700000"));
    }
    @After
    public void cleanup() throws IOException, InterruptedException {
        if(Paths.get(dbPath).toFile().exists()){
            Thread.sleep(5000);
            deleteFolder(dbPath);
        }
    }
}
