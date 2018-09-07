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
 * Enum Message Definition.
 *
 * @since 0.982.0
 */
public class EnumMessage {
    private List<EnumField> fieldList;
    private String messageName;

    private EnumMessage(String messageName, List<EnumField> fieldList) {
        this.messageName = messageName;
        this.fieldList = fieldList;
    }

    public static EnumMessage.Builder newBuilder(DescriptorProtos.EnumDescriptorProto enumDescriptor) {
        return new EnumMessage.Builder(enumDescriptor);
    }

    public List<EnumField> getFieldList() {
        return fieldList;
    }

    public String getMessageName() {
        return messageName;
    }

    /**
     * Enum Message.Builder.
     */
    public static class Builder {
        private DescriptorProtos.EnumDescriptorProto enumDescriptor;
    
        public EnumMessage build() {
            List<EnumField> fieldList = new ArrayList<>();
            for (DescriptorProtos.EnumValueDescriptorProto fieldDescriptor : enumDescriptor.getValueList()) {
                EnumField field = EnumField.newBuilder(fieldDescriptor).build();
                fieldList.add(field);
            }
            return new EnumMessage(enumDescriptor.getName(), fieldList);
        }

        private Builder(DescriptorProtos.EnumDescriptorProto enumDescriptor) {
            this.enumDescriptor = enumDescriptor;
        }
    }
}
