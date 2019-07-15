/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.nativeimpl.bir;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.ballerinalang.util.BLangConstants.BLANG_BIR_PACKAGE_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.USER_REPO_BIR_DIRNAME;

/**
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "bir",
        functionName = "decompressSingleFileToBlob",
        args = {
                @Argument(name = "libPath", type = STRING),
                @Argument(name = "pkgName", type = STRING),
        },
        returnType = {
                @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE)
        }
)
public class DecompressBIR extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String libPath = context.getStringArgument(0);
        String pkgName = context.getStringArgument(1);
        BValueArray binaryArray = decompressBIR(pkgName, libPath);
        context.setReturnValues(binaryArray);
    }

    private BValueArray decompressBIR(String pkgName, String libPath) {
        try (InputStream in = getCompiledBIRBinary(pkgName, libPath);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            // Create binary array from the Serialized the BIR model.
            return new BValueArray(byteArrayOutputStream.toByteArray());
            // Set the return value
        } catch (IOException e) {
            throw new BLangRuntimeException("error decompressing executable " + libPath + File.separator + pkgName);
        }
    }

    private InputStream getCompiledBIRBinary(String pkgName, String libPath) throws IOException {
        Path path = Paths.get(libPath);
        Path resolve = path.resolve(pkgName + ".zip");
        ZipFile zipFile = new ZipFile(resolve.toFile());
        ZipEntry zipEntry =
                zipFile.getEntry(USER_REPO_BIR_DIRNAME + "/" + pkgName + BLANG_BIR_PACKAGE_FILE_SUFFIX);
        return new BufferedInputStream(zipFile.getInputStream(zipEntry));
    }
}
