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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.toCamelCase;

/**
 * Message Definition bean class.
 *
 * @since 0.982.0
 */
public class Message {
    private List<Field> fieldList;
    private String messageName;
    private Map<String, List<Message>> oneofFieldMap;
    private List<EnumMessage> enumList;

    private Message(String messageName, List<Field> fieldList) {
        this.messageName = messageName;
        this.fieldList = fieldList;
    }

    private void setOneofFieldMap(Map<String, List<Message>> oneofFieldMap) {
        this.oneofFieldMap = oneofFieldMap;
    }

    public Map<String, List<Message>> getOneofFieldMap() {
        return oneofFieldMap;
    }

    public List<EnumMessage> getEnumList() {
        return enumList;
    }

    private void setEnumList(List<EnumMessage> enumList) {
        this.enumList = enumList;
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
     * Message Definition.Builder.
     */
    public static class Builder {
        private DescriptorProtos.DescriptorProto messageDescriptor;

        public Message build() {
            List<Field> fieldList = new ArrayList<>();
            Map<String, List<Message>> oneofFieldMap = new HashMap<>();
            for (DescriptorProtos.FieldDescriptorProto fieldDescriptorProto : messageDescriptor.getFieldList()) {
                if (fieldDescriptorProto.hasOneofIndex()) {
                    List<Field> tempList = new ArrayList<>(1);
                    tempList.add(Field.newBuilder(fieldDescriptorProto).build());
                    Message message = new Message(messageDescriptor.getName() + "_" + toCamelCase
                            (fieldDescriptorProto.getName()), tempList);
                    String oneofField = messageDescriptor.getOneofDecl(fieldDescriptorProto.getOneofIndex()).getName();
                    List<Message> oneofMessageList = oneofFieldMap.computeIfAbsent(oneofField, k -> new ArrayList<>());
                    oneofMessageList.add(message);
                } else {
                    Field field = Field.newBuilder(fieldDescriptorProto).build();
                    fieldList.add(field);
                }
            }
            List<EnumMessage> enumList = new ArrayList<>();
            for (DescriptorProtos.EnumDescriptorProto enumDescriptorProto : messageDescriptor.getEnumTypeList()) {
                EnumMessage enumMessage = EnumMessage.newBuilder(enumDescriptorProto).build();
                enumList.add(enumMessage);
            }
            Message message = new Message(messageDescriptor.getName(), fieldList);

            if (!oneofFieldMap.isEmpty()) {
                message.setOneofFieldMap(oneofFieldMap);
            }
            if (!enumList.isEmpty()) {
                message.setEnumList(enumList);
            }
            return message;
        }

        private Builder(DescriptorProtos.DescriptorProto messageDescriptor) {
            this.messageDescriptor = messageDescriptor;
        }
    }
}
