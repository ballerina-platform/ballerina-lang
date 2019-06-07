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
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.system.utils.SystemConstants;
import org.ballerinalang.stdlib.system.utils.SystemUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.ballerinalang.stdlib.system.utils.SystemUtils.getFileInfo;

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

        String inputPath = context.getStringArgument(0);
        File inputFile = Paths.get(inputPath).toAbsolutePath().toFile();

        if (!inputFile.exists()) {
            context.setReturnValues(SystemUtils.getBallerinaError("INVALID_OPERATION",
                    "File doesn't exist in path " + inputPath));
            return;
        }

        if (!inputFile.isDirectory()) {
            context.setReturnValues(SystemUtils.getBallerinaError("INVALID_OPERATION", "File in path " + inputPath +
                    " is not a directory"));
            return;
        }
        BMap[] results = null;
        try (Stream<Path> walk = Files.walk(inputFile.toPath())) {
            results = walk.map(x -> {
                try {
                    return getFileInfo(context, x.toFile());
                } catch (IOException e) {
                    throw new BallerinaException("Error while accessing file info", e);
                }
            }).toArray(BMap[]::new);
            context.setReturnValues(new BValueArray(results, BTypes.typeMap));
        } catch (IOException | BallerinaException ex) {
            context.setReturnValues(SystemUtils.getBallerinaError("OPERATION_FAILED", ex));
        } catch (SecurityException ex) {
            context.setReturnValues(SystemUtils.getBallerinaError("PERMISSION_ERROR", ex));
        }
    }
}
