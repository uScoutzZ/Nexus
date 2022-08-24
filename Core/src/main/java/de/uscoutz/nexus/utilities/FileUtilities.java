package de.uscoutz.nexus.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileUtilities {

    public static void copyFolder(File sourceFolder, File destinationFolder) {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }

            String files[] = sourceFolder.list();

            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                copyFolder(srcFile, destFile);
            }
        } else {
            try {
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFolder(Path path) {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteFolder(entry);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if(path.toFile().exists()) {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
