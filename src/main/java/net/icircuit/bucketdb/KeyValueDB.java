package net.icircuit.bucketdb;

import net.icircuit.bucketdb.exceptions.KvDbOperationFailedException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface KeyValueDB<T> {
    CompletableFuture<Optional<T>> get(String key);
    CompletableFuture<Boolean> containsKey(String key);
    CompletableFuture put(String key, T value) throws KvDbOperationFailedException;
    CompletableFuture put(String key,T value,long ttl) throws KvDbOperationFailedException;
    CompletableFuture remove(String key) throws KvDbOperationFailedException;
}
