package net.icircuit.bucketdb.exceptions;

public class KvDbOperationFailedException extends RuntimeException {
    public KvDbOperationFailedException(String message) {
        super(message);
    }
}
