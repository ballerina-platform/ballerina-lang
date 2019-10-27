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
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemServerConnector;
import org.wso2.transport.localfilesystem.server.exception.LocalFileSystemServerConnectorException;

/**
 * Start server connector.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "file",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = "ballerina/file"),
        isPublic = true
)
public class Start {

    public static Object start(Strand strand, ObjectValue listener) {
        LocalFileSystemServerConnector serverConnector = (LocalFileSystemServerConnector) listener
                .getNativeData(DirectoryListenerConstants.FS_SERVER_CONNECTOR);
        try {
            serverConnector.start();
        } catch (LocalFileSystemServerConnectorException e) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e.getMessage());
        }
        return null;
    }
}
