/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Client Stub File definition bean class.
 *
 * @since 0.982.0
 */
public class StubFile {
    private String rootDescriptorKey;
    private Set<Descriptor> descriptors = new TreeSet<>((descriptor1, descriptor2) -> {
        if (descriptor1.getKey().equalsIgnoreCase(descriptor2.getKey())) {
            return 0;
        }
        return 1;
    });
    private List<Message> messageList = new ArrayList<>();
    private List<EnumMessage> enumList = new ArrayList<>();
    private List<ServiceStub> stubList = new ArrayList<>();
    private String fileName;
    
    public StubFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRootDescriptorKey() {
        return rootDescriptorKey;
    }

    public void setRootDescriptorKey(String rootDescriptorKey) {
        this.rootDescriptorKey = rootDescriptorKey;
    }

    public void addDescriptor(Descriptor descriptor) {
        descriptors.add(descriptor);
    }
    public Set<Descriptor> getDescriptors() {
        return descriptors;
    }

    public void addMessage(Message message) {
        messageList.add(message);
    }

    public boolean messageExists(String messageName) {
        if (messageName == null) {
            return false;
        }
        for (Message message : messageList) {
            if (messageName.equals(message.getMessageName())) {
                return true;
            }
        }
        return false;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void addEnumMessage(EnumMessage message) {
        enumList.add(message);
    }

    public List<EnumMessage> getEnumList() {
        return enumList;
    }

    public void addServiceStub(ServiceStub serviceStub) {
        stubList.add(serviceStub);
    }

    public List<ServiceStub> getStubList() {
        return stubList;
    }
    
}
