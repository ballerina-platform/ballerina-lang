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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.file.utils.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Native function ballerina.compression:compress.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compression",
        functionName = "compress",
        args = {
                @Argument(name = "dirPath", type = TypeKind.STRUCT, structType = "Path",
                        structPackage = "ballerina.file"),
                @Argument(name = "destDir", type = TypeKind.STRUCT, structType = "Path",
                        structPackage = "ballerina.file")
        },
        returnType = {@ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class Compress extends BlockingNativeCallableUnit {

    /**
     * File path defined in ballerina.compression
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * File path of the destination directory defined in ballerina.compression.
     */
    private static final int DEST_PATH_FIELD_INDEX = 1;

    /**
     * Compresses a given folder or file.
     *
     * @param dirPath directory path to be compressed
     * @param destDir destination path to place the compressed file
     * @throws IOException exception if an error occurrs when compressing
     */
    private static void compress(Path dirPath, Path destDir) throws IOException {
        compressFiles(dirPath, new FileOutputStream(destDir.toFile()));
    }

    /**
     * Add file inside the src directory to the ZipOutputStream.
     *
     * @param zos      ZipOutputStream
     * @param filePath file path of each file inside the driectory
     * @throws IOException exception if an error occurrs when compressing
     */
    private static void addEntry(ZipOutputStream zos, Path filePath, String fileStr) throws IOException {
        ZipEntry ze = new ZipEntry(fileStr);
        zos.putNextEntry(ze);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    /**
     * Compresses files.
     *
     * @param outputStream outputstream
     * @return outputstream of the compressed file
     * @throws IOException exception if an error occurrs when compressing
     */
    static OutputStream compressFiles(Path dir, OutputStream outputStream) throws IOException {
        Stream<Path> list = Files.walk(dir);
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        list.forEach(p -> {
            StringJoiner joiner = new StringJoiner("/");
            for (Path path : dir.relativize(p)) {
                joiner.add(path.toString());
            }
            if (Files.isRegularFile(p)) {
                try {
                    addEntry(zos, p, joiner.toString());
                } catch (IOException e) {
                    throw new BLangRuntimeException("Error occurred when compressing");
                }
            }
        });
        zos.close();
        return outputStream;
    }

    @Override
    public void execute(Context context) {
        BStruct srcPathStruct = (BStruct) context.getRefArgument(SRC_PATH_FIELD_INDEX);
        Path srcPath = (Path) srcPathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        BStruct destPathStruct = (BStruct) context.getRefArgument(DEST_PATH_FIELD_INDEX);
        Path destPath = (Path) destPathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        if (!srcPath.toFile().exists()) {
            context.setReturnValues(CompressionUtils.createCompressionError(context, "Path of the folder to be " +
                    "compressed is not available"));
        } else {
            try {
                compress(srcPath, destPath);
                context.setReturnValues();
            } catch (IOException | BLangRuntimeException e) {
                context.setReturnValues(CompressionUtils.createCompressionError(context,
                                                                                "Error occurred when compressing "
                                                                                        + e.getMessage()));
            }
        }
    }
}
