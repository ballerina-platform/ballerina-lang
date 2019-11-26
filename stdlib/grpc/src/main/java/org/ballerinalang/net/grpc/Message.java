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
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.io.IOException;
import java.util.Map;

import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.toCamelCase;

/**
 * Generic Proto3 Message.
 *
 * @since 1.0.0
 */
public class Message {
    private String messageName;
    private int memoizedSize = -1;
    private HttpHeaders headers;
    private Object bMessage = null;
    private Descriptors.Descriptor descriptor = null;

    private boolean isError = false;
    private Throwable error;

    public Message(String messageName, Object bMessage) {
        this.messageName = messageName;
        this.bMessage = bMessage;
        this.descriptor = MessageRegistry.getInstance().getMessageDescriptor(messageName);
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
        MapValue<String, Object> bMapValue = null;
        if (bType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            bMapValue = new MapValueImpl<>(bType);
            bMessage = bMapValue;
        }

        if (input == null) {
            if (bMapValue != null) {
                for (Map.Entry<Integer, Descriptors.FieldDescriptor> entry : fieldDescriptors.entrySet()) {
                    if (entry.getValue().getType().toProto().getNumber() ==
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE &&
                            !entry.getValue().isRepeated()) {
                        bMapValue.put(entry.getValue().getName(), null);
                    } else if (entry.getValue().getType().toProto().getNumber() ==
                            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE) {
                        bMapValue.put(entry.getValue().getName(),
                                entry.getValue().getEnumType().findValueByNumber(0).toString());
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
                            bMessage = "";
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
                String name = fieldDescriptor.getName();
                switch (fieldDescriptor.getType().toProto().getNumber()) {
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue floatArray = new ArrayValueImpl(new BArrayType(BTypes.typeFloat));
                                if (bMapValue.containsKey(name)) {
                                    floatArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, floatArray);
                                }
                                floatArray.add(floatArray.size(), input.readDouble());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readDouble());
                            } else {
                                bMapValue.put(name, input.readDouble());
                            }
                        } else {
                            bMessage = input.readDouble();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue floatArray = new ArrayValueImpl(new BArrayType(BTypes.typeFloat));
                                if (bMapValue.containsKey(name)) {
                                    floatArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, floatArray);
                                }
                                floatArray.add(floatArray.size(),
                                        Double.parseDouble(String.valueOf(input.readFloat())));
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                double bValue = Double.parseDouble(String.valueOf(input.readFloat()));
                                updateBMapValue(bType, bMapValue, fieldDescriptor, bValue);
                            } else {
                                bMapValue.put(name, Double.parseDouble(String.valueOf(input.readFloat())));
                            }
                        } else {
                            bMessage = Double.parseDouble(String.valueOf(input.readFloat()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = new ArrayValueImpl(new BArrayType(BTypes.typeInt));
                                if (bMapValue.containsKey(name)) {
                                    intArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, intArray);
                                }
                                intArray.add(intArray.size(), input.readInt64());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readInt64());
                            } else {
                                bMapValue.put(name, input.readInt64());
                            }
                        } else {
                            bMessage = input.readInt64();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = new ArrayValueImpl(new BArrayType(BTypes.typeInt));
                                if (bMapValue.containsKey(name)) {
                                    intArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, intArray);
                                }
                                intArray.add(intArray.size(), input.readUInt64());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readUInt64());
                            } else {
                                bMapValue.put(name, input.readUInt64());
                            }
                        } else {
                            bMessage = input.readUInt64();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = new ArrayValueImpl(new BArrayType(BTypes.typeInt));
                                if (bMapValue.containsKey(name)) {
                                    intArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, intArray);
                                }
                                intArray.add(intArray.size(), input.readInt32());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readInt32());
                            } else {
                                bMapValue.put(name, input.readInt32());
                            }
                        } else {
                            bMessage = input.readInt32();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = new ArrayValueImpl(new BArrayType(BTypes.typeInt));
                                if (bMapValue.containsKey(name)) {
                                    intArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, intArray);
                                }
                                intArray.add(intArray.size(), input.readFixed64());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readFixed64());
                            } else {
                                bMapValue.put(name, input.readFixed64());
                            }
                        } else {
                            bMessage = input.readFixed64();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue intArray = new ArrayValueImpl(new BArrayType(BTypes.typeInt));
                                if (bMapValue.containsKey(name)) {
                                    intArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, intArray);
                                }
                                intArray.add(intArray.size(), input.readFixed32());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readFixed32());
                            } else {
                                bMapValue.put(name, input.readFixed32());
                            }
                        } else {
                            bMessage = input.readFixed32();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue booleanArray = new ArrayValueImpl(new BArrayType(BTypes.typeBoolean));
                                if (bMapValue.containsKey(name)) {
                                    booleanArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, booleanArray);
                                }
                                booleanArray.add(booleanArray.size(), input.readBool());
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readBool());
                            } else {
                                bMapValue.put(name, input.readBool());
                            }
                        } else {
                            bMessage = input.readBool();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeString));
                                if (bMapValue.containsKey(name)) {
                                    stringArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, stringArray);
                                }
                                stringArray.add(stringArray.size(), input.readStringRequireUtf8());
                                bMapValue.put(name, stringArray);
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                updateBMapValue(bType, bMapValue, fieldDescriptor, input.readStringRequireUtf8());
                            } else {
                                bMapValue.put(name, input.readStringRequireUtf8());
                            }
                        } else {
                            bMessage = input.readStringRequireUtf8();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeString));
                                if (bMapValue.containsKey(name)) {
                                    stringArray = (ArrayValue) bMapValue.get(name);
                                } else {
                                    bMapValue.put(name, stringArray);
                                }
                                stringArray.add(stringArray.size(), fieldDescriptor.getEnumType().findValueByNumber
                                        (input.readEnum()).toString());
                                bMapValue.put(name, stringArray);
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                Object bValue = fieldDescriptor.getEnumType().findValueByNumber(input
                                        .readEnum()).toString();
                                updateBMapValue(bType, bMapValue, fieldDescriptor, bValue);
                            } else {
                                bMapValue.put(name, fieldDescriptor.getEnumType().findValueByNumber(input
                                        .readEnum()).toString());
                            }
                        } else {
                            bMessage = fieldDescriptor.getEnumType().findValueByNumber(input.readEnum()).toString();
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                        if (bMapValue != null) {
                             if (fieldDescriptor.getContainingOneof() != null) {
                                Object bValue = new ArrayValueImpl(input.readByteArray());
                                updateBMapValue(bType, bMapValue, fieldDescriptor, bValue);
                             } else {
                                 bMapValue.put(name, new ArrayValueImpl(input.readByteArray()));
                             }
                        } else {
                            bMessage = new ArrayValueImpl(input.readByteArray());
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
                                ArrayValue structArray = bMapValue.get(name) != null ?
                                        (ArrayValue) bMapValue.get(name) : null;
                                BType fieldType = recordType.getFields().get(name).getFieldType();
                                if (structArray == null || structArray.size() == 0) {
                                    structArray = new ArrayValueImpl((BArrayType) fieldType);
                                    bMapValue.put(name, structArray);
                                }
                                structArray.add(structArray.size(), readMessage(fieldDescriptor,
                                        ((BArrayType) fieldType).getElementType(), input).bMessage);
                            } else if (fieldDescriptor.getContainingOneof() != null) {
                                Object bValue = readMessage(fieldDescriptor, bType, input).bMessage;
                                updateBMapValue(bType, bMapValue, fieldDescriptor, bValue);
                            } else {
                                BType fieldType = recordType.getFields().get(name).getFieldType();
                                bMapValue.put(name, readMessage(fieldDescriptor, fieldType, input).bMessage);
                            }
                        } else {
                            BType fieldType = recordType.getFields().get(name).getFieldType();
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

    private void updateBMapValue(BType bType, MapValue<String, Object> bMapValue,
                                 Descriptors.FieldDescriptor fieldDescriptor, Object bValue) {
        MapValue<String, Object> bMsg = getOneOfBValue(bType, fieldDescriptor, bValue);
        bMapValue.put(fieldDescriptor.getContainingOneof().getName(), bMsg);
    }

    private MapValue<String, Object> getOneOfBValue(BType bType, Descriptors.FieldDescriptor fieldDescriptor,
                                                    Object bValue) {
        Descriptors.OneofDescriptor oneofDescriptor = fieldDescriptor.getContainingOneof();
        String msgType = oneofDescriptor.getContainingType().getName() + "_" + toCamelCase
                (fieldDescriptor.getName());
        int typeFlags = TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE);
        MapValue<String, Object> bMsg =
                new MapValueImpl<>(new BRecordType(msgType, bType.getPackage(), 0, true, typeFlags));
        bMsg.put(fieldDescriptor.getName(), bValue);
        return bMsg;
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
        MapValue<String, Object> bMapValue = null;
        if (bMessage instanceof MapValue) {
            bMapValue = (MapValue) bMessage;
        }
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeDouble(fieldDescriptor.getNumber(), valueArray.getFloat(i));
                            }
                        } else {
                            output.writeDouble(fieldDescriptor.getNumber(), (Double) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeDouble(fieldDescriptor.getNumber(),
                                    (Double) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Double) {
                        output.writeDouble(fieldDescriptor.getNumber(), (Double) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                        (valueArray.getFloat(i))));
                            }
                        } else {
                            output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf(bValue)));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeFloat(fieldDescriptor.getNumber(),
                                    Float.parseFloat(String.valueOf(
                                            ((MapValue) bValue).get(fieldDescriptor.getName()))));
                        }
                    } else if (bMessage instanceof Double) {
                        output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                (bMessage)));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeInt64(fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            output.writeInt64(fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeInt64(fieldDescriptor.getNumber(),
                                    (long) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeInt64(fieldDescriptor.getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeUInt64(fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            output.writeUInt64(fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeUInt64(fieldDescriptor.getNumber(),
                                    (long) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeUInt64(fieldDescriptor.getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeInt32(fieldDescriptor.getNumber(),
                                        getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            output.writeInt32(fieldDescriptor.getNumber(), getIntValue(bValue));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeInt32(fieldDescriptor.getNumber(),
                                    getIntValue(((MapValue) bValue).get(fieldDescriptor.getName())));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeInt32(fieldDescriptor.getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFixed64(fieldDescriptor.getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            output.writeFixed64(fieldDescriptor.getNumber(), (long) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeFixed64(fieldDescriptor.getNumber(),
                                    (long) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeFixed64(fieldDescriptor.getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFixed32(fieldDescriptor.getNumber(),
                                        getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            output.writeFixed32(fieldDescriptor.getNumber(), getIntValue(bValue));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeFixed32(fieldDescriptor.getNumber(),
                                    getIntValue(((MapValue) bValue).get(fieldDescriptor.getName())));
                        }
                    } else if (bMessage instanceof Long) {
                        output.writeFixed32(fieldDescriptor.getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeBool(fieldDescriptor.getNumber(), valueArray.getBoolean(i));
                            }
                        } else {
                            output.writeBool(fieldDescriptor.getNumber(), ((boolean) bValue));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeBool(fieldDescriptor.getNumber(),
                                    (boolean) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Boolean) {
                        output.writeBool(fieldDescriptor.getNumber(), (boolean) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeString(fieldDescriptor.getNumber(), valueArray.getString(i));
                            }
                        } else {
                            output.writeString(fieldDescriptor.getNumber(), (String) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeString(fieldDescriptor.getNumber(), (String) ((MapValue) bValue)
                                    .get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof String) {
                        output.writeString(fieldDescriptor.getNumber(), (String) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                Message message = new Message(fieldDescriptor.getMessageType().getName(),
                                        valueArray.getRefValue(i));
                                output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                                output.writeUInt32NoTag(message.getSerializedSize());
                                message.writeTo(output);
                            }
                        } else {
                            Message message = new Message(fieldDescriptor.getMessageType().getName(),
                                    bValue);
                            output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                            output.writeUInt32NoTag(message.getSerializedSize());
                            message.writeTo(output);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            Message message = new Message(fieldDescriptor.getMessageType().getName(),
                                    ((MapValue) bValue).get(fieldDescriptor.getName()));
                            output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                            output.writeUInt32NoTag(message.getSerializedSize());
                            message.writeTo(output);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        output.writeEnum(fieldDescriptor.getNumber(), fieldDescriptor.getEnumType().findValueByName
                                ((String) bValue).getNumber());
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeEnum(fieldDescriptor.getNumber(),
                                    fieldDescriptor.getEnumType().findValueByName
                                            ((String) ((MapValue) bValue).get(fieldDescriptor.getName())).getNumber());
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            output.writeByteArray(fieldDescriptor.getNumber(), valueArray.getBytes());
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            output.writeByteArray(fieldDescriptor.getNumber(),
                                    ((ArrayValue) ((MapValue) bValue).get(fieldDescriptor.getName())).getBytes());
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

    private Object getOneofFieldMap(MapValue<String, Object> bMapValue, Descriptors.FieldDescriptor fieldDescriptor) {
        Descriptors.OneofDescriptor oneofDescriptor = fieldDescriptor.getContainingOneof();
        return bMapValue.get(oneofDescriptor.getName());
    }

    private boolean isOneofField(MapValue<String, Object> bMapValue, Descriptors.FieldDescriptor fieldDescriptor) {
        return bMapValue != null && fieldDescriptor.getContainingOneof() != null;
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
                    .withDescription("Error while processing the message, Couldn't find message descriptor.")
                    .asRuntimeException();
        }
        MapValue<String, Object> bMapValue = null;
        if (bMessage instanceof MapValue) {
            bMapValue = (MapValue<String, Object>) bMessage;
        }

        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor
                                        .getNumber(), valueArray.getFloat(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                    (double) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                    (double) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Double) {
                        size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                ((double) bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                        .getNumber(), Float.parseFloat(String.valueOf(valueArray.getFloat(i))));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                    .getNumber(), Float.parseFloat(String.valueOf(bValue)));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor.getNumber(),
                                    Float.parseFloat(String.valueOf(
                                            ((MapValue) bValue).get(fieldDescriptor.getName()))));
                        }
                    } else if (bMessage instanceof Double) {
                        size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                .getNumber(), Float.parseFloat(String.valueOf(bMessage)));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                        .getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                    .getNumber(), (long) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor.getNumber(),
                                    (long) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                .getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                        .getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                    .getNumber(), (long) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor.getNumber(),
                                    (long) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                .getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                        .getNumber(), getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                    .getNumber(), getIntValue(bValue));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor.getNumber(),
                                    getIntValue(((MapValue) bValue).get(fieldDescriptor.getName())));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                .getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                        .getNumber(), valueArray.getInt(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                    .getNumber(), (long) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeFixed64Size(
                                    fieldDescriptor.getNumber(),
                                    (long) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                .getNumber(), (long) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                        .getNumber(), getIntValue(valueArray.getInt(i)));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                    .getNumber(), getIntValue(bValue));
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeFixed32Size(
                                    fieldDescriptor.getNumber(),
                                    getIntValue(((MapValue) bValue).get(fieldDescriptor.getName())));
                        }
                    } else if (bMessage instanceof Long) {
                        size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                .getNumber(), getIntValue(bMessage));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                        .getNumber(), valueArray.getBoolean(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                    .getNumber(), (boolean) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor.getNumber(),
                                    (boolean) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof Boolean) {
                        size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                .getNumber(), (boolean) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), valueArray
                                        .getString(i));
                            }
                        } else {
                            size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), (String) bValue);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(),
                                    (String) ((MapValue) bValue).get(fieldDescriptor.getName()));
                        }
                    } else if (bMessage instanceof String) {
                        size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), (String) bMessage);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                MapValue<String, Object> value = (MapValue) valueArray.getRefValue(i);
                                Message message = new Message(fieldDescriptor.getMessageType().getName(), value);
                                size += computeMessageSize(fieldDescriptor, message);
                            }
                        } else {
                            Message message = new Message(fieldDescriptor.getMessageType().getName(),
                                    bValue);
                            size += computeMessageSize(fieldDescriptor, message);
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            Message message = new Message(fieldDescriptor.getMessageType().getName(),
                                    ((MapValue) bValue).get(fieldDescriptor.getName()));
                            size += computeMessageSize(fieldDescriptor, message);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());

                        size += com.google.protobuf.CodedOutputStream.computeEnumSize(fieldDescriptor
                                .getNumber(), fieldDescriptor.getEnumType().findValueByName
                                ((String) bValue).getNumber());
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeEnumSize(fieldDescriptor.getNumber(),
                                    fieldDescriptor.getEnumType().findValueByName
                                            ((String) ((MapValue) bValue).get(fieldDescriptor.getName())).getNumber());
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    if (bMapValue != null && bMapValue.containsKey(fieldDescriptor.getName())) {
                        Object bValue = bMapValue.get(fieldDescriptor.getName());
                        if (bValue instanceof ArrayValue) {
                            ArrayValue valueArray = (ArrayValue) bValue;
                            size += com.google.protobuf.CodedOutputStream
                                    .computeByteArraySize(fieldDescriptor.getNumber(), valueArray.getBytes());
                        }
                    } else if (isOneofField(bMapValue, fieldDescriptor)) {
                        Object bValue = getOneofFieldMap(bMapValue, fieldDescriptor);
                        if (hasOneofFieldValue(fieldDescriptor.getName(), bValue)) {
                            size += com.google.protobuf.CodedOutputStream.computeByteArraySize(
                                    fieldDescriptor.getNumber(),
                                    ((ArrayValue) ((MapValue) bValue).get(fieldDescriptor.getName())).getBytes());
                        }
                    } else if (bMessage instanceof ArrayValue) {
                        ArrayValue valueArray = (ArrayValue) bMessage;
                        size += com.google.protobuf.CodedOutputStream
                                .computeByteArraySize(fieldDescriptor.getNumber(), valueArray.getBytes());
                    }
                    break;
                }
                default: {
                    throw Status.Code.INTERNAL.toStatus().withDescription(
                            "Error while calculating the serialized type. Field type is not supported : "
                                    + fieldDescriptor.getType()).asRuntimeException();
                }
            }
        }
        memoizedSize = size;
        return size;
    }

    private boolean hasOneofFieldValue(String fieldName, Object bValue) {
        return (bValue instanceof MapValue) && ((MapValue) bValue).containsKey(fieldName);
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
        Message result = new MessageParser(fieldDescriptor.getMessageType().getName(), bType).parseFrom(in);
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
