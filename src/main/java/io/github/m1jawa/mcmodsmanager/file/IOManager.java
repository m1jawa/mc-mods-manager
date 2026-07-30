package io.github.m1jawa.mcmodsmanager.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOManager {
    private IOManager() {}

    public static void removeFile(Path path) throws IOException{
        Files.deleteIfExists(path);
    }
}
