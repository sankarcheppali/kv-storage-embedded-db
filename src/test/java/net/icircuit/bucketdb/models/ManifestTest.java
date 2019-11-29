package net.icircuit.bucketdb.models;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ManifestTest {
    public static Path dbPath = Paths.get("../db-manifest-test");


    @BeforeClass
    public static void setup() throws IOException {
        if(!dbPath.toFile().exists())
            Files.createDirectory(dbPath);
    }
    @Test
    public void getDBWriter() throws IOException {
        Manifest manifest = new Manifest(dbPath);
        DBWriter writer = manifest.getDBWriter();
        assertTrue("writer is not null",writer!=null);
    }

}