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

import java.util.HashMap;
import java.util.Map;

/**
 * Field definition builder.
 *
 * @since 1.0.0
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
                .getName()).append(" = ").append(fieldDescriptorProto.getNumber()).append(";").append
                (ServiceProtoConstants.NEW_LINE_CHARACTER);
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
            // ignore the label when label is null.
            if (label == null) {
                return this;
            }

            fieldLabel = label;
            DescriptorProtos.FieldDescriptorProto.Label protoLabel = STRING_LABEL_MAP.get(label);
            if (protoLabel == null) {
                throw new IllegalArgumentException("Illegal label: " + label);
            }
            fieldDescriptorBuilder.setLabel(protoLabel);
            return this;
        }

        public Builder setType(String type) {
            fieldType = BALLERINA_TO_PROTO_MAP.get(type) != null ? BALLERINA_TO_PROTO_MAP.get(type) : type;
            DescriptorProtos.FieldDescriptorProto.Type primType = STRING_TYPE_MAP.get(fieldType);
            if (primType != null) {
                fieldDescriptorBuilder.setType(primType);
            } else {
                fieldDescriptorBuilder.setTypeName(fieldType);
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


    private static final Map<String, DescriptorProtos.FieldDescriptorProto.Type> STRING_TYPE_MAP;
    private static final Map<String, DescriptorProtos.FieldDescriptorProto.Label> STRING_LABEL_MAP;
    private static final Map<String, String> BALLERINA_TO_PROTO_MAP;

    static {
        STRING_TYPE_MAP = new HashMap<>();
        STRING_TYPE_MAP.put("double", DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);
        STRING_TYPE_MAP.put("float", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT);
        STRING_TYPE_MAP.put("int32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        STRING_TYPE_MAP.put("int64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
        STRING_TYPE_MAP.put("uint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
        STRING_TYPE_MAP.put("uint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64);
        STRING_TYPE_MAP.put("sint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32);
        STRING_TYPE_MAP.put("sint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64);
        STRING_TYPE_MAP.put("fixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32);
        STRING_TYPE_MAP.put("fixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64);
        STRING_TYPE_MAP.put("sfixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32);
        STRING_TYPE_MAP.put("sfixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64);
        STRING_TYPE_MAP.put("bool", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
        STRING_TYPE_MAP.put("string", DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING);
        STRING_TYPE_MAP.put("bytes", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES);

        STRING_TYPE_MAP.put("int", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
        STRING_TYPE_MAP.put("boolean", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
        STRING_TYPE_MAP.put("blob", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES);

        STRING_LABEL_MAP = new HashMap<>();
        STRING_LABEL_MAP.put("optional", DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        STRING_LABEL_MAP.put("required", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED);
        STRING_LABEL_MAP.put("repeated", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);

        BALLERINA_TO_PROTO_MAP = new HashMap<>();
        BALLERINA_TO_PROTO_MAP.put("int", "int64");
        BALLERINA_TO_PROTO_MAP.put("boolean", "bool");
        BALLERINA_TO_PROTO_MAP.put("string", "string");
        BALLERINA_TO_PROTO_MAP.put("blob", "byte");
    }
}
