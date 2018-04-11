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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Native function ballerina.compression:zipFile.
 *
 * @since 0.962.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compression",
        functionName = "zipFile",
        args = {@Argument(name = "dirPath", type = TypeKind.STRING),
                @Argument(name = "destDir", type = TypeKind.STRING)},
        isPublic = true
)
public class ZipFile extends BlockingNativeCallableUnit {

    /**
     * File path defined in ballerina.compression
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * File path of the destination directory defined in ballerina.compression.
     */
    private static final int DEST_PATH_FIELD_INDEX = 1;

    /**
     * Compresses a given folder/file.
     *
     * @param dirPath directory path to be compressed
     * @param destDir destination path to place the compressed file
     */
    private static void compress(String dirPath, String destDir) {
        try {
            zipFiles(dirPath, new FileOutputStream(destDir));
        } catch (FileNotFoundException e) {
            throw new BLangRuntimeException("File to compress not found" + dirPath);
        }
    }

    /**
     * Add file inside the src directory to the ZipOutputStream.
     *
     * @param zos      ZipOutputStream
     * @param filePath file path of each file inside the driectory
     */
    static void addEntry(ZipOutputStream zos, String filePath) throws IOException {
        ZipEntry ze = new ZipEntry(filePath);
        zos.putNextEntry(ze);
        Files.copy(Paths.get(filePath), zos);
        zos.closeEntry();
    }

    /**
     * Zip files.
     *
     * @param dirPath      path of the directory to be zipped
     * @param outputStream outputstream object
     * @return outputstream
     */
    static OutputStream zipFiles(String dirPath, OutputStream outputStream) {
        try {
            Stream<Path> list = Files.list(Paths.get(dirPath));
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            try (ZipOutputStream finalZos = zos) {
                list.forEach(p -> {
                    try {
                        ZipFile.addEntry(finalZos, p.toString());
                    } catch (IOException e) {
                        throw new BLangRuntimeException("Error occurred when compressing");
                    }
                });
            }
            zos.close();
        } catch (IOException e) {
            throw new BLangRuntimeException("Error occurred when completing the compression process");
        }
        return outputStream;
    }

    @Override
    public void execute(Context context) {
        String dirPath = context.getStringArgument(SRC_PATH_FIELD_INDEX);
        String destDir = context.getStringArgument(DEST_PATH_FIELD_INDEX);
        compress(dirPath, destDir);
    }
}
