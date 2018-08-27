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

import com.google.protobuf.DescriptorProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * User Defined Message Definition Builder.
 *
 * @since 1.0.0
 */
public class Message {
    private List<Field> fieldList;
    private String messageName;

    private Message(String messageName, List<Field> fieldList) {
        this.messageName = messageName;
        this.fieldList = fieldList;
    }

    public static Message.Builder newBuilder(DescriptorProtos.DescriptorProto messageDescriptor) {
        return new Message.Builder(messageDescriptor);
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public String getMessageName() {
        return messageName;
    }

    /**
     * MessageDefinition.Builder.
     */
    public static class Builder {
        private DescriptorProtos.DescriptorProto messageDescriptor;

        public Message build() {
            List<Field> fieldList = new ArrayList<>();
            for (DescriptorProtos.FieldDescriptorProto fieldDescriptorProto : messageDescriptor.getFieldList()) {
               Field field = Field.newBuilder(fieldDescriptorProto).build();
               fieldList.add(field);
            }

            return new Message(messageDescriptor.getName(), fieldList);
        }

        private Builder(DescriptorProtos.DescriptorProto messageDescriptor) {
            this.messageDescriptor = messageDescriptor;
        }
    }
}
