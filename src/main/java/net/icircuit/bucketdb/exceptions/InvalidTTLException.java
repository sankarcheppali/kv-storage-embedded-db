package net.icircuit.bucketdb.exceptions;

public class InvalidTTLException extends KvDbOperationFailedException {
    public InvalidTTLException(String message) {
        super(message);
    }
}
