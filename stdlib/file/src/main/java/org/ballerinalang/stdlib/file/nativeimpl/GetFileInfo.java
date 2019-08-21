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

package org.ballerinalang.stdlib.file.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Extern function ballerina.file:getFileInfo.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = FileConstants.ORG_NAME,
        packageName = FileConstants.PACKAGE_NAME,
        functionName = "getFileInfo",
        isPublic = true
)
public class GetFileInfo {

    private static final Logger log = LoggerFactory.getLogger(GetFileInfo.class);

    public static Object getFileInfo(Strand strand, String path) {
        File inputFile = Paths.get(path).toAbsolutePath().toFile();
        if (!inputFile.exists()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + path);
        }
        try {
            return FileUtils.getFileInfo(inputFile);
        } catch (IOException e) {
            log.error("IO error while creating the file " + path, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e);
        }
    }
}
