package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.DuplicateKeyException;
import net.icircuit.bucketdb.exceptions.KeyNotFoundException;
import net.icircuit.bucketdb.models.Config;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static net.icircuit.bucketdb.Util.deleteFolder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class BucketDBTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    public static String dbPath = "../db";
    public BucketDB bucketDB;

    @Before
    public void setUp() throws IOException{
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
    public void shouldNotCrateMoreThanOneDBTest() throws IOException {
        assertThat("for given folder, only one db process should be created",
                bucketDB,
                is(BucketDB.getInstance(dbPath)));
    }
    @Test
    public void basicOperations() {
        //get a key
        Optional<JSONObject> value = bucketDB.get("key2");
        assertTrue("value should be present", value.isPresent());

        //delete key
        bucketDB.remove("key2");
        Optional<JSONObject> value2 = bucketDB.get("key2");
        assertTrue("deleted value should not be present", !value2.isPresent());
    }

    @Test
    public void throwOnDuplicateKeyTest() {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("name","name");
        jsonObject.put("address","address");
        thrown.expect(DuplicateKeyException.class);
        bucketDB.create("key3",jsonObject);
    }

    @Test
    public void throwOnMissingKeyTest(){
        thrown.expect(KeyNotFoundException.class);
        bucketDB.delete("key4");
    }
    @After
    public void cleanup() throws IOException {
        deleteFolder(dbPath);
    }

}