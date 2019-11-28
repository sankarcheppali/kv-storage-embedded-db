package net.icircuit.bucketdb.exceptions;

public class InvalidKeyException extends KvDbOperationFailedException {
    public InvalidKeyException(String message) {
        super(message);
    }
}
