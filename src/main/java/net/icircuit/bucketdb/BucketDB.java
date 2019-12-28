package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.*;
import net.icircuit.bucketdb.models.*;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BucketDB implements KeyValueDB<JSONObject>{

    private static Map<Path,BucketDB> dbs = new HashMap<>();
    private Manifest manifest;
    private DBWriter dbWriter;
    private DBReader dbReader;

    private BucketDB(Path path) throws IOException {
        manifest = new Manifest(path);
        dbWriter = manifest.getDBWriter();
        dbReader = manifest.getDbReader();
    }
    public  static BucketDB getInstance() throws IOException {
        return getInstance(Config.DEFAULT_DB_PATH);
    }
    public synchronized static BucketDB getInstance(String path) throws IOException {
        Path dbPath = Paths.get(path);
        if(dbPath.toFile().exists() && !dbPath.toFile().isDirectory()){
            throw new IllegalArgumentException("path should be a directory");
        }
        if(!dbs.containsKey(dbPath)){
            dbs.put(dbPath,new BucketDB(dbPath));
        }
        return dbs.get(dbPath);
    }

    public CompletableFuture<Optional<JSONObject>> getAsync(String key) {
        checkArguments(key);
        return CompletableFuture.supplyAsync(()-> dbReader.get(key));
    }


    public CompletableFuture putAsync(String key, JSONObject value){
        checkArguments(key,value);
        return CompletableFuture.runAsync(() -> {
            try {
                dbWriter.add(key,value);
            } catch (IOException e) {
                e.printStackTrace();
                throw  new CompletionException(e);
            }
        });
    }

    public CompletableFuture putAsync(String key, JSONObject value, long ttl) {
        checkArguments(key,value,ttl);
        return CompletableFuture.runAsync(() -> {
            try {
                dbWriter.add(key,value,ttl);
            } catch (IOException e) {
                e.printStackTrace();
                throw  new CompletionException(e);
            }
        });
    }

    public CompletableFuture removeAsync(String key) {
        checkArguments(key);
        return CompletableFuture.runAsync(() -> {
            try {
                dbWriter.delete(key);
            } catch (IOException e) {
                e.printStackTrace();
                throw new CompletionException(e);
            }
        });
    }
    private boolean checkArguments(String key){
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }
        return true;
    }
    private boolean checkArguments(String key,JSONObject value){
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }

        if(value.toString().getBytes().length > Config.MAX_VALUE_SIZE){
            throw new IllegalArgumentException(new InvalidValueException("Value size should be less than "+Config.MAX_VALUE_SIZE+" bytes"));
        }
        return true;
    }
    private boolean checkArguments(String key,JSONObject value,long ttl){
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }
        if(ttl < 1){
            throw new IllegalArgumentException(new InvalidTTLException("TTL should be a positive number"));
        }
        if(value.toString().getBytes().length > Config.MAX_VALUE_SIZE){
            throw new IllegalArgumentException(new InvalidValueException("Value size should be less than "+Config.MAX_VALUE_SIZE+" bytes"));
        }
        return true;
    }

    /**
     * Gets the value associated with this key
     * @param key string with less than or equal to 32 chars
     * @return
     */
    @Override
    public Optional<JSONObject> get(String key) {
        checkArguments(key);
        return dbReader.get(key);
    }

    /**
     * Store a given key-value pair in the storage. Put will update
     *  if the record is already is present
     * @param key - string with less than or equal to 32 chars
     * @param value - JSONObject value, max size is 16KB
     */
    @Override
    public void put(String key, JSONObject value)  {
        checkArguments(key, value);
        try {
            dbWriter.add(key,value);
        } catch (IOException e) {
            e.printStackTrace();
            throw new KvDbOperationFailedException(e);
        }
    }

    /**
     * Store a given key-value pair in the storage. Put will update
     * if the record is already is present
     * @param key - string with less than or equal to 32 chars
     * @param value - JSONObject value, max size is 16KB
     * @param ttl - specifies how long this entry is valid in milli seconds
     */
    @Override
    public void put(String key, JSONObject value, long ttl) {
        checkArguments(key,value,ttl);
        try {
            dbWriter.add(key,value,ttl);
        } catch (IOException e) {
            e.printStackTrace();
            throw new KvDbOperationFailedException(e);
        }
    }

    /**
     * deletes the value associated with given key, will not throw any error if the key is not present
     * @param key
     */
    @Override
    public void remove(String key) {
        checkArguments(key);
        try {
            dbWriter.delete(key);
        } catch (IOException e) {

            e.printStackTrace();
            throw new KvDbOperationFailedException(e);
        }
    }

    /**
     * similar to put, but thorws exception is key is already present
     * @param key
     * @param value
     */
    public void create(String key,JSONObject value){
        Optional<JSONObject> optional = get(key);
        if(optional.isPresent()){
            throw new DuplicateKeyException(key +" is already present");
        }
        if(size()>Config.MAX_DB_SIZE){
            throw new MaxSizeReachedException("Max allowed DB size is "+Config.MAX_DB_SIZE);
        }
        put(key,value);
    }

    /**
     * similar to put, but throws exception is key is already present
     *
     * @param key
     * @param value
     * @param ttl
     */
    public void create(String key,JSONObject value,long ttl){
        Optional<JSONObject> optional = get(key);
        if(optional.isPresent()){
            throw new DuplicateKeyException(key +" is already present");
        }
        if(size()>Config.MAX_DB_SIZE){
            throw new MaxSizeReachedException("Max allowed DB size is "+Config.MAX_DB_SIZE);
        }
        put(key,value,ttl);
    }
    /**
     * similar to remove, but throws exception is key is not present
     * @param key
     */
    public void delete(String key){
        Optional<JSONObject> optional = get(key);
        if(!optional.isPresent()){
            throw new KeyNotFoundException(key +" is not present");
        }
        remove(key);
    }

    public long size(){
        return manifest.size();
    }
}
