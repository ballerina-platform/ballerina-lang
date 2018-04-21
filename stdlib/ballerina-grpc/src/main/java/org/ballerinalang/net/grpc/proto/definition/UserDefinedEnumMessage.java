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
package org.ballerinalang.net.grpc.proto.definition;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * User Defined Message Definition Builder.
 *
 * @since 1.0.0
 */
public class UserDefinedEnumMessage extends Message {
    List<EnumField> fieldList = new ArrayList<>();
    private DescriptorProtos.EnumDescriptorProto descriptorProto;
    
    public DescriptorProtos.EnumDescriptorProto getDescriptorProto() {
        return descriptorProto;
    }

    
    public MessageKind getMessageKind() {
        return MessageKind.USER_DEFINED;
    }

    private UserDefinedEnumMessage(DescriptorProtos.EnumDescriptorProto descriptorProto) {
        this.descriptorProto = descriptorProto;
        this.messageName = descriptorProto.getName();
    }

    public static UserDefinedEnumMessage.Builder newBuilder(String messageType) throws
            GrpcServerException {
        if (messageType == null) {
            throw new GrpcServerException("Error while initializing the builder, message type cannot be null");
        }
        // message type is coming as <package_name>:<name>.
        String name;
        if (messageType.contains(":")) {
            name = messageType.replace(':', '.');
        } else {
            name = messageType;
        }
        return new UserDefinedEnumMessage.Builder(name);
    }

    public String getMessageDefinition() {
        StringBuilder msgDefinition = new StringBuilder();
        msgDefinition.append("enum ").append(messageName).append(" {\n");

        for (EnumField field : fieldList) {
            msgDefinition.append("\t").append(field.getFieldDefinition());
        }
        msgDefinition.append("}\n");
        return msgDefinition.toString();
    }

    /**
     * MessageDefinition.Builder
     */
    public static class Builder {
        private DescriptorProtos.EnumDescriptorProto.Builder messageDescriptorBuilder;
        private List<EnumField> fieldList = new ArrayList<>();
    
        public UserDefinedEnumMessage build() {
            UserDefinedEnumMessage message = new UserDefinedEnumMessage(messageDescriptorBuilder.build());
            message.fieldList = fieldList;
            return message;
        }

        private Builder(String messageName) {
            messageDescriptorBuilder = DescriptorProtos.EnumDescriptorProto.newBuilder();
            messageDescriptorBuilder.setName(messageName);
        }

        public void addFieldDefinition(EnumField fieldDefinition) {
            fieldList.add(fieldDefinition);
            messageDescriptorBuilder.addValue(fieldDefinition.getFieldDescriptorProto());
        }
    }
}
