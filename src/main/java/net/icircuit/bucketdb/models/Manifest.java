package net.icircuit.bucketdb.models;

import javafx.collections.transformation.SortedList;
import javafx.util.Pair;
import net.icircuit.bucketdb.FileNameComparator;
import net.icircuit.bucketdb.models.proto.ManifestProto.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class Manifest {
    private String filePrefix="MANIFEST";
    private Path dbPathFolder;
    private Path manifestFile;
    public Manifest(Path dbPathFolder) throws IOException {
        this.dbPathFolder = dbPathFolder;
        //if db path is not present, create it
        if(!Files.exists(dbPathFolder)){
            Files.createDirectories(dbPathFolder);
        }
        List<Path> manifestFileList = Files.list(dbPathFolder)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().startsWith(filePrefix))
                .collect(Collectors.toList());
        //sort oldest to latest
        manifestFileList.sort(new FileNameComparator());
        //find a valid manifest file - based on crc
        Pair<Path,DBManifestFile> validManifestPair= readValidManifest(manifestFileList);
        if(validManifestPair != null){
            manifestFile = validManifestPair.getKey();
            DBManifest dbManifest = validManifestPair.getValue().getDbManifest();
            //TODO: load buckets and remove buckets not present in the manifest
        }else {
            manifestFile = createManifest(dbPathFolder);
        }
        //remove all invalid manifest files
        manifestFileList.stream().filter(path -> path!=manifestFile).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private Path createManifest(Path dbPathFolder) throws IOException {
        String fileName = filePrefix+"-"+new Date().getTime();
        Path path = Paths.get(dbPathFolder.toString(),fileName);
        Files.createFile(path);
        return path;
    }
    /**
     * this method should be called every time a bucket operation is performed
     * when new bucket is created or deleted
     */
    public void saveManifest() {
        //TODO: write current sorted file list to manifest
    }

    private Pair<Path, DBManifestFile> readValidManifest(List<Path> manifestFileList){
       List<Pair<Path,DBManifestFile>> dbManifestFileList =manifestFileList.stream().map(path -> {
            try {
                return new Pair<>(path,DBManifestFile.parseFrom(Files.newInputStream(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter((Objects::nonNull))
               .filter(pair -> isValidManifestFile(pair.getValue()))
               .collect(Collectors.toList());
       if(dbManifestFileList.size() == 0)return null;
       return dbManifestFileList.get(dbManifestFileList.size()-1);
    }

    private boolean isValidManifestFile(DBManifestFile dbManifestFile){
        CRC32 crcCaluculator = new CRC32();
        long actualCRC = dbManifestFile.getDbManifestCRC();
        crcCaluculator.update(dbManifestFile.getDbManifest().toByteArray());
        long expectedCRC = crcCaluculator.getValue();
        return expectedCRC == actualCRC;
    }

}
