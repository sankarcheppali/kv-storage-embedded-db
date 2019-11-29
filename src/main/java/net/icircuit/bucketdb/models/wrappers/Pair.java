package net.icircuit.bucketdb.models.wrappers;

public class Pair<T,R> {
    private T key;
    private R value;

    public Pair(T key, R value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public R getValue() {
        return value;
    }
}
