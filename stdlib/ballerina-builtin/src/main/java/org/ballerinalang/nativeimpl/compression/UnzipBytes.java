/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.nativeimpl.compression;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Native function ballerina.compression:unzipBytes.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compression",
        functionName = "unzipBytes",
        args = {@Argument(name = "content", type = TypeKind.BLOB),
                @Argument(name = "destDir", type = TypeKind.STRING),
                @Argument(name = "folderToUnzip", type = TypeKind.STRING)},
        isPublic = true
)
public class UnzipBytes extends BlockingNativeCallableUnit {
    /**
     * File content as byte array defined.
     */
    private static final int SRC_AS_BYTEARRAY_FIELD_INDEX = 0;
    /**
     * File path of the destination directory.
     */
    private static final int DEST_PATH_FIELD_INDEX = 0;

    /**
     * Folder to unzip from the compressed bytes.
     */
    private static final int FOLDER_TO_UNZIP_INDEX = 1;

    /**
     * Decompress/unzip byte arrays/blob.
     *  @param inputStream file content as an inputstream
     * @param outputFolder           destination folder
     * @param folderToUnzip folder to unzip in the zipped file
     */
    static void decompress(InputStream inputStream, String outputFolder, String folderToUnzip) {
        ZipInputStream zin = null;
        try {
            Path outdir = Paths.get(outputFolder);
            zin = new ZipInputStream(inputStream);
            ZipEntry entry;
            String name, dir;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                if (!folderToUnzip.isEmpty() && name.startsWith(folderToUnzip)) {
                    int index = name.lastIndexOf("/") + 1;
                    name = name.substring(index);
                }
                    if (entry.isDirectory()) {
                        Files.createDirectories(outdir.resolve(name));
                        continue;
                    }
                    dir = getDirectoryPath(name);
                    if (dir != null) {
                        Files.createDirectories(outdir.resolve(dir));
                    }
                    extractFile(zin, outdir, name);
            }
        } catch (IOException e) {
            throw new BLangRuntimeException("I/O Exception when processing files");
        } finally {
            try {
                if (zin != null) {
                    zin.close();
                }
            } catch (IOException e) {
                throw new BLangRuntimeException("I/O Exception when closing the input stream " + e.getMessage());
            }
        }
    }

    /**
     * Extract files from the zipInputStream.
     *
     * @param in     zipInputStream object
     * @param outdir output directory file
     * @param name   name of the file
     */
    private static void extractFile(ZipInputStream in, Path outdir, String name) {
        try {
            Path resourcePath = Paths.get(outdir.toString()).resolve(name);
            Files.copy(in, resourcePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BLangRuntimeException("I/O Exception when closing the input stream " + outdir);
        }
    }

    /**
     * Create the directory name.
     *
     * @param name name of the directory
     * @return directory name
     */
    private static String getDirectoryPath(String name) {
        if (name != null) {
            int s = name.lastIndexOf("/");
            return s == -1 ? null : name.substring(0, s);
        }
        return null;
    }

    @Override
    public void execute(Context context) {
        byte[] content = context.getBlobArgument(SRC_AS_BYTEARRAY_FIELD_INDEX);
        InputStream inputStream = new ByteArrayInputStream(content);
        String destDir = context.getStringArgument(DEST_PATH_FIELD_INDEX);
        String folderToUnzip = context.getStringArgument(FOLDER_TO_UNZIP_INDEX);
        decompress(inputStream, destDir, folderToUnzip);
    }
}
