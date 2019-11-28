package net.icircuit.bucketdb;

import java.nio.file.Path;
import java.util.Comparator;

/**
 * Comparator for different files of the DB such as Manifest,sorted files etc
 * All the file name format is prefix-timestamp-uniquenumber
 *
 */
public class FileNameComparator implements Comparator<Path> {
    @Override
    public int compare(Path o1, Path o2) {
         //extract timestamps
        String[] o1Tokens = o1.getFileName().toString().split("-");
        long t1 = Long.parseLong(o1Tokens[1]);
        int u1 = Integer.parseInt(o1Tokens[2]);
        String[] o2Tokens = o2.getFileName().toString().split("-");
        long t2 = Long.parseLong(o2Tokens[1]);
        int u2 = Integer.parseInt(o2Tokens[2]);
        return Long.compare(t1+u1,t2+u2);
    }
}
