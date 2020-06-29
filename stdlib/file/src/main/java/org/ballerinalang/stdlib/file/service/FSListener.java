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

package org.ballerinalang.stdlib.file.service;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.StrandMetadata;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemEvent;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemListener;

import java.util.Map;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.FILE_SYSTEM_EVENT;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.MODULE_NAME;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.MODULE_VERSION;
import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.RESOURCE_NAME_ON_MESSAGE;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_EVENT_NAME;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_EVENT_OPERATION;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_PACKAGE_ID;

/**
 * File System connector listener for Ballerina.
 */
public class FSListener implements LocalFileSystemListener {

    private static final Logger log = LoggerFactory.getLogger(FSListener.class);
    private BRuntime runtime;
    private ObjectValue service;
    private Map<String, AttachedFunction> attachedFunctionRegistry;
    private static final StrandMetadata ON_MESSAGE_METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                                 MODULE_NAME, MODULE_VERSION,
                                                                                 RESOURCE_NAME_ON_MESSAGE);

    public FSListener(BRuntime runtime, ObjectValue service, Map<String, AttachedFunction> resourceRegistry) {
        this.runtime = runtime;
        this.service = service;
        this.attachedFunctionRegistry = resourceRegistry;
    }

    @Override
    public void onMessage(LocalFileSystemEvent fileEvent) {
        Object[] parameters = getJvmSignatureParameters(fileEvent);
        AttachedFunction resource = getAttachedFunction(fileEvent.getEvent());
        if (resource != null) {
            runtime.invokeMethodAsync(service, resource.getName(), null, ON_MESSAGE_METADATA, new DirectoryCallback(),
                                      parameters);
        } else {
            log.warn(String.format("FileEvent received for unregistered resource: [%s] %s", fileEvent.getEvent(),
                    fileEvent.getFileName()));
        }
    }

    private Object[] getJvmSignatureParameters(LocalFileSystemEvent fileEvent) {
        MapValue<BString, Object> eventStruct = BallerinaValues.createRecordValue(FILE_PACKAGE_ID, FILE_SYSTEM_EVENT);
        eventStruct.put(StringUtils.fromString(FILE_EVENT_NAME), StringUtils.fromString(fileEvent.getFileName()));
        eventStruct.put(StringUtils.fromString(FILE_EVENT_OPERATION), StringUtils.fromString(fileEvent.getEvent()));
        return new Object[] { eventStruct, true };
    }

    private AttachedFunction getAttachedFunction(String event) {
        return attachedFunctionRegistry.get(event);
    }
}
