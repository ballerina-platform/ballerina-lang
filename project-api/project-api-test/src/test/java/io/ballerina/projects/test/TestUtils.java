/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Contains utils to test the balo writer.
 *
 * @since 2.0.0
 */
public class TestUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());

    static void unzip(String fileZipPath, String destinationDirectory) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        // Get the zip file content.
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileZipPath));
        // Get the zipped file entry.
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            // Get the name.
            String fileName = zipEntry.getName();
            // Construct the output file.
            File outputFile = new File(destinationDirectory + File.separator + fileName);
            // If the zip entry is for a directory, we create the directory and continue with the next entry.
            if (zipEntry.isDirectory()) {
                outputFile.mkdir();
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }

            // Create all non-existing directories.
            new File(outputFile.getParent()).mkdirs();
            // Create a new file output stream.
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            // Write the content from zip input stream to the file output stream.
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            // Close the file output stream.
            fileOutputStream.close();
            // Continue with the next entry.
            zipEntry = zipInputStream.getNextEntry();
        }
        // Close zip input stream.
        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("cannot delete file:" + file.toPath(), e);
        }
    }

    static void resetPermissions(Path projectPath) {
        projectPath.toFile().setExecutable(true, true);
        projectPath.toFile().setWritable(true, true);
        projectPath.toFile().setReadable(true, true);
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }
}
