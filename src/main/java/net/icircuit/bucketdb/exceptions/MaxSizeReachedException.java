package net.icircuit.bucketdb.exceptions;

public class MaxSizeReachedException extends KvDbOperationFailedException {
    public MaxSizeReachedException(String message) {
        super(message);
    }

    public MaxSizeReachedException(Exception e) {
        super(e);
    }
}
