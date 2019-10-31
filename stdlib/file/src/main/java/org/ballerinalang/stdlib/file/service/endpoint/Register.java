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
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.file.service.DirectoryListenerConstants;
import org.ballerinalang.stdlib.file.service.FSListener;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemConnectorFactory;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemServerConnector;
import org.wso2.transport.localfilesystem.server.connector.contractimpl.LocalFileSystemConnectorFactoryImpl;
import org.wso2.transport.localfilesystem.server.exception.LocalFileSystemServerConnectorException;
import org.wso2.transport.localfilesystem.server.util.Constants;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.FILE_SYSTEM_EVENT;

/**
 * Register file listener service.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "file",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = "ballerina/file"),
        isPublic = true
)
public class Register {

    public static Object register(Strand strand, ObjectValue listener, ObjectValue service, Object name) {
        MapValueImpl serviceEndpointConfig = listener.getMapValue(DirectoryListenerConstants.SERVICE_ENDPOINT_CONFIG);
        try {
            final Map<String, AttachedFunction> resourceRegistry = getResourceRegistry(service);
            final String events = String.join(",", resourceRegistry.keySet());
            final Map<String, String> paramMap = getParamMap(serviceEndpointConfig, events);
            LocalFileSystemConnectorFactory connectorFactory = new LocalFileSystemConnectorFactoryImpl();
            LocalFileSystemServerConnector serverConnector = connectorFactory
                    .createServerConnector(service.getType().getName(), paramMap,
                            new FSListener(strand.scheduler, service, resourceRegistry));
            listener.addNativeData(DirectoryListenerConstants.FS_SERVER_CONNECTOR, serverConnector);
        } catch (LocalFileSystemServerConnectorException e) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR,
                    "Unable to initialize server connector: " + e.getMessage());
        }
        return null;
    }

    private static Map<String, AttachedFunction> getResourceRegistry(ObjectValue service) {
        Map<String, AttachedFunction> registry = new HashMap<>(5);
        final AttachedFunction[] attachedFunctions = service.getType().getAttachedFunctions();
        for (AttachedFunction resource : attachedFunctions) {
            switch (resource.getName()) {
                case DirectoryListenerConstants.RESOURCE_NAME_ON_CREATE:
                    registry.put(DirectoryListenerConstants.EVENT_CREATE, resource);
                    break;
                case DirectoryListenerConstants.RESOURCE_NAME_ON_DELETE:
                    registry.put(DirectoryListenerConstants.EVENT_DELETE, resource);
                    break;
                case DirectoryListenerConstants.RESOURCE_NAME_ON_MODIFY:
                    registry.put(DirectoryListenerConstants.EVENT_MODIFY, resource);
                    break;
                default:
                    // Do nothing.
            }
        }
        if (registry.size() == 0) {
            String msg = "At least a single resource required from following: "
                    + DirectoryListenerConstants.RESOURCE_NAME_ON_CREATE + " ,"
                    + DirectoryListenerConstants.RESOURCE_NAME_ON_DELETE + " ,"
                    + DirectoryListenerConstants.RESOURCE_NAME_ON_MODIFY + ". " + "Parameter should be of type - "
                    + "file:" + FILE_SYSTEM_EVENT;
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException(msg);
        }
        return registry;
    }

    private static Map<String, String> getParamMap(MapValueImpl serviceEndpointConfig, String events) {
        final String path = serviceEndpointConfig.getStringValue(DirectoryListenerConstants.ANNOTATION_PATH);
        final boolean recursive = serviceEndpointConfig
                .getBooleanValue(DirectoryListenerConstants.ANNOTATION_DIRECTORY_RECURSIVE);
        Map<String, String> paramMap = new HashMap<>(3);
        if (path != null && !path.isEmpty()) {
            paramMap.put(Constants.FILE_URI, path);
        }
        paramMap.put(Constants.DIRECTORY_WATCH_EVENTS, events);
        paramMap.put(Constants.DIRECTORY_WATCH_RECURSIVE, String.valueOf(recursive));
        return paramMap;
    }
}
