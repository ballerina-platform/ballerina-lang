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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Native function ballerina.compression:ZipToBytes.
 *
 * @since 0.964
 */
@BallerinaFunction(
        packageName = "ballerina.compression",
        functionName = "zipToBytes",
        args = {@Argument(name = "dirPath", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BLOB)},
        isPublic = true
)
public class ZipToBytes extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(ZipToBytes.class);
    /**
     * File path defined in ballerina.compression.
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * Default buffer size.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Zip file/folder to bytes.
     *
     * @param fileToZip               file/folder to be zipped
     * @param excludeContainingFolder excludes the containing folder if true, else it does not
     * @return bytes of the zipped file/fodler
     */
    private byte[] zipToByte(String fileToZip, boolean excludeContainingFolder) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);
        Path srcFile = Paths.get(fileToZip);
        if (excludeContainingFolder && Files.isDirectory(srcFile)) {
            for (String fileName : srcFile.toFile().list()) {
                try {
                    addToZip("", Paths.get(fileToZip).resolve(fileName).toString(), zipOut);
                } catch (IOException e) {
                    log.error("Error occured when adding files inside the folder to be zipped " + e.getMessage());
                }
            }
        } else {
            try {
                addToZip("", fileToZip, zipOut);
            } catch (IOException e) {
                log.error("Error occured when adding files to be zipped " + e.getMessage());
            }
        }

        try {
            zipOut.flush();
            zipOut.close();
        } catch (IOException e) {
            log.error("Error occured when flushing/closing the ZipOutputStream");
        }
        return baos.toByteArray();
    }

    /**
     * Add file to zip.
     *
     * @param path    path of the file inside the folder
     * @param srcFile source folder path
     * @param zipOut  ZipOutput stream
     * @throws IOException exception thrown when handling files
     */
    private void addToZip(String path, String srcFile, ZipOutputStream zipOut) throws IOException {
        Path file = Paths.get(srcFile);
        String filePath = "".equals(path) ? file.getFileName().toString() : Paths.get(path).resolve(file.getFileName())
                .toString();
        if (Files.isDirectory(file)) {
            if (file.toFile().list() != null) {
                for (String fileName : file.toFile().list()) {
                    addToZip(filePath, Paths.get(srcFile).resolve(fileName).toString(), zipOut);
                }
            }
        } else {
            zipOut.putNextEntry(new ZipEntry(filePath));
            FileInputStream in = new FileInputStream(srcFile);

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
        }
    }

    @Override
    public BValue[] execute(Context context) {
        BBlob readByteBlob;
        String dirPath = getStringArgument(context, SRC_PATH_FIELD_INDEX);
        byte[] compressedBytes = zipToByte(dirPath, true);
        readByteBlob = new BBlob(compressedBytes);
        return getBValues(readByteBlob);
    }
}
