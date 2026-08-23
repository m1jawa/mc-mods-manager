package io.github.m1jawa.mcmodsmanager.file;

import io.github.m1jawa.mcmodsmanager.cli.InfoManager;
import io.github.m1jawa.mcmodsmanager.model.InfoType;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class CacheManager {
    private CacheManager() {}

    private static final String APP_FOLDER_NAME = "mcmm";
    private static final String CACHE_FILE = "versions_cache.ser";

    public static void saveCache(HashSet<String> versionSet) throws IOException{
        try (FileOutputStream fos = new FileOutputStream(getCacheFilePath().toString())) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(versionSet);
        } catch (IOException e) {
            throw new IOException("Cant write cache file: " + e.getMessage());
        }
    }


    public static HashSet<String> loadCache() {
        File file = new File(getCacheFilePath().toString());
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (HashSet<String>) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static Path getCacheFilePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        Path appFolder;

        if (os.contains("win")){
            //using %AppData% for windows
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                appFolder = Paths.get(appData, APP_FOLDER_NAME);
            } else {
                appFolder = Paths.get(userHome, "AppData", "Roaming", APP_FOLDER_NAME);
            }
        } else { // implies Linux
            String cacheHome = System.getenv("XDG_CACHE_HOME");

            if (cacheHome == null || cacheHome.isEmpty()) {
                appFolder = Paths.get(userHome, ".cache", APP_FOLDER_NAME);
            } else {
                appFolder = Paths.get(cacheHome, APP_FOLDER_NAME);
            }
        }

        File directory = appFolder.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return appFolder.resolve(CACHE_FILE);
    }
}
