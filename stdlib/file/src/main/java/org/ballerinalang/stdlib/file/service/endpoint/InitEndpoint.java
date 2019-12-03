/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.file.service.endpoint;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.file.service.DirectoryListenerConstants;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initialize endpoints.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "file",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = "ballerina/file"),
        isPublic = true
)
public class InitEndpoint {

    public static Object initEndpoint(Strand strand, ObjectValue listener) {
        final String path = listener.getMapValue(DirectoryListenerConstants.SERVICE_ENDPOINT_CONFIG).
                getStringValue(DirectoryListenerConstants.ANNOTATION_PATH);
        if (path == null || path.isEmpty()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, "'path' field is empty");
        }
        final Path dirPath = Paths.get(path);
        if (Files.notExists(dirPath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, "Folder does not exist: " + path);
        }
        if (!Files.isDirectory(dirPath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, "Unable to find a directory: " + path);
        }
        return null;
    }
}
