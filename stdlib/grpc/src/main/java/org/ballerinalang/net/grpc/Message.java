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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.ballerinalang.util.codegen.ProgramFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic Proto3 Message.
 *
 * @since 1.0.0
 */
public class Message {
    private String messageName;
    private int memoizedSize = -1;
    private HttpHeaders headers;
    private BValue bMessage = null;

    private boolean isError = false;
    private Throwable error;

    public Message(String messageName, BValue bMessage) {
        this.messageName = messageName;
        this.bMessage = bMessage;
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

    public BValue getbMessage() {
        return bMessage;
    }

    public Message(Throwable error) {
        this.error = error;
        this.isError = true;
    }

    public Message(
            String messageName,
            ProgramFile programFile,
            BType bType,
            com.google.protobuf.CodedInputStream input,
            Map<Integer, Descriptors.FieldDescriptor> fieldDescriptors)
            throws IOException {
        this(messageName);
        BMap<String, BValue> bMapValue = null;
        Map<String, BType> bMapFields = new HashMap<>();
        if (bType instanceof BRecordType) {
            bMapValue = BLangConnectorSPIUtil.createBStruct(programFile, bType.getPackagePath(), bType.getName());
            for (BField messageField : ((BRecordType) bType).getFields().values()) {
                bMapFields.put(messageField.fieldName, messageField.fieldType);
            }
            bMessage = bMapValue;
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
                                BFloatArray floatArray = new BFloatArray();
                                if (bMapValue.hasKey(name)) {
                                    floatArray = (BFloatArray) bMapValue.get(name);
                                }
                                floatArray.add(floatArray.size(), input.readDouble());
                            } else {
                                bMapValue.put(name, new BFloat(input.readDouble()));
                            }
                        } else {
                            bMessage = new BFloat(input.readDouble());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BFloatArray floatArray = new BFloatArray();
                                if (bMapValue.hasKey(name)) {
                                    floatArray = (BFloatArray) bMapValue.get(name);
                                }
                                floatArray.add(floatArray.size(), Double.parseDouble(String.valueOf(
                                        input.readFloat())));
                            } else {
                                bMapValue.put(name, new BFloat(Double.parseDouble(String.valueOf(input.readFloat()))));
                            }
                        } else {
                            bMessage = new BFloat(Double.parseDouble(String.valueOf(input.readFloat())));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BIntArray intArray = new BIntArray();
                                if (bMapValue.hasKey(name)) {
                                    intArray = (BIntArray) bMapValue.get(name);
                                }
                                intArray.add(intArray.size(), input.readInt64());
                            } else {
                                bMapValue.put(name, new BInteger(input.readInt64()));
                            }
                        } else {
                            bMessage = new BInteger(input.readInt64());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BIntArray intArray = new BIntArray();
                                if (bMapValue.hasKey(name)) {
                                    intArray = (BIntArray) bMapValue.get(name);
                                }
                                intArray.add(intArray.size(), input.readUInt64());
                            } else {
                                bMapValue.put(name, new BInteger(input.readUInt64()));
                            }
                        } else {
                            bMessage = new BInteger(input.readUInt64());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BIntArray intArray = new BIntArray();
                                if (bMapValue.hasKey(name)) {
                                    intArray = (BIntArray) bMapValue.get(name);
                                }
                                intArray.add(intArray.size(), input.readInt32());
                            } else {
                                bMapValue.put(name, new BInteger(input.readInt32()));
                            }
                        } else {
                            bMessage = new BInteger(input.readInt32());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BIntArray intArray = new BIntArray();
                                if (bMapValue.hasKey(name)) {
                                    intArray = (BIntArray) bMapValue.get(name);
                                }
                                intArray.add(intArray.size(), input.readFixed64());
                            } else {
                                bMapValue.put(name, new BInteger(input.readFixed64()));
                            }
                        } else {
                            bMessage = new BInteger(input.readFixed64());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BIntArray intArray = new BIntArray();
                                if (bMapValue.hasKey(name)) {
                                    intArray = (BIntArray) bMapValue.get(name);
                                }
                                intArray.add(intArray.size(), input.readFixed32());
                            } else {
                                bMapValue.put(name, new BInteger(input.readFixed32()));
                            }
                        } else {
                            bMessage = new BInteger(input.readFixed32());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BBooleanArray booleanArray = new BBooleanArray();
                                if (bMapValue.hasKey(name)) {
                                    booleanArray = (BBooleanArray) bMapValue.get(name);
                                }
                                booleanArray.add(booleanArray.size(), input.readBool() ? 1 : 0);
                            } else {
                                bMapValue.put(name, new BBoolean(input.readBool()));
                            }
                        } else {
                            bMessage = new BBoolean(input.readBool());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BStringArray stringArray = new BStringArray();
                                if (bMapValue.hasKey(name)) {
                                    stringArray = (BStringArray) bMapValue.get(name);
                                }
                                stringArray.add(stringArray.size(), input.readStringRequireUtf8());
                                bMapValue.put(name, stringArray);
                            } else {
                                bMapValue.put(name, new BString(input.readStringRequireUtf8()));
                            }
                        } else {
                            bMessage = new BString(input.readStringRequireUtf8());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BStringArray stringArray = new BStringArray();
                                if (bMapValue.hasKey(name)) {
                                    stringArray = (BStringArray) bMapValue.get(name);
                                }
                                stringArray.add(stringArray.size(), fieldDescriptor.getEnumType().findValueByNumber
                                        (input.readEnum()).toString());
                                bMapValue.put(name, stringArray);
                            } else {
                                bMapValue.put(name, new BString(fieldDescriptor.getEnumType().findValueByNumber(input
                                        .readEnum()).toString()));
                            }
                        } else {
                            bMessage = new BString(fieldDescriptor.getEnumType().findValueByNumber(input
                                    .readEnum()).toString());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                        if (bMapValue != null) {
                            bMapValue.put(name, new BByteArray(input.readByteArray()));
                        } else {
                            bMessage = new BByteArray(input.readByteArray());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        if (bMapValue != null) {
                            if (fieldDescriptor.isRepeated()) {
                                BRefValueArray structArray = bMapValue.get(name) != null ?
                                        (BRefValueArray) bMapValue.get(name) : null;
                                if (structArray == null || structArray.size() == 0) {
                                    structArray = new BRefValueArray(((BArrayType) bMapFields.get(name))
                                            .getElementType());
                                    bMapValue.put(name, structArray);
                                }

                                structArray.add(structArray.size(), (BRefType) readMessage(fieldDescriptor, programFile,
                                        ((BArrayType) bMapFields.get(name)).getElementType(), input).bMessage);
                                bMapValue.put(name, structArray);
                            } else {
                                bMapValue.put(name, readMessage(fieldDescriptor, programFile,
                                        bMapFields.get(name), input).bMessage);
                            }
                        } else {
                            bMessage = readMessage(fieldDescriptor, programFile, bMapFields.get(name), input).bMessage;
                        }
                        break;
                    }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                                "type is not supported : " + fieldDescriptor.getType());
                    }
                }
            }
        }
    }
    
    public com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        return MessageRegistry.getInstance().getMessageDescriptor(messageName);
    }
    
    private byte memoizedIsInitialized = -1;
    
    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) {
            return true;
        }
        if (isInitialized == 0) {
            return false;
        }
        memoizedIsInitialized = 1;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder payload = new StringBuilder("Message : ");
        if (bMessage != null) {
            payload.append("{ ").append(bMessage.stringValue()).append(" }");
        } else {
            payload.append("null");
        }
        return payload.toString();
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
        if (bMessage == null) {
            return;
        }
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        BMap<String, BValue> bMapValue = null;
        if (TypeTags.RECORD_TYPE_TAG == bMessage.getType().getTag()) {
            bMapValue = (BMap<String, BValue>) bMessage;
        }
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BFloatArray valueArray = (BFloatArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeDouble(fieldDescriptor.getNumber(), valueArray.get(i));
                            }
                        } else {
                            output.writeDouble(fieldDescriptor.getNumber(), ((BFloat) bValue).value());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.FLOAT_TAG) {
                        output.writeDouble(fieldDescriptor.getNumber(), ((BFloat) bMessage).value());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BFloatArray valueArray = (BFloatArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                        (valueArray.get(i))));
                            }
                        } else {
                            output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                    (((BFloat) bValue).value())));
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.FLOAT_TAG) {
                        output.writeFloat(fieldDescriptor.getNumber(), Float.parseFloat(String.valueOf
                                (((BFloat) bMessage).value())));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeInt64(fieldDescriptor.getNumber(), valueArray.get(i));
                            }
                        } else {
                            output.writeInt64(fieldDescriptor.getNumber(), ((BInteger) bValue).value());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        output.writeInt64(fieldDescriptor.getNumber(), ((BInteger) bMessage).value());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeUInt64(fieldDescriptor.getNumber(), valueArray.get(i));
                            }
                        } else {
                            output.writeUInt64(fieldDescriptor.getNumber(), ((BInteger) bValue).value());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        output.writeUInt64(fieldDescriptor.getNumber(), ((BInteger) bMessage).value());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeInt32(fieldDescriptor.getNumber(), Integer.parseInt(String.valueOf
                                        (valueArray.get(i))));
                            }
                        } else {
                            output.writeInt32(fieldDescriptor.getNumber(), Integer.parseInt(String.valueOf((
                                    (BInteger) bValue).value())));
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        output.writeInt32(fieldDescriptor.getNumber(), Integer.parseInt(String.valueOf(((BInteger)
                                bMessage).value())));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFixed64(fieldDescriptor.getNumber(), valueArray.get(i));
                            }
                        } else {
                            output.writeFixed64(fieldDescriptor.getNumber(), ((BInteger) bValue).value());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        output.writeFixed64(fieldDescriptor.getNumber(), ((BInteger) bMessage).value());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeFixed32(fieldDescriptor.getNumber(), Integer.parseInt(String.valueOf
                                        (valueArray.get(i))));
                            }
                        } else {
                            output.writeFixed32(fieldDescriptor.getNumber(), Integer.parseInt(String.valueOf((
                                    (BInteger) bValue).value())));
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        output.writeFixed32(fieldDescriptor.getNumber(), Integer.parseInt(String.valueOf(((BInteger)
                                bMessage).value())));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BBooleanArray valueArray = (BBooleanArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeBool(fieldDescriptor.getNumber(), valueArray.get(i) != 0);
                            }
                        } else {
                            output.writeBool(fieldDescriptor.getNumber(), ((BBoolean) bValue).booleanValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.BOOLEAN_TAG) {
                        output.writeBool(fieldDescriptor.getNumber(), ((BBoolean) bMessage).booleanValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BStringArray valueArray = (BStringArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                output.writeString(fieldDescriptor.getNumber(), valueArray.get(i));
                            }
                        } else {
                            output.writeString(fieldDescriptor.getNumber(), bValue.stringValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.STRING_TAG) {
                        output.writeString(fieldDescriptor.getNumber(), bMessage.stringValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BRefValueArray valueArray = (BRefValueArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                BValue value = valueArray.get(i);
                                Message message = new Message(fieldDescriptor.getMessageType().getName(), value);
                                output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                                output.writeUInt32NoTag(message.getSerializedSize());
                                message.writeTo(output);
                            }
                        } else {
                            Message message = new Message(fieldDescriptor.getMessageType().getName(), bValue);
                            output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                            output.writeUInt32NoTag(message.getSerializedSize());
                            message.writeTo(output);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.get(fieldDescriptor.getName());
                        output.writeEnum(fieldDescriptor.getNumber(), fieldDescriptor.getEnumType().findValueByName
                                (bValue.stringValue()).getNumber());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BByteArray valueArray = (BByteArray) bValue;
                            output.writeByteArray(fieldDescriptor.getNumber(), valueArray.getBytes());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.ARRAY_TAG) {
                        BByteArray valueArray = (BByteArray) bMessage;
                        output.writeByteArray(fieldDescriptor.getNumber(), valueArray.getBytes());
                    }
                    break;
                }
                default: {
                    throw new UnsupportedFieldTypeException("Error while writing output stream. Field " +
                            "type is not supported : " + fieldDescriptor.getType());
                }
            }
        }
    }

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
        BMap<String, BValue> bMapValue = null;
        if (TypeTags.RECORD_TYPE_TAG == bMessage.getType().getTag()) {
            bMapValue = (BMap<String, BValue>) bMessage;
        }

        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BFloatArray valueArray = (BFloatArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor
                                        .getNumber(), valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                    ((BFloat) bValue).floatValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.FLOAT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                ((BFloat) bMessage).floatValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BFloatArray valueArray = (BFloatArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                        .getNumber(), (float) valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                    .getNumber(), (float) ((BFloat) bValue).floatValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.FLOAT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                .getNumber(), (float) ((BFloat) bMessage).floatValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                        .getNumber(), valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                    .getNumber(), ((BInteger) bValue).intValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                .getNumber(), ((BInteger) bMessage).intValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                        .getNumber(), valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                    .getNumber(), ((BInteger) bValue).intValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                .getNumber(), ((BInteger) bMessage).intValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                        .getNumber(), (int) valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                    .getNumber(), (int) ((BInteger) bValue).intValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                .getNumber(), (int) ((BInteger) bMessage).intValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                        .getNumber(), valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                    .getNumber(), ((BInteger) bValue).intValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                .getNumber(), ((BInteger) bMessage).intValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BIntArray valueArray = (BIntArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                        .getNumber(), (int) valueArray.get(i));
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                    .getNumber(), (int) ((BInteger) bValue).intValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.INT_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                .getNumber(), (int) ((BInteger) bMessage).intValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BBooleanArray valueArray = (BBooleanArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                        .getNumber(), valueArray.get(i) != 0);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                    .getNumber(), ((BBoolean) bValue).booleanValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.BOOLEAN_TAG) {
                        size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                .getNumber(), ((BBoolean) bMessage).booleanValue());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BStringArray valueArray = (BStringArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), valueArray
                                        .get(i));
                            }
                        } else {
                            size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), bValue
                                    .stringValue());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.STRING_TAG) {
                        size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), bMessage.stringValue
                                ());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BRefValueArray valueArray = (BRefValueArray) bValue;
                            for (int i = 0; i < valueArray.size(); i++) {
                                BValue value = valueArray.get(i);
                                Message message = new Message(fieldDescriptor.getMessageType().getName(), value);
                                size += computeMessageSize(fieldDescriptor, message);
                            }
                        } else {
                            Message message = new Message(fieldDescriptor.getMessageType().getName(), bValue);
                            size += computeMessageSize(fieldDescriptor, message);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.get(fieldDescriptor.getName());

                        size += com.google.protobuf.CodedOutputStream.computeEnumSize(fieldDescriptor
                                .getNumber(), fieldDescriptor.getEnumType().findValueByName
                                (bValue.stringValue()).getNumber());
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    if (bMapValue != null && bMapValue.hasKey(fieldDescriptor.getName())) {
                        BValue bValue = bMapValue.getIfExist(fieldDescriptor.getName());
                        if (bValue.getType().getTag() == TypeTags.ARRAY_TAG) {
                            BByteArray valueArray = (BByteArray) bValue;
                            size += com.google.protobuf.CodedOutputStream
                                    .computeByteArraySize(fieldDescriptor.getNumber(), valueArray.getBytes());
                        }
                    } else if (bMessage.getType().getTag() == TypeTags.ARRAY_TAG) {
                        BByteArray valueArray = (BByteArray) bMessage;
                        size += com.google.protobuf.CodedOutputStream
                                .computeByteArraySize(fieldDescriptor.getNumber(), valueArray.getBytes());
                    }
                    break;
                }
                default: {
                    throw new UnsupportedFieldTypeException("Error while calculating the serialized type. Field " +
                            "type is not supported : " + fieldDescriptor.getType());
                }
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


    private Message readMessage(final Descriptors.FieldDescriptor fieldDescriptor, final ProgramFile programFile,
                                final BType bType, final CodedInputStream in) throws IOException {
        int length = in.readRawVarint32();
        final int oldLimit = in.pushLimit(length);
        Message result = new MessageParser(fieldDescriptor.getMessageType().getName(), programFile, bType).parseFrom
                (in);
        in.popLimit(oldLimit);
        return result;
    }
}
