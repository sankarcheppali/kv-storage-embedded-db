package net.icircuit.bucketdb.models;

public class Config {
    public static String DEFAULT_DB_PATH="./db";
    public static int RECORDS_PER_BLOCK = 90;
    public static long MAX_WHL_SIZE = 4000000; // ~ 4MB
    public static int MAX_KEY_LENGTH = 32;
    public static int MAX_VALUE_SIZE = 16000; // ~ 16KB
    public static int MAX_FILES_IN_BUCKET=5;
    public static int MAX_SIZE_FOR_MINOR_FILE = 5000000; // ~ 5MB
    public static long MAX_SIZE_FOR_BUCKET = MAX_SIZE_FOR_MINOR_FILE * 5; // ~ 25 MB
    public static long MAX_DB_SIZE = MAX_SIZE_FOR_BUCKET * 40; // ~ 1GB
    private static long mutationCounter = 0;
    public synchronized static long getUniq(){
        mutationCounter += 1;
        return mutationCounter;
    }
}
