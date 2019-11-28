package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.InvalidKeyException;
import net.icircuit.bucketdb.exceptions.InvalidTTLException;
import net.icircuit.bucketdb.exceptions.InvalidValueException;
import net.icircuit.bucketdb.exceptions.KvDbOperationFailedException;
import net.icircuit.bucketdb.models.Config;
import net.icircuit.bucketdb.models.DBWriter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BucketDB implements KeyValueDB<JSONObject>{

    private DBWriter dbWriter;
    private BucketDB(){}
    private BucketDB(String path){}


    @Override
    public CompletableFuture<Optional<JSONObject>> get(String key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> containsKey(String key) {
        return null;
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
