package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.KvDbOperationFailedException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;

public interface KeyValueDB<T> {
    Optional<JSONObject> get(String key);
    void put(String key, T value) throws KvDbOperationFailedException;
    void put(String key, T value, long ttl) throws KvDbOperationFailedException, IOException;
    void remove(String key) throws KvDbOperationFailedException;
}
