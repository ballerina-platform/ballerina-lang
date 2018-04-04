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
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;

/**
 * Enum Field definition builder.
 *
 * @since 1.0.0
 */
public class EnumField {
    private DescriptorProtos.EnumValueDescriptorProto fieldDescriptorProto;
    
    EnumField(DescriptorProtos.EnumValueDescriptorProto descriptorProto) {
        this.fieldDescriptorProto = descriptorProto;
    }

    public static EnumField.Builder newBuilder() {
        return new EnumField.Builder();
    }

    public DescriptorProtos.EnumValueDescriptorProto getFieldDescriptorProto() {
        return fieldDescriptorProto;
    }

    public String getFieldDefinition() {
        return fieldDescriptorProto.getName() + " = " + fieldDescriptorProto.getNumber() +
                ";" + ServiceProtoConstants.NEW_LINE_CHARACTER;
    }

    /**
     * Enum Field Definition.Builder
     */
    public static class Builder {
        private DescriptorProtos.EnumValueDescriptorProto.Builder fieldDescriptorBuilder;
        
        public EnumField build() {
            return new EnumField(fieldDescriptorBuilder.build());
        }

        private Builder() {
            fieldDescriptorBuilder = DescriptorProtos.EnumValueDescriptorProto.newBuilder();
        }

        public Builder setName(String name) {
            fieldDescriptorBuilder.setName(name);
            return this;
        }

        public Builder setIndex(int index) {
            fieldDescriptorBuilder.setNumber(index);
            return this;
        }
        
    }
}
