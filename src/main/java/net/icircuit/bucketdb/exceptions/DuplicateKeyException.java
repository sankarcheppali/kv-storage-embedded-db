package net.icircuit.bucketdb.exceptions;

import java.io.IOException;

public class DuplicateKeyException extends KvDbOperationFailedException {
    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(Exception e) {
        super(e);
    }
}
