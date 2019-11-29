package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.InvalidKeyException;
import net.icircuit.bucketdb.exceptions.InvalidTTLException;
import net.icircuit.bucketdb.exceptions.InvalidValueException;
import net.icircuit.bucketdb.exceptions.KvDbOperationFailedException;
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

    @Override
    public CompletableFuture<Optional<JSONObject>> get(String key) {
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }
        return CompletableFuture.supplyAsync(()-> dbReader.get(key));
    }


    @Override
    public CompletableFuture put(String key, JSONObject value){
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }
        if(value.toString().getBytes().length > Config.MAX_VALUE_SIZE){
            throw new IllegalArgumentException(new InvalidValueException("Value size should be less than "+Config.MAX_VALUE_SIZE+" bytes"));
        }
        return CompletableFuture.runAsync(() -> {
            try {
                dbWriter.add(key,value);
            } catch (IOException e) {
                e.printStackTrace();
                throw  new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture put(String key, JSONObject value, long ttl) {
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }
        if(ttl < 1){
            throw new IllegalArgumentException(new InvalidTTLException("TTL should be a positive number"));
        }
        if(value.toString().getBytes().length > Config.MAX_VALUE_SIZE){
            throw new IllegalArgumentException(new InvalidValueException("Value size should be less than "+Config.MAX_VALUE_SIZE+" bytes"));
        }
        return CompletableFuture.runAsync(() -> {
            try {
                dbWriter.add(key,value,ttl);
            } catch (IOException e) {
                e.printStackTrace();
                throw  new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture remove(String key) {
        if(key.length() > Config.MAX_KEY_LENGTH){
            throw new IllegalArgumentException(new InvalidKeyException("Key length should be less than or equal to "+Config.MAX_KEY_LENGTH));
        }
        return CompletableFuture.runAsync(() -> {
            try {
                dbWriter.delete(key);
            } catch (IOException e) {
                e.printStackTrace();
                throw new CompletionException(e);
            }
        });
    }
}
