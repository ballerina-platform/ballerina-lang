package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

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
        filesToBeZipped.forEach((path) -> addToZip(path, "src", finalZipOut));
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
     * @param srcFilePath path of the folder/file to be zipped
     * @param includedDir directory to be included
     * @param zipOut      ZipOutputStream object
     */
    private static void addToZip(Path srcFilePath, String includedDir, ZipOutputStream zipOut) {
        String filePath = srcFilePath.getFileName().toString();
        if (!includedDir.isEmpty()) {
            filePath = Paths.get(includedDir).resolve(filePath).toString();
        }
        try {
            zipOut.putNextEntry(new ZipEntry(filePath));
            FileInputStream in = new FileInputStream(srcFilePath.toString());

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
        } catch (IOException e) {
            outStream.println("Error occurred when writing the " + filePath + " to the zipped folder");
        }
    }

    /**
     * Generates the balo/zip of the package.
     *
     * @param bLangPackage bLangPackage node
     * @param pkgPath      full package path
     * @param paths        paths of bal files inside the package
     */
    static void generateBalo(BLangPackage bLangPackage, String pkgPath, Stream<Path> paths) {
        PackageID packageID = bLangPackage.packageID;
        Path destPath = Paths.get(pkgPath).resolve(".ballerina").resolve("repo")
                .resolve(packageID.getOrgName().getValue()).resolve(packageID.getName().getValue())
                .resolve(packageID.getPackageVersion().getValue());
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                outStream.println("Error when occured when creating directories in " +
                        "./ballerina/repos/ to save the compressed file");
            }
        }
        String zippedPackageExt = packageID.getName() + ".zip";
        String baloDirPath = Paths.get(destPath.toString()).resolve(zippedPackageExt).toString();
        zipFile(paths, baloDirPath);
    }
}
