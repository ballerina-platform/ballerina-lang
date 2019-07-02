/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.system.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.system.utils.SystemConstants;
import org.ballerinalang.stdlib.system.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Extern function ballerina.system:readDir.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = SystemConstants.ORG_NAME,
        packageName = SystemConstants.PACKAGE_NAME,
        functionName = "readDir",
        isPublic = true
)
public class ReadDir extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    private static BType fileInfoType;

    public static Object readDir(Strand strand, String path) {
        File inputFile = Paths.get(path).toAbsolutePath().toFile();

        if (!inputFile.exists()) {
            return SystemUtils.getBallerinaError("INVALID_OPERATION", "File doesn't exist in path " + path);
        }

        if (!inputFile.isDirectory()) {
            return SystemUtils.getBallerinaError("INVALID_OPERATION", "File in path " + path + " is not a directory");
        }
        ObjectValue[] results;
        try (Stream<Path> walk = Files.walk(inputFile.toPath())) {
            results = walk.map(x -> {
                try {
                    ObjectValue objectValue = SystemUtils.getFileInfo(x.toFile());
                    fileInfoType = objectValue.getType();
                    return objectValue;
                } catch (IOException e) {
                    throw new BallerinaException("Error while accessing file info", e);
                }
            }).toArray(ObjectValue[]::new);
            return new ArrayValue(results, new BArrayType(fileInfoType));
        } catch (IOException | BallerinaException ex) {
            return SystemUtils.getBallerinaError("OPERATION_FAILED", ex);
        } catch (SecurityException ex) {
            return SystemUtils.getBallerinaError("PERMISSION_ERROR", ex);
        }
    }
}
