package net.icircuit.bucketdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
    public static void deleteFolder(String dbPath) throws IOException {
        if(Paths.get(dbPath).toFile().exists()){
            try{
                //delete files
                Files.walk(Paths.get(dbPath)).filter(Files::isRegularFile).forEach(path -> path.toFile().delete());
                //delete directories
                Files.walk(Paths.get(dbPath)).filter(Files::isDirectory).forEach(path -> path.toFile().delete());
                //delete root
                Paths.get(dbPath).toFile().delete();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public static void deleteFolder(Path dbPath) throws IOException {
        if(dbPath.toFile().exists()){
            try{
                //delete files
                Files.walk(dbPath).filter(Files::isRegularFile).forEach(path -> path.toFile().delete());
                //delete directories
                Files.walk(dbPath).filter(Files::isDirectory).forEach(path -> path.toFile().delete());
                //delete root
                dbPath.toFile().delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
