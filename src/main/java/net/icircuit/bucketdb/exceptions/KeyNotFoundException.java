package net.icircuit.bucketdb.exceptions;

import java.io.IOException;

public class KeyNotFoundException extends KvDbOperationFailedException {
    public KeyNotFoundException(String message) {
        super(message);
    }

    public KeyNotFoundException(Exception e) {
        super(e);
    }
}
