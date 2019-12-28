package net.icircuit.bucketdb.exceptions;

import java.io.IOException;

public class KvDbOperationFailedException extends RuntimeException {
    public KvDbOperationFailedException(String message) {
        super(message);
    }

    public KvDbOperationFailedException(Exception e) {
        super(e);
    }
}
