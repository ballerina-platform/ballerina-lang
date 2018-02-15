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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Native function ballerina.compression:zipFile.
 *
 * @since 0.962.0
 */
@BallerinaFunction(
        packageName = "ballerina.compression",
        functionName = "zipFile",
        args = {@Argument(name = "dirPath", type = TypeKind.STRING),
                @Argument(name = "destDir", type = TypeKind.STRING)},
        isPublic = true
)
public class ZipFile extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(UnzipBytes.class);

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
        ZipOutputStream zos = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(dirPath);
            List<String> filesListInDir = CompressionUtils.populateFilesList(dir);

            try {
                fos = new FileOutputStream(destDir);
            } catch (FileNotFoundException e) {
                log.debug("File with the specified pathname does not exist", e);
                log.error("File with the specified pathname does not exist : " + destDir);
            }
            if (fos != null) {
                zos = new ZipOutputStream(fos);
                for (String filePath : filesListInDir) {
                    log.debug("Zipping " + filePath);
                    //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                    ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length() + 1,
                            filePath.length()));
                    zos.putNextEntry(ze);
                    //read the file and write to ZipOutputStream
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(filePath);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.debug("Failed or interrupted I/O operation has occured", e);
            log.error("Failed or interrupted I/O operation has occured");
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.debug("Failed or interrupted I/O operation has occured when closing the ZipOutputStream", e);
                log.error("Failed or interrupted I/O operation has occured when closing the ZipOutputStream");
            }
        }
    }

    @Override
    public BValue[] execute(Context context) {
        String dirPath = getStringArgument(context, SRC_PATH_FIELD_INDEX);
        String destDir = getStringArgument(context, DEST_PATH_FIELD_INDEX);
        compress(dirPath, destDir);
        return VOID_RETURN;
    }
}
