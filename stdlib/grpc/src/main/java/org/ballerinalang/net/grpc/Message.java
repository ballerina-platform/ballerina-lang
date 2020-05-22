/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.WireFormat;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Generic Proto3 Message.
 *
 * @since 1.0.0
 */
public class Message {

    private static final String GOOGLE_PROTOBUF_ANY = "google.protobuf.Any";
    private static final String GOOGLE_PROTOBUF_ANY_VALUE = "google.protobuf.Any.value";
    private static final String GOOGLE_PROTOBUF_ANY_TYPE_URL = "google.protobuf.Any.type_url";

    private String messageName;
    private int memoizedSize = -1;
    private HttpHeaders headers;
    private Object bMessage = null;
    private Descriptors.Descriptor descriptor = null;

    private static final BArrayType stringArrayType = new BArrayType(BTypes.typeString);
    private static final BArrayType booleanArrayType = new BArrayType(BTypes.typeBoolean);
    private static final BArrayType intArrayType = new BArrayType(BTypes.typeInt);
    private static final BArrayType floatArrayType = new BArrayType(BTypes.typeFloat);

    private boolean isError = false;
    private Throwable error;

    public Message(String messageName, Object bMessage) {
        this.messageName = messageName;
        this.bMessage = bMessage;
        this.descriptor = MessageRegistry.getInstance().getMessageDescriptor(messageName);
    }

    public Message(Descriptors.Descriptor descriptor, Object bMessage) {
        this.descriptor = descriptor;
        this.bMessage = bMessage;
        this.messageName = descriptor.getName();
    }

    private Message(String messageName) {
        this.messageName = messageName;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public boolean isError() {
        return isError;
    }

    public Throwable getError() {
        return error;
    }

    public Object getbMessage() {
        return bMessage;
    }

    public Message(Throwable error) {
        this.error = error;
        this.isError = true;
    }

    public Message(
            String messageName,
            BType bType,
            com.google.protobuf.CodedInputStream input,
            Map<Integer, Descriptors.FieldDescriptor> fieldDescriptors)
            throws IOException {
        this(messageName);

        if (bType instanceof BUnionType && ((BUnionType) bType).isNullable()) {
            List<BType> memberTypes = ((BUnionType) bType).getMemberTypes();
            if (memberTypes.size() != 2) {
                throw Status.Code.INTERNAL.toStatus().withDescription("Error while decoding request " +
                        "message. Field type is not a valid optional field type : " +
                        bType.getName()).asRuntimeException();
            }
            for (BType memberType : memberTypes) {
                if (memberType.getTag() != TypeTags.NULL_TAG) {
                    bType = memberType;
                    break;
                }
            }
        }

        BMap<BString, Object> bMapValue = null;
        if (bType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            bMapValue = BValueCreator.createRecordValue(bType.getPackage(), bType.getName());
            bMessage = bMapValue;
        }

        if (input == null) {
            if (bMapValue != null) {
                for (Map.Entry<Integer, Descriptors.FieldDescriptor> entry : fieldDescriptors.entrySet()) {
                    BString bFieldName = org.ballerinalang.jvm.StringUtils.fromString(entry.getValue().getName());
                    if (entry.getValue().getType().toProto().getNumber() ==
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE &&
                            !entry.getValue().isRepeated()) {
                        bMapValue.put(bFieldName, null);
                    } else if (entry.getValue().getType().toProto().getNumber() ==
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE) {
                        bMapValue.put(bFieldName, org.ballerinalang.jvm.StringUtils
                                .fromString(entry.getValue().getEnumType().findValueByNumber(0).toString()));
                    }
                }
            } else {
                // Here fieldDescriptors map size should be one. Because the value can assign to one scalar field.
                for (Map.Entry<Integer, Descriptors.FieldDescriptor> entry : fieldDescriptors.entrySet()) {
                    switch (entry.getValue().getType().toProto().getNumber()) {
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE:
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                            bMessage = (double) 0;
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE:
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                            bMessage = (long) 0;
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                            bMessage = org.ballerinalang.jvm.StringUtils.fromString("");
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                            bMessage = Boolean.FALSE;
                            break;
                        }
                        default: {
                            throw Status.Code.INTERNAL.toStatus().withDescription("Error while decoding request " +
                                    "message. Field type is not supported : " +
                                    entry.getValue().getType()).asRuntimeException();
                        }
                    }
                }
            }
            return;
        }
        boolean done = false;
        while (!done) {
            int tag = input.readTag();
            if (tag == 0) {
                done = true;
            } else if (fieldDescriptors.containsKey(tag)) {
                Descriptors.FieldDescriptor fieldDescriptor = fieldDescriptors.get(tag);
                BString bFieldName = org.ballerinalang.jvm.StringUtils.fromString(fieldDescriptor.getName());
                switch (fieldDescriptor.getType().toProto().getNumber()) {
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue floatArray = (ArrayValue) BValueCreator.createArrayValue(floatArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    floatArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, floatArray);
                                }
                                floatArray.add(floatArray.size(), input.readDouble());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readDouble());
                            } else {
                                bMapValue.put(bFieldName, input.readDouble());
                            }
                        } else {
                            bMessage = input.readDouble();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue floatArray = (ArrayValue) BValueCreator.createArrayValue(floatArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    floatArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, floatArray);
                                }
                                floatArray.add(floatArray.size(),
                                        Double.parseDouble(String.valueOf(input.readFloat())));
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                double bValue = Double.parseDouble(String.valueOf(input.readFloat()));
                                updateBMapValue(bMapValue, fieldDescriptor, bValue);
                            } else {
                                bMapValue.put(bFieldName, Double.parseDouble(String.valueOf(input.readFloat())));
                            }
                        } else {
                            bMessage = Double.parseDouble(String.valueOf(input.readFloat()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    intArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, intArray);
                                }
                                intArray.add(intArray.size(), input.readInt64());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readInt64());
                            } else {
                                bMapValue.put(bFieldName, input.readInt64());
                            }
                        } else {
                            bMessage = input.readInt64();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    intArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, intArray);
                                }
                                intArray.add(intArray.size(), input.readUInt64());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readUInt64());
                            } else {
                                bMapValue.put(bFieldName, input.readUInt64());
                            }
                        } else {
                            bMessage = input.readUInt64();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    intArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, intArray);
                                }
                                intArray.add(intArray.size(), input.readInt32());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readInt32());
                            } else {
                                bMapValue.put(bFieldName, input.readInt32());
                            }
                        } else {
                            bMessage = input.readInt32();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    intArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, intArray);
                                }
                                intArray.add(intArray.size(), input.readFixed64());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readFixed64());
                            } else {
                                bMapValue.put(bFieldName, input.readFixed64());
                            }
                        } else {
                            bMessage = input.readFixed64();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    intArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, intArray);
                                }
                                intArray.add(intArray.size(), input.readFixed32());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readFixed32());
                            } else {
                                bMapValue.put(bFieldName, input.readFixed32());
                            }
                        } else {
                            bMessage = input.readFixed32();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue booleanArray = (ArrayValue) BValueCreator.createArrayValue(booleanArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    booleanArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, booleanArray);
                                }
                                booleanArray.add(booleanArray.size(), input.readBool());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor, input.readBool());
                            } else {
                                bMapValue.put(bFieldName, input.readBool());
                            }
                        } else {
                            bMessage = input.readBool();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue stringArray = (ArrayValue) BValueCreator.createArrayValue(stringArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    stringArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, stringArray);
                                }
                                stringArray.add(stringArray.size(),
                                        org.ballerinalang.jvm.StringUtils.fromString(input.readStringRequireUtf8()));
                                bMapValue.put(bFieldName, stringArray);
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bMapValue, fieldDescriptor,
                                        org.ballerinalang.jvm.StringUtils.fromString(input.readStringRequireUtf8()));
                            } else {
                                bMapValue.put(bFieldName,
                                        org.ballerinalang.jvm.StringUtils.fromString(input.readStringRequireUtf8()));
                            }
                        } else if (!fieldDescriptor.getFullName().equals(GOOGLE_PROTOBUF_ANY_TYPE_URL)) {
                            bMessage = org.ballerinalang.jvm.StringUtils.fromString(input.readStringRequireUtf8());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue stringArray = (ArrayValue) BValueCreator.createArrayValue(stringArrayType);
                                if (bMapValue.containsKey(bFieldName)) {
                                    stringArray = (ArrayValue) bMapValue.get(bFieldName);
                                } else {
                                    bMapValue.put(bFieldName, stringArray);
                                }
                                stringArray.add(stringArray.size(), org.ballerinalang.jvm.StringUtils.fromString(
                                        fieldDescriptor.getEnumType().findValueByNumber(input.readEnum()).toString()));
                                bMapValue.put(bFieldName, stringArray);
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                String bValue = fieldDescriptor.getEnumType().findValueByNumber(input
                                        .readEnum()).toString();
                                updateBMapValue(bMapValue, fieldDescriptor,
                                        org.ballerinalang.jvm.StringUtils.fromString(bValue));
                            } else {
                                bMapValue.put(bFieldName, org.ballerinalang.jvm.StringUtils.fromString(
                                        fieldDescriptor.getEnumType().findValueByNumber(input.readEnum()).toString()));
                            }
                        } else {
                            bMessage = org.ballerinalang.jvm.StringUtils.fromString(
                                    fieldDescriptor.getEnumType().findValueByNumber(input.readEnum()).toString());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                        if (bMapValue != null) {
                             if (fieldDescriptor.getContainingOneof() != null) {
                                Object bValue = BValueCreator.createArrayValue(input.readByteArray());
                                updateBMapValue(bMapValue, fieldDescriptor, bValue);
                             } else {
                                 bMapValue.put(bFieldName, BValueCreator.createArrayValue(input.readByteArray()));
                             }
                        } else {
                            bMessage = BValueCreator.createArrayValue(input.readByteArray());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        BRecordType recordType;
                        if (bType instanceof BRecordType) {
                            recordType = (BRecordType) bType;
                        } else {
                            throw Status.Code.INTERNAL.toStatus().withDescription("Error while decoding request " +
                                    "message. record type is not supported : " +
                                    fieldDescriptor.getType()).asRuntimeException();
                        }
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue structArray = bMapValue.get(bFieldName) != null ?
                                        (ArrayValue) bMapValue.get(bFieldName) : null;
                                BType fieldType = recordType.getFields().get(bFieldName.getValue()).getFieldType();
                                if (structArray == null || structArray.size() == 0) {
                                    structArray = (ArrayValue) BValueCreator.createArrayValue((BArrayType) fieldType);
                                    bMapValue.put(bFieldName, structArray);
                                }
                                structArray.add(structArray.size(), readMessage(fieldDescriptor,
                                        ((BArrayType) fieldType).getElementType(), input).bMessage);
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                BType fieldType = recordType.getFields().get(bFieldName.getValue()).getFieldType();
                                Object bValue = readMessage(fieldDescriptor, fieldType, input).bMessage;
                                updateBMapValue(bMapValue, fieldDescriptor, bValue);
                            } else {
                                BType fieldType = recordType.getFields().get(bFieldName.getValue()).getFieldType();
                                bMapValue.put(bFieldName, readMessage(fieldDescriptor, fieldType, input).bMessage);
                            }
                        } else {
                            BType fieldType = recordType.getFields().get(bFieldName.getValue()).getFieldType();
                            bMessage = readMessage(fieldDescriptor, fieldType, input).bMessage;
                        }
                        break;
                    }
                    default: {
                        throw Status.Code.INTERNAL.toStatus().withDescription("Error while decoding request message. " +
                                "Field type is not supported : " + fieldDescriptor.getType()).asRuntimeException();
                    }
                }
            }
        }
    }

    private void updateBMapValue(BMap<BString, Object> bMapValue,
                                 Descriptors.FieldDescriptor fieldDescriptor, Object bValue) {
        bMapValue.put(org.ballerinalang.jvm.StringUtils.fromString(fieldDescriptor.getName()), bValue);
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        if (descriptor != null) {
            return descriptor;
        }
        return MessageRegistry.getInstance().getMessageDescriptor(messageName);
    }

    @SuppressWarnings("unchecked")
    void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
        if (bMessage == null) {
            return;
        }
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        if (messageDescriptor == null) {
            throw Status.Code.INTERNAL.toStatus()
                    .withDescription("Error while processing the message, Couldn't find message descriptor for " +
                            "message name: " + messageName)
                    .asRuntimeException();
        }

        MapValue<BString, Object> bMapValue = null;
        if (bMessage instanceof MapValue) {
            bMapValue = (MapValue<BString, Object>) bMessage;
        }
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            BString bFieldName = org.ballerinalang.jvm.StringUtils.fromString(fieldDescriptor.getName());
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeDouble(fieldDescriptor.getNumber(), valueArray.getFloat(i));
                            }
                        } else {
                            output.writeDouble(fieldDescriptor.getNumber(), (Double) bValue);
                        }
                    } else if (bMessage instanceof Double) {
                        output.writeDouble(fieldDescriptor.getNumber(), (Double) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                        (valueArray.getFloat(i))));
                            }
                        } else {
                            output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf(bValue)));
                        }
                    } else if (bMessage instanceof Double) {
                        output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                (bMessage)));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeInt64(fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            output.writeInt64(fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeInt64(fieldDescriptor.getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeUInt64(fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            output.writeUInt64(fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeUInt64(fieldDescriptor.getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeInt32(fieldDescriptor.getNumber(),
                                                  getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            output.writeInt32(fieldDescriptor.getNumber(), getIntValue(bValue));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeInt32(fieldDescriptor.getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFixed64(fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            output.writeFixed64(fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeFixed64(fieldDescriptor.getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFixed32(fieldDescriptor.getNumber(),
                                                    getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            output.writeFixed32(fieldDescriptor.getNumber(), getIntValue(bValue));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeFixed32(fieldDescriptor.getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeBool(fieldDescriptor.getNumber(), valueArray.getBoolean(i));
                            }
                        } else {
                            output.writeBool(fieldDescriptor.getNumber(), ((boolean) bValue));
                        }
                    } else if (bMessage instanceof Boolean) {
                        output.writeBool(fieldDescriptor.getNumber(), (boolean) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeString(fieldDescriptor.getNumber(), valueArray.getBString(i).getValue());
                            }
                        } else {
                            output.writeString(fieldDescriptor.getNumber(), ((BString) bValue).getValue());
                        }
                    } else if (bMessage instanceof BString
                            && !fieldDescriptor.getFullName().equals(GOOGLE_PROTOBUF_ANY_TYPE_URL)) {
                        output.writeString(fieldDescriptor.getNumber(), ((BString) bMessage).getValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue
                                && !fieldDescriptor.getMessageType().getFullName().equals(GOOGLE_PROTOBUF_ANY_VALUE)) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                Message message = new Message(fieldDescriptor.getMessageType(),
                                                              valueArray.getRefValue(i));
                                output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                                output.writeUInt32NoTag(message.getSerializedSize());
                                message.writeTo(output);
                            }
                        } else {
                            Message message = new Message(fieldDescriptor.getMessageType(), bValue);
                            output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                            output.writeUInt32NoTag(message.getSerializedSize());
                            message.writeTo(output);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        output.writeEnum(fieldDescriptor.getNumber(), fieldDescriptor.getEnumType()
                                .findValueByName(((BString) bValue).getValue()).getNumber());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            output.writeByteArray(fieldDescriptor.getNumber(), valueArray.getBytes());
                        }
                    } else if (bMessage instanceof ArrayValue) {
                        ArrayValue valueArray = (ArrayValue) bMessage;
                        output.writeByteArray(fieldDescriptor.getNumber(), valueArray.getBytes());
                    }
                    break;
                }
                default: {
                    throw Status.Code.INTERNAL.toStatus().withDescription("Error while writing output stream. " +
                            "Field type is not supported : " + fieldDescriptor.getType()).asRuntimeException();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) {
            return size;
        }
        size = 0;
        if (bMessage == null) {
            memoizedSize = size;
            return size;
        }
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        if (messageDescriptor == null) {
            throw Status.Code.INTERNAL.toStatus()
                    .withDescription("Error while processing the message, Couldn't find message descriptor for " +
                            "message name: " + messageName)
                    .asRuntimeException();
        }
        MapValue<BString, Object> bMapValue = null;
        if (bMessage instanceof MapValue) {
            bMapValue = (MapValue<BString, Object>) bMessage;
        }

        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            BString bFieldName = org.ballerinalang.jvm.StringUtils.fromString(fieldDescriptor.getName());
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeDoubleSize(
                                        fieldDescriptor.getNumber(), valueArray.getFloat(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                                                                            (double) bValue);
                        }
                    } else if (bMessage instanceof Double) {
                        size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                ((double) bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFloatSize(
                                        fieldDescriptor.getNumber(),
                                        Float.parseFloat(String.valueOf(valueArray.getFloat(i))));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFloatSize(
                                    fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf(bValue)));
                        }
                    } else if (bMessage instanceof Double) {
                        size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                .getNumber(), Float.parseFloat(String.valueOf(bMessage)));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeInt64Size(
                                        fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt64Size(
                                    fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                .getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(
                                        fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeUInt64Size(
                                    fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                .getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeInt32Size(
                                        fieldDescriptor.getNumber(), getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt32Size(
                                    fieldDescriptor.getNumber(), getIntValue(bValue));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                .getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed64Size(
                                        fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed64Size(
                                    fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                .getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed32Size(
                                        fieldDescriptor.getNumber(), getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed32Size(
                                    fieldDescriptor.getNumber(), getIntValue(bValue));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                .getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeBoolSize(
                                        fieldDescriptor.getNumber(), valueArray.getBoolean(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeBoolSize(
                                    fieldDescriptor.getNumber(), (boolean) bValue);
                        }
                    } else if (bMessage instanceof Boolean) {
                        size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                .getNumber(), (boolean) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), valueArray
                                        .getBString(i).getValue());
                            }
                        } else {
                            size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(),
                                    ((BString) bValue).getValue());
                        }
                    } else if (bMessage instanceof BString) {
                        size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(),
                                ((BString) bMessage).getValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue
                                && !fieldDescriptor.getMessageType().getFullName().equals(GOOGLE_PROTOBUF_ANY)) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                MapValue<BString, Object> value = (MapValue<BString, Object>) valueArray.getRefValue(i);
                                Message message = new Message(fieldDescriptor.getMessageType(), value);
                                size += computeMessageSize(fieldDescriptor, message);
                            }
                        } else {
                            Message message = new Message(fieldDescriptor.getMessageType(), bValue);
                            size += computeMessageSize(fieldDescriptor, message);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);

                        size += com.google.protobuf.CodedOutputStream.computeEnumSize(
                                fieldDescriptor.getNumber(),
                                fieldDescriptor.getEnumType().findValueByName(((BString) bValue).getValue())
                                        .getNumber());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(bFieldName)) {
                        Object bValue = bMapValue.get(bFieldName);
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            size += com.google.protobuf.CodedOutputStream
                                    .computeByteArraySize(fieldDescriptor.getNumber(), valueArray.getBytes());
                        }
                    } else if (bMessage instanceof ArrayValue) {
                        ArrayValue valueArray = (ArrayValue) bMessage;
                        size += com.google.protobuf.CodedOutputStream
                                .computeByteArraySize(fieldDescriptor.getNumber(), valueArray.getBytes());
                    }
                    break;
                }
                default:
                    throw Status.Code.INTERNAL.toStatus().withDescription(
                            "Error while calculating the serialized type. Field type is not supported : "
                                    + fieldDescriptor.getType()).asRuntimeException();

            }
        }
        memoizedSize = size;
        return size;
    }

    private int computeMessageSize(Descriptors.FieldDescriptor fieldDescriptor, Message message) {
        return CodedOutputStream.computeTagSize(fieldDescriptor
                .getNumber()) + CodedOutputStream.computeUInt32SizeNoTag
                (message.getSerializedSize()) + message.getSerializedSize();
    }

    public byte[] toByteArray() {
        try {
            final byte[] result = new byte[getSerializedSize()];
            final CodedOutputStream output = CodedOutputStream.newInstance(result);
            writeTo(output);
            output.checkNoSpaceLeft();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Serializing " + messageName + " to a byte array threw an IOException" +
                    " (should never happen).", e);
        }
    }


    private Message readMessage(final Descriptors.FieldDescriptor fieldDescriptor, final BType bType,
                                final CodedInputStream in) throws IOException {
        int length = in.readRawVarint32();
        final int oldLimit = in.pushLimit(length);
        Message result = new MessageParser(fieldDescriptor.getMessageType(), bType).parseFrom(in);
        in.popLimit(oldLimit);
        return result;
    }

    private int getIntValue(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return (int) value;
    }

    @Override
    public String toString() {
        StringBuilder payload = new StringBuilder("Message : ");
        if (bMessage != null) {
            payload.append("{ ").append(StringUtils.getJsonString(bMessage)).append(" }");
        } else {
            payload.append("null");
        }
        return payload.toString();
    }
}
