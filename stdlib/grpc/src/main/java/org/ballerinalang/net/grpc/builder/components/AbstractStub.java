/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds the abstract features of the stubs which are common to service and client stubs.
 */
public abstract class AbstractStub {
    private Map<String, Message> messageMap = new HashMap<>();
    private List<EnumMessage> enumList = new ArrayList<>();
    private String rootDescriptor;
    private Set<Descriptor> descriptors = new TreeSet<>((descriptor1, descriptor2) -> {
        if (descriptor1.getKey().equalsIgnoreCase(descriptor2.getKey())) {
            return 0;
        }
        return 1;
    });

    public void addMessage(Message message) {
        messageMap.put(message.getMessageName(), message);
    }

    public boolean isMessageExists(String messageName) {
        if (messageName == null) {
            return false;
        }
        return messageMap.containsKey(messageName);
    }

    public Map<String, Message> getMessageMap() {
        return messageMap;
    }

    public void addEnumMessage(EnumMessage message) {
        enumList.add(message);
    }

    public List<EnumMessage> getEnumList() {
        return enumList;
    }

    public String getRootDescriptor() {
        return rootDescriptor;
    }

    public void setRootDescriptor(String rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }

    public void addDescriptor(Descriptor descriptor) {
        descriptors.add(descriptor);
    }

    public void setDescriptors(Set<Descriptor> descriptors) {
        this.descriptors = descriptors;
    }

    public Set<Descriptor> getDescriptors() {
        return descriptors;
    }

    public void setMessageList(List<Message> messageList) {
        Map<String, Message> messageMap = new HashMap<>();
        for (Message messageInList : messageList){
            messageMap.put(messageInList.getMessageName(), messageInList);
        }
        this.messageMap = messageMap;
    }

    public void setEnumList(List<EnumMessage> enumList) {
        this.enumList = enumList;
    }
}
