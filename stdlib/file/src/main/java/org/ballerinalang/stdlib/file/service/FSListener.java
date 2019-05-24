/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemEvent;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemListener;

import java.util.Map;

import static org.ballerinalang.stdlib.file.service.DirectoryListenerConstants.FILE_SYSTEM_EVENT;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_EVENT_NAME;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_EVENT_OPERATION;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_PACKAGE;

/**
 * File System connector listener for Ballerina.
 */
public class FSListener implements LocalFileSystemListener {

    private static final Logger log = LoggerFactory.getLogger(FSListener.class);
    private ObjectValue service = null;
    private Map<String, AttachedFunction> attachedFunctionRegistry;

    private Map<String, Resource> resourceRegistry;
    private StructureTypeInfo structInfo = null;

    public FSListener(Map<String, Resource> resourceRegistry, StructureTypeInfo structInfo) {
        this.resourceRegistry = resourceRegistry;
        this.structInfo = structInfo;
    }

    public FSListener(ObjectValue service, Map<String, AttachedFunction> resourceRegistry) {
        this.service = service;
        this.attachedFunctionRegistry = resourceRegistry;
    }

    @Override
    public void onMessage(LocalFileSystemEvent fileEvent) {
        //TODO remove following condition once bvm values are removed. This is temp fix to handle both types
        if (this.structInfo != null) {
            BValue[] parameters = getSignatureParameters(fileEvent);
            Resource resource = getResource(fileEvent.getEvent());
            if (resource != null) {
                Executor.submit(resource, new DirectoryListenerCallback(), null, null, parameters);
            } else {
                log.warn("FileEvent received for unregistered resource: [" + fileEvent.getEvent() + "] " + fileEvent
                        .getFileName());
            }
        } else {
            Object[] parameters = getJvmSignatureParameters(fileEvent);
            AttachedFunction resource = getAttachedFunction(fileEvent.getEvent());
            if (resource != null) {
                org.ballerinalang.jvm.values.connector.Executor.submit(service, resource.getName(),
                                                                       new DirectoryCallback(), null,
                                                                       parameters);
            } else {
                log.warn("FileEvent received for unregistered resource: [" + fileEvent.getEvent() + "] " + fileEvent
                        .getFileName());
            }
        }
    }

    private BValue[] getSignatureParameters(LocalFileSystemEvent fileEvent) {
        BMap<String, BValue> eventStruct = new BMap<>(this.structInfo.getType());
        eventStruct.put(FILE_EVENT_NAME, new BString(fileEvent.getFileName()));
        eventStruct.put(FILE_EVENT_OPERATION, new BString(fileEvent.getEvent()));
        return new BValue[] { eventStruct };
    }

    private Resource getResource(String event) {
        return resourceRegistry.get(event);
    }

    private Object[] getJvmSignatureParameters(LocalFileSystemEvent fileEvent) {
        MapValue<String, Object> eventStruct = BallerinaValues.createRecordValue(FILE_PACKAGE, FILE_SYSTEM_EVENT);
        eventStruct.put(FILE_EVENT_NAME, fileEvent.getFileName());
        eventStruct.put(FILE_EVENT_OPERATION, fileEvent.getEvent());
        return new Object[] { eventStruct };
    }

    private AttachedFunction getAttachedFunction(String event) {
        return attachedFunctionRegistry.get(event);
    }
}
