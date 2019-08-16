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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.system.utils.SystemConstants;
import org.ballerinalang.stdlib.system.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extern function ballerina.system:createFile.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = SystemConstants.ORG_NAME,
        packageName = SystemConstants.PACKAGE_NAME,
        functionName = "createFile",
        isPublic = true
)
public class CreateFile {

    private static final Logger log = LoggerFactory.getLogger(CreateFile.class);

    public static Object createFile(Strand strand, String path) {
        try {
            Path filepath = Files.createFile(Paths.get(path));
            return filepath.toAbsolutePath().toString();
        } catch (FileAlreadyExistsException e) {
            String msg = "File already exists. Failed to create the file: " + path;
            log.error(msg, e);
            return SystemUtils.getBallerinaError(SystemConstants.INVALID_OPERATION_ERROR, msg);
        } catch (SecurityException e) {
            String msg = "Permission denied. Failed to create the file: " + path;
            log.error(msg, e);
            return SystemUtils.getBallerinaError(SystemConstants.PERMISSION_ERROR, msg);
        } catch (NoSuchFileException e) {
            String msg = "The file does not exist in path " + path;
            return SystemUtils.getBallerinaError(SystemConstants.FILE_SYSTEM_ERROR, msg);
        } catch (IOException e) {
            String msg = "IO error occurred while creating the file " + path;
            log.error(msg, e);
            return SystemUtils.getBallerinaError(SystemConstants.FILE_SYSTEM_ERROR, msg);
        } catch (Exception e) {
            String msg = "Error occurred while creating the file " + path;
            log.error(msg, e);
            return SystemUtils.getBallerinaError(SystemConstants.FILE_SYSTEM_ERROR, msg);
        }
    }
}
