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

import com.google.protobuf.DescriptorProtos;

import java.util.HashMap;
import java.util.Map;

/**
 * Field definition builder.
 */
public class Field {
    private DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private String fieldType;
    private String fieldLabel;

    private Field(DescriptorProtos.FieldDescriptorProto descriptorProto) {
        this.fieldDescriptorProto = descriptorProto;
    }

    public static Field.Builder newBuilder(String fieldName) {
        return new Field.Builder(fieldName);
    }

    public DescriptorProtos.FieldDescriptorProto getFieldDescriptorProto() {
        return fieldDescriptorProto;
    }

    public String getFieldDefinition() {
        StringBuilder fieldDefinition = new StringBuilder();
        if (fieldLabel != null) {
            fieldDefinition.append(fieldLabel).append(" ");
        }
        fieldDefinition.append(fieldType).append(" ").append(fieldDescriptorProto
                .getName()).append(" = ").append(fieldDescriptorProto.getNumber()).append(";\n");
        return fieldDefinition.toString();

    }

    /**
     * FieldDefinition.Builder
     */
    public static class Builder {
        private DescriptorProtos.FieldDescriptorProto.Builder fieldDescriptorBuilder;
        private String fieldType;
        private String fieldLabel;

        public Field build() {
            Field field = new Field(fieldDescriptorBuilder.build());
            field.fieldType = fieldType;
            field.fieldLabel = fieldLabel;
            return field;
        }

        private Builder(String fieldName) {
            fieldDescriptorBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder();
            fieldDescriptorBuilder.setName(fieldName);
        }

        public Builder setLabel(String label) {
            fieldLabel = label;
            DescriptorProtos.FieldDescriptorProto.Label protoLabel = sLabelMap.get(label);
            if (protoLabel == null) {
                throw new IllegalArgumentException("Illegal label: " + label);
            }
            fieldDescriptorBuilder.setLabel(protoLabel);
            return this;
        }

        public Builder setType(String type) {
            fieldType = type;
            DescriptorProtos.FieldDescriptorProto.Type primType = sTypeMap.get(type);
            if (primType != null) {
                fieldDescriptorBuilder.setType(primType);
            } else {
                fieldDescriptorBuilder.setTypeName(type);
            }
            return this;
        }

        public Builder setIndex(int index) {
            fieldDescriptorBuilder.setNumber(index);
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            if (defaultValue != null) {
                fieldDescriptorBuilder.setDefaultValue(defaultValue);
            }
            return this;
        }
    }


    private static Map<String, DescriptorProtos.FieldDescriptorProto.Type> sTypeMap;
    private static Map<String, DescriptorProtos.FieldDescriptorProto.Label> sLabelMap;

    static {
        sTypeMap = new HashMap<>();
        sTypeMap.put("double", DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);
        sTypeMap.put("float", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT);
        sTypeMap.put("int32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        sTypeMap.put("int64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
        sTypeMap.put("uint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
        sTypeMap.put("uint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64);
        sTypeMap.put("sint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32);
        sTypeMap.put("sint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64);
        sTypeMap.put("fixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32);
        sTypeMap.put("fixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64);
        sTypeMap.put("sfixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32);
        sTypeMap.put("sfixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64);
        sTypeMap.put("bool", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
        sTypeMap.put("string", DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING);
        sTypeMap.put("bytes", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES);

        sLabelMap = new HashMap<>();
        sLabelMap.put("optional", DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        sLabelMap.put("required", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED);
        sLabelMap.put("repeated", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
    }
}
