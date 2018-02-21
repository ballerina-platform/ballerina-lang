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
package org.ballerinalang.net.grpc.definition;

import com.google.protobuf.DescriptorProtos.DescriptorProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Message Definition Builder.
 */
public class Message {
    private DescriptorProto descriptorProto;
    private List<Field> fieldList = new ArrayList<>();

    private Message(DescriptorProto descriptorProto) {
        this.descriptorProto = descriptorProto;
    }

    public static Message.Builder newBuilder(String messageName) {
        return new Message.Builder(messageName);
    }

    public DescriptorProto getDescriptorProto() {
        return descriptorProto;
    }

    public String getMessageDefinition() {
        StringBuilder msgDefinition = new StringBuilder();
        msgDefinition.append("message ").append(descriptorProto.getName()).append(" {\n");

        for (Field field : fieldList) {
            msgDefinition.append("\t").append(field.getFieldDefinition());
        }
        msgDefinition.append("}\n");
        return msgDefinition.toString();

    }

    /**
     * MessageDefinition.Builder
     */
    public static class Builder {
        private DescriptorProto.Builder messageDescriptorBuilder;
        private List<Field> fieldList = new ArrayList<>();

        public Message build() {
            Message message = new Message(messageDescriptorBuilder.build());
            message.fieldList = fieldList;
            return message;
        }

        private Builder(String methodName) {
            messageDescriptorBuilder = DescriptorProto.newBuilder();
            messageDescriptorBuilder.setName(methodName);
        }

        public Builder addMessageDefinition(Message messageDefinition) {
            messageDescriptorBuilder.addNestedType(messageDefinition.getDescriptorProto());
            return this;
        }

        public Builder addFieldDefinition(Field fieldDefinition) {
            fieldList.add(fieldDefinition);
            messageDescriptorBuilder.addField(fieldDefinition.getFieldDescriptorProto());
            return this;
        }
    }
}
