package net.icircuit.bucketdb.exceptions;

public class InvalidValueException extends KvDbOperationFailedException {
    public InvalidValueException(String message) {
        super(message);
    }
}
