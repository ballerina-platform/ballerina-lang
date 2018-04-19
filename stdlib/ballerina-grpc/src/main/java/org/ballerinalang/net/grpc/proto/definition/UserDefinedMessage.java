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
import java.util.Collections;
import java.util.List;

/**
 * User Defined Message Definition Builder.
 *
 * @since 1.0.0
 */
public class UserDefinedMessage extends Message {
    List<Field> fieldList = new ArrayList<>();
    private List<UserDefinedMessage> nestedMsgList = new ArrayList<>();
    private List<UserDefinedEnumMessage> nestedEnumList = new ArrayList<>();
    private DescriptorProtos.DescriptorProto descriptorProto;

    @Override
    public DescriptorProtos.DescriptorProto getDescriptorProto() {
        return descriptorProto;
    }

    @Override
    public MessageKind getMessageKind() {
        return MessageKind.USER_DEFINED;
    }

    private UserDefinedMessage(DescriptorProtos.DescriptorProto descriptorProto) {
        this.descriptorProto = descriptorProto;
        this.messageName = descriptorProto.getName();
    }

    public static UserDefinedMessage.Builder newBuilder(String messageType) throws
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
        return new UserDefinedMessage.Builder(name);
    }

    public String getMessageDefinition() {
        StringBuilder msgDefinition = new StringBuilder();
        msgDefinition.append("message ").append(messageName).append(" {\n");

        for (Field field : fieldList) {
            msgDefinition.append("\t").append(field.getFieldDefinition());
        }
        msgDefinition.append("}\n");
        return msgDefinition.toString();
    }

    @Override
    public List<UserDefinedMessage> getNestedMessageList() {
        return Collections.unmodifiableList(nestedMsgList);
    }

    @Override
    public List<UserDefinedEnumMessage> getNestedEnumList() {
        return Collections.unmodifiableList(nestedEnumList);
    }

    /**
     * MessageDefinition.Builder
     */
    public static class Builder {
        private DescriptorProtos.DescriptorProto.Builder messageDescriptorBuilder;
        private List<Field> fieldList = new ArrayList<>();
        private List<UserDefinedMessage> nestedMsgList = new ArrayList<>();
        private List<UserDefinedEnumMessage> nestedEnumList = new ArrayList<>();

        public Message build() {
            UserDefinedMessage message = new UserDefinedMessage(messageDescriptorBuilder.build());
            message.fieldList = fieldList;
            message.nestedEnumList = nestedEnumList;
            message.nestedMsgList = nestedMsgList;
            return message;
        }

        private Builder(String messageName) {
            messageDescriptorBuilder = DescriptorProtos.DescriptorProto.newBuilder();
            messageDescriptorBuilder.setName(messageName);
        }

        public void addMessageDefinition(Message messageDefinition) {
            if (messageDefinition instanceof UserDefinedEnumMessage) {
                UserDefinedEnumMessage enumMessage = (UserDefinedEnumMessage) messageDefinition;
                messageDescriptorBuilder.addEnumType(enumMessage.getDescriptorProto());
                nestedEnumList.add(enumMessage);
            } else if (messageDefinition instanceof UserDefinedMessage) {
                UserDefinedMessage message = (UserDefinedMessage) messageDefinition;
                nestedMsgList.add(message);
            }
        }

        public void addFieldDefinition(Field fieldDefinition) {
            fieldList.add(fieldDefinition);
            messageDescriptorBuilder.addField(fieldDefinition.getFieldDescriptorProto());
        }
    }
}
