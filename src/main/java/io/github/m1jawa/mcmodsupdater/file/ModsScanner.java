package io.github.m1jawa.mcmodsupdater.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ModsScanner {
    private ModsScanner() {}

    public static List<Path> scanDirectory(Path path) throws IOException{

        try (Stream<Path> stream = Files.list(path)) {
            return stream.filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".jar"))
                .toList();
        }
    }
}
