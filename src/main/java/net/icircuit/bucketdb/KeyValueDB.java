package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.KvDbOperationFailedException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;

public interface KeyValueDB<T> {
    /**
     * Gets the value associated with this key
     * @param key string with less than or equal to 32 chars
     * @return
     */
    Optional<JSONObject> get(String key);

    /**
     * Store a given key-value pair in the storage. Put will update
     * if the record is already is present
     * @param key - string with less than or equal to 32 chars
     * @param value - JSONObject value, max size is 16KB
     *
     */
    void put(String key, T value);

    /**
     *  Store a given key-value pair in the storage. Put will update
     *  if the record is already is present
     * @param key - string with less than or equal to 32 chars
     * @param value - JSONObject value, max size is 16KB
     * @param ttl - specifies how long this entry is valid in milli seconds
     *
     */
    void put(String key, T value, long ttl) throws KvDbOperationFailedException, IOException;

    /**
     * deletes the value associated with given key, will not throw any error if the key is not present
     * @param key
     * @throws KvDbOperationFailedException
     */
    void remove(String key);
}
