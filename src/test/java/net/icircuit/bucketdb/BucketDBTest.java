package net.icircuit.bucketdb;

import net.icircuit.bucketdb.models.Config;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class BucketDBTest {
    public static String dbPath = "../db";
    public BucketDB bucketDB;
    @Before
    public void setUp() throws IOException, ExecutionException, InterruptedException {
        cleanup();
        bucketDB = BucketDB.getInstance(dbPath);
        bucketDB.put("key1", new JSONObject(new HashMap<String, String>() {{
            put("name", "name-1");
            put("address", "address-3");
        }}));
        bucketDB.put("key2", new JSONObject(new HashMap<String, String>() {{
            put("name", "name-1");
            put("address", "address-3");
        }}));
        bucketDB.put("key3", new JSONObject(new HashMap<String, String>() {{
            put("name", "name-3");
            put("address", "address-3");
        }}));

    }
    @Test
    public void basicOperations() throws ExecutionException, InterruptedException {
         //get a key
         Optional<JSONObject>  value= bucketDB.get("key2");
         assertTrue("value should be present",value.isPresent());

         //delete key
        bucketDB.remove("key2");
        Optional<JSONObject> value2= bucketDB.get("key2");
        assertTrue("deleted value should not be present",!value2.isPresent());
    }
    @After
    public void cleanup() throws IOException {
        if(Paths.get(dbPath).toFile().exists()){
            Files.list(Paths.get(dbPath)).forEach(path -> path.toFile().delete());
            Paths.get(dbPath).toFile().delete();
        }
    }

}