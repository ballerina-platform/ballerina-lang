package org.wso2.ballerinalang.compiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip Utils needed to zip the packages.
 */
class ZipUtils {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static PrintStream outStream = System.err;

    /**
     * Zip all the bal files in the package.
     *
     * @param filesToBeZipped bal files to be zipped
     * @param zipFile         name of the zip file
     */
    private static void zipFile(Stream<Path> filesToBeZipped, String zipFile) {
        ZipOutputStream zipOut = null;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (FileNotFoundException e) {
            outStream.println("File to be zipped not found " + zipFile);
        }
        ZipOutputStream finalZipOut = zipOut;
        // Bal files
        filesToBeZipped.forEach((path) -> addToZip("", path.toString(), "src", finalZipOut));
        try {
            if (zipOut != null) {
                zipOut.close();
            }
        } catch (IOException e) {
            outStream.println("Error occurred when closing the ZipOutputStream");
        }
    }

    /**
     * Add file to the zipped folder.
     *
     * @param path        path of the folder/file to be zipped
     * @param srcFile     path of the folder/file to be zipped
     * @param includedDir directory to be included
     * @param zipOut      ZipOutputStream object
     */
    private static void addToZip(String path, String srcFile, String includedDir, ZipOutputStream zipOut) {
        Path file = Paths.get(srcFile);
        String filePath = "".equals(path) ? file.getFileName().toString() : Paths.get(path).resolve(file.getFileName())
                .toString();
        if (!includedDir.isEmpty()) {
            filePath = Paths.get(includedDir).resolve(filePath).toString();
        }
        if (Files.isDirectory(file)) {
            if (file.toFile() != null && file.toFile().list() != null) {
                for (String fileName : file.toFile().list()) {
                    addToZip(filePath, Paths.get(srcFile).resolve(fileName).toString(), "src", zipOut);
                }
            }
        } else {
            try {
                zipOut.putNextEntry(new ZipEntry(filePath));
                FileInputStream in = new FileInputStream(srcFile);

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, len);
                }
            } catch (IOException e) {
                outStream.println("Error occurred when writing the " + filePath + " to the zipped folder");
            }
        }
    }

    /**
     * Generates the balo/zip of the package.
     *
     * @param pkgPath full package path
     * @param pkgID   package id
     * @param paths   paths of bal files inside the package
     */
    static void generateBalo(String pkgPath, String pkgID, Stream<Path> paths) {
        Path destPath = Paths.get(pkgPath).resolve(".ballerina").resolve("repos").resolve("$current");
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                outStream.println("Error when occured when creating directories in " +
                        "./ballerina/repos/ to save the compressed file");
            }
        }
        String zippedPackageExt = pkgID + ".zip";
        String baloDirPath = Paths.get(destPath.toString()).resolve(zippedPackageExt).toString();
        zipFile(paths, baloDirPath);
    }
}
