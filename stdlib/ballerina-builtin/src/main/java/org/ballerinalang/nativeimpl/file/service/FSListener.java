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

package org.ballerinalang.nativeimpl.file.service;

import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemEvent;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemListener;

import java.util.Map;

/**
 * File System connector listener for Ballerina.
 */
public class FSListener implements LocalFileSystemListener {

    private static final Logger log = LoggerFactory.getLogger(FSListener.class);

    private Map<String, Resource> resourceRegistry;
    private StructInfo structInfo;

    public FSListener(Map<String, Resource> resourceRegistry, StructInfo structInfo) {
        this.resourceRegistry = resourceRegistry;
        this.structInfo = structInfo;
    }

    @Override
    public void onMessage(LocalFileSystemEvent fileEvent) {
        BValue[] parameters = getSignatureParameters(fileEvent);
        Resource resource = getResource(fileEvent.getEvent());
        if (resource != null) {
            Executor.submit(resource, new DirectoryListenerCallback(), null, null, parameters);
        } else {
            log.warn("FileEvent received for unregistered resource: [" + fileEvent.getEvent() + "] " + fileEvent
                    .getFileName());
        }
    }

    private BValue[] getSignatureParameters(LocalFileSystemEvent fileEvent) {
        BStruct eventStruct = new BStruct(this.structInfo.getType());
        eventStruct.setStringField(0, fileEvent.getFileName());
        eventStruct.setStringField(1, fileEvent.getEvent());
        return new BValue[] { eventStruct };
    }

    private Resource getResource(String event) {
        return resourceRegistry.get(event);
    }
}
