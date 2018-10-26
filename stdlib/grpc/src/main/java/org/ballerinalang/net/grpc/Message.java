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

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.WireFormat;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic Proto3 Message.
 *
 * @since 1.0.0
 */
public class Message {
    private Map<String, Object> fields = new HashMap<>();
    private String messageName;
    private int memoizedSize = -1;
    private HttpHeaders headers;

    private boolean isError = false;
    private Throwable error;

    protected Message(String messageName) {
        this.messageName = messageName;
    }

    protected void addField(String fieldName, Object value) {
        fields.put(fieldName, value);
    }
    
    public Map<String, Object> getFields() {
        return fields;
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

    public Message(Throwable error) {
        this.error = error;
        this.isError = true;
    }

    public Message(
            String messageName,
            com.google.protobuf.CodedInputStream input)
            throws IOException {
        this(messageName);
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        Map<Integer, Descriptors.FieldDescriptor> fieldDescriptors = new HashMap<>();
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            Descriptors.FieldDescriptor.Type fieldType = fieldDescriptor.getType();
            int number = fieldDescriptor.getNumber();
            int byteCode = ((number << 3) + MessageUtils.getFieldWireType(fieldType));
            fieldDescriptors.put(byteCode, fieldDescriptor);
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
                        if (fieldDescriptor.isRepeated()) {
                            List<Double> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Double>) this.fields.get(name);
                            }
                            messages.add(input.readDouble());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readDouble());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Float> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Float>) this.fields.get(name);
                            }
                            messages.add(input.readFloat());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readFloat());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Long> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Long>) this.fields.get(name);
                            }
                            messages.add(input.readInt64());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readInt64());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Long> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Long>) this.fields.get(name);
                            }
                            messages.add(input.readUInt64());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readUInt64());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Integer> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Integer>) this.fields.get(name);
                            }
                            messages.add(input.readInt32());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readInt32());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Long> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Long>) this.fields.get(name);
                            }
                            messages.add(input.readFixed64());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readFixed64());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Integer> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Integer>) this.fields.get(name);
                            }
                            messages.add(input.readFixed32());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readFixed32());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Boolean> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Boolean>) this.fields.get(name);
                            }
                            messages.add(input.readBool());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readBool());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<String> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<String>) this.fields.get(name);
                            }
                            messages.add(input.readStringRequireUtf8());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readStringRequireUtf8());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Integer> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Integer>) this.fields.get(name);
                            }
                            messages.add(input.readEnum());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name,
                                    fieldDescriptor.getEnumType().findValueByNumber(input.readEnum()).toString());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<byte[]> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<byte[]>) this.fields.get(name);
                            }
                            messages.add(input.readByteArray());
                            this.fields.put(name, messages);
                        } else {
                            this.fields.put(name, input.readByteArray());
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        if (fieldDescriptor.isRepeated()) {
                            List<Message> messages = new ArrayList<>();
                            if (this.fields.containsKey(name)) {
                                messages = (List<Message>) this.fields.get(name);
                            }
                            messages.add(readMessage(fieldDescriptor, input));
                            this.fields.put(name, messages);
                        } else {
                            Message message = readMessage(fieldDescriptor, input);
                            this.fields.put(name, message);
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
        StringBuilder payload = new StringBuilder("Message { fields: ");
        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                payload.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
            payload.append("}");
        } else {
            payload.append("null");
        }
        return payload.toString();
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            if (fields.containsKey(fieldDescriptor.getName())) {
                switch (fieldDescriptor.getType().toProto().getNumber()) {
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Double[] messages = (Double[]) msgObject;
                            for (Double message : messages) {
                                output.writeDouble(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeDouble(fieldDescriptor.getNumber(), (Double) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Float[] messages = (Float[]) msgObject;
                            for (Float message : messages) {
                                output.writeFloat(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeFloat(fieldDescriptor.getNumber(), (Float) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Long[] messages = (Long[]) msgObject;
                            for (Long message : messages) {
                                output.writeInt64(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeInt64(fieldDescriptor.getNumber(), (Long) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Long[] messages = (Long[]) msgObject;
                            for (Long message : messages) {
                                output.writeUInt64(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeUInt64(fieldDescriptor.getNumber(), (Long) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Integer[] messages = (Integer[]) msgObject;
                            for (Integer message : messages) {
                                output.writeInt32(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeInt32(fieldDescriptor.getNumber(), (Integer) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Long[] messages = (Long[]) msgObject;
                            for (Long message : messages) {
                                output.writeFixed64(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeFixed64(fieldDescriptor.getNumber(), (Long) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Integer[] messages = (Integer[]) msgObject;
                            for (Integer message : messages) {
                                output.writeFixed32(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeFixed32(fieldDescriptor.getNumber(), (Integer) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Boolean[] messages = (Boolean[]) msgObject;
                            for (Boolean message : messages) {
                                output.writeBool(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            output.writeBool(fieldDescriptor.getNumber(), (Boolean) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            String[] messages = (String[]) msgObject;
                            for (String message : messages) {
                                output.writeString(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            if (msgObject instanceof String) {
                                output.writeString(fieldDescriptor.getNumber(), (String) msgObject);
                            } else {
                                output.writeBytes(fieldDescriptor.getNumber(), (ByteString) msgObject);
                            }
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Message[] messages = (Message[]) msgObject;
                            for (Message message : messages) {
                                output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                                output.writeUInt32NoTag(message.getSerializedSize());
                                message.writeTo(output);
                            }
                        } else {
                            output.writeTag(fieldDescriptor.getNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
                            output.writeUInt32NoTag(((Message) msgObject).getSerializedSize());
                            ((Message) msgObject).writeTo(output);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        output.writeEnum(fieldDescriptor.getNumber(), ((Descriptors.EnumValueDescriptor)
                                msgObject).getNumber());
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        byte[] messages = (byte[]) msgObject;
                        output.writeByteArray(fieldDescriptor.getNumber(), messages);
                        break;
                    }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while writing output stream. Field " +
                                "type is not supported : " + fieldDescriptor.getType());
                    }
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
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        if (messageDescriptor == null) {
            throw Status.Code.INTERNAL.toStatus()
                    .withDescription("Error while processing the message, Couldn't find message descriptor.")
                    .asRuntimeException();
        }
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            if (fields.containsKey(fieldDescriptor.getName())) {
                switch (fieldDescriptor.getType().toProto().getNumber()) {
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Double[] messages = (Double[]) msgObject;
                            for (Double message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeDoubleSize(fieldDescriptor.getNumber(),
                                    (Double) fields.get(fieldDescriptor.getName()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Float[] messages = (Float[]) msgObject;
                            for (Float message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFloatSize(
                                    fieldDescriptor.getNumber(),
                                    Float.parseFloat(fields.get(fieldDescriptor.getName()).toString()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Long[] messages = (Long[]) msgObject;
                            for (Long message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt64Size(fieldDescriptor.getNumber
                                    (), (Long) fields.get(fieldDescriptor.getName()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Long[] messages = (Long[]) msgObject;
                            for (Long message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeUInt64Size(fieldDescriptor.getNumber
                                    (), (Long) fields.get(fieldDescriptor.getName()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Integer[] messages = (Integer[]) msgObject;
                            for (Integer message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeInt32Size(fieldDescriptor.getNumber
                                    (), Integer.parseInt(fields.get(fieldDescriptor.getName()).toString()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Long[] messages = (Long[]) msgObject;
                            for (Long message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed64Size(fieldDescriptor
                                    .getNumber(), (Long) fields.get(fieldDescriptor.getName()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Integer[] messages = (Integer[]) msgObject;
                            for (Integer message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeFixed32Size(fieldDescriptor
                                    .getNumber(), Integer.parseInt(fields.get(fieldDescriptor.getName()).toString()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Boolean[] messages = (Boolean[]) msgObject;
                            for (Boolean message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeBoolSize(fieldDescriptor.getNumber()
                                    , (Boolean) fields.get(fieldDescriptor.getName()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            String[] messages = (String[]) msgObject;
                            for (String message : messages) {
                                    size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), message);
                            }
                        } else {
                            if (msgObject instanceof String) {
                                size += CodedOutputStream.computeStringSize(fieldDescriptor.getNumber(), (String)
                                        msgObject);
                            } else {
                                size += CodedOutputStream.computeBytesSize(fieldDescriptor.getNumber(), (ByteString)
                                        msgObject);
                            }
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Message[] messages = (Message[]) msgObject;
                            for (Message message : messages) {
                                size += computeMessageSize(fieldDescriptor, message);
                            }
                        } else {
                            size += computeMessageSize(fieldDescriptor, (Message) fields.get(fieldDescriptor.getName
                                    ()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Descriptors.EnumValueDescriptor[] messages = (Descriptors.EnumValueDescriptor[]) msgObject;
                            for (Descriptors.EnumValueDescriptor message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeEnumSize(fieldDescriptor
                                        .getNumber(), message.getNumber());
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeEnumSize(fieldDescriptor
                                    .getNumber(), fieldDescriptor
                                    .getNumber());
                        }
                        break;
                    }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    Object msgObject = fields.get(fieldDescriptor.getName());
                    if (MessageUtils.isArray(msgObject)) {
                        byte[] messages = (byte[]) msgObject;
                        size += com.google.protobuf.CodedOutputStream
                                .computeByteArraySize(fieldDescriptor.getNumber(), messages);
                    } else {
                        size += com.google.protobuf.CodedOutputStream.computeByteArraySize(fieldDescriptor.getNumber(),
                                (byte[]) fields.get(fieldDescriptor.getName()));
                    }
                    break;
                }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while calculating the serialized type. Field " +
                                "type is not supported : " + fieldDescriptor.getType());
                    }
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

    public MessageParser getParserForType() {
        return new MessageParser(messageName);
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


    private Message readMessage(final Descriptors.FieldDescriptor fieldDescriptor, final CodedInputStream in)
            throws IOException {
        int length = in.readRawVarint32();
        final int oldLimit = in.pushLimit(length);
        Message result = new MessageParser(fieldDescriptor.getMessageType().getName()).parseFrom(in);
        in.popLimit(oldLimit);
        return result;
    }
}
