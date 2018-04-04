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

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageLite;
import io.grpc.Metadata;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.util.HashMap;
import java.util.Map;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * Generic Proto3 Message.
 *
 * @since 1.0.0
 */
public class Message extends GeneratedMessageV3 {
    private static final long serialVersionUID = 0L;
    private Map<String, Object> fields = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();
    private String messageName;
    
    // Use Message.newBuilder() to construct.
    protected Message(Builder builder) {
        super(builder);
        this.messageName = builder.messageName;
        this.headers = retrieveHeaderMap();
    }
    
    protected Message(String messageName) {
        this.messageName = messageName;
        this.headers = retrieveHeaderMap();
    }
    
    private Map<String, Object> retrieveHeaderMap() {
        Map<String, Object> headerMap = new HashMap<>();
        if (MessageContext.isPresent()) {
            MessageContext messageContext = MessageContext.DATA_KEY.get();
            for (String keyValue : messageContext.keys()) {
                if (keyValue.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                    headerMap.put(keyValue, messageContext.get(Metadata.Key.of(keyValue, Metadata
                            .BINARY_BYTE_MARSHALLER)));
                } else {
                    headerMap.put(keyValue, messageContext.get(Metadata.Key.of(keyValue, ASCII_STRING_MARSHALLER)));
                }
            }
        }
        return headerMap;
    }
    
    public Map<String, Object> getHeaders() {
        return headers;
    }
    
    public Object getHeader(String headerName) {
        return headers.get(headerName);
    }
    
    void setFieldValues(Map<String, Object> fieldValues) {
        this.fields = fieldValues;
    }
    
    public Map<String, Object> getFields() {
        return fields;
    }
    
    @Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }
    
    public Message(
            String messageName,
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        this(messageName);
        
        Descriptors.Descriptor messageDescriptor = getDescriptor();
        Map<Integer, Descriptors.FieldDescriptor> fields = new HashMap<>();
        for (Descriptors.FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
            Descriptors.FieldDescriptor.Type fieldType = fieldDescriptor.getType();
            int number = fieldDescriptor.getNumber();
            int byteCode = ((number << 3) + MessageUtils.getFieldWireType(fieldType));
            fields.put(byteCode, fieldDescriptor);
        }
        
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
                if (tag == 0) {
                    done = true;
                } else if (fields.containsKey(tag)) {
                    Descriptors.FieldDescriptor fieldDescriptor = fields.get(tag);
                    String name = fieldDescriptor.getName();
                    switch (fieldDescriptor.getType().toProto().getNumber()) {
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                            double value = input.readDouble();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                            float value = input.readFloat();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                            long value = input.readInt64();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                            long value = input.readUInt64();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                            int value = input.readInt32();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                            long value = input.readFixed64();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                            int value = input.readFixed32();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                            boolean value = input.readBool();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                            String value = input.readStringRequireUtf8();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                            int value = input.readEnum();
                            this.fields.put(name, value);
                            break;
                        }
                        case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                            Message message = input.readMessage(new MessageParser(fieldDescriptor.getMessageType()
                                            .getName()), extensionRegistry);
                            this.fields.put(name, message);
                            break;
                        }
                        default: {
                            throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                                    "type is not supported : " + fieldDescriptor.getType());
                        }
                    }
                } else {
                    if (!parseUnknownFieldProto3(
                            input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                    }
                }
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(
                    e).setUnfinishedMessage(this);
        } finally {
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }
    
    public com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        return MessageRegistry.getInstance().getMessageDescriptor(messageName);
    }
    
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
        throw new UnsupportedOperationException("Operation is not supported");
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
                            com.google.protobuf.GeneratedMessageV3.writeString(output, fieldDescriptor.getNumber(),
                                    msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Object[] messages = (Object[]) msgObject;
                            for (Object message : messages) {
                                output.writeMessage(fieldDescriptor.getNumber(), (MessageLite) message);
                            }
                        } else {
                            output.writeMessage(fieldDescriptor.getNumber(), (MessageLite) msgObject);
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        output.writeEnum(fieldDescriptor.getNumber(), ((Descriptors.EnumValueDescriptor)
                                msgObject).getNumber());
                        break;
                    }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while writing output stream. Field " +
                                "type is not supported : " + fieldDescriptor.getType());
                    }
                }
                
            }
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) {
            return size;
        }
        
        size = 0;
        Descriptors.Descriptor messageDescriptor = getDescriptor();
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
                            size += com.google.protobuf.CodedOutputStream.computeFloatSize(fieldDescriptor.getNumber
                                    (), Float.parseFloat(fields.get(fieldDescriptor.getName()).toString()));
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
                                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(fieldDescriptor
                                        .getNumber(), message);
                            }
                        } else {
                            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(fieldDescriptor
                                    .getNumber(), fields.get(fieldDescriptor.getName()));
                        }
                        break;
                    }
                    case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                        Object msgObject = fields.get(fieldDescriptor.getName());
                        if (MessageUtils.isArray(msgObject)) {
                            Object[] messages = (Object[]) msgObject;
                            for (Object message : messages) {
                                size += com.google.protobuf.CodedOutputStream.computeMessageSize(fieldDescriptor
                                        .getNumber(), (MessageLite) message);
                            }
                        } else {
                            size += com.google.protobuf.CodedOutputStream.computeMessageSize(fieldDescriptor
                                    .getNumber(), (MessageLite) fields.get(fieldDescriptor.getName()));
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
                    default: {
                        throw new UnsupportedFieldTypeException("Error while calculating the serialized type. Field " +
                                "type is not supported : " + fieldDescriptor.getType());
                    }
                }
            }
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }
    
    public Builder newBuilderForType() {
        throw new UnsupportedOperationException("This method is not supported.");
    }
    
    public static Builder newBuilder(String messageName) {
        return new Message.Builder(messageName);
    }
    
    public Builder toBuilder() {
        throw new UnsupportedOperationException("This method is not supported.");
    }
    
    @Override
    protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        throw new UnsupportedOperationException("This method is not supported.");
    }
    
    /**
     * <pre>
     * The request message containing the user's name.
     * </pre>
     * <p>
     * Protobuf type {@code org.ballerinalang.net.grpc.Message}
     */
    public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> {
        
        private Map<String, Object> fields = new HashMap<>();
        private final String messageName;
        
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            throw new UnsupportedOperationException("Operation is not supported");
        }
        
        // Construct using org.ballerinalang.net.grpc.Message.newBuilder()
        private Builder(String messageName) {
            this.messageName = messageName;
        }
        
        private Descriptors.Descriptor getDescriptor() {
            return MessageRegistry.getInstance().getMessageDescriptor(messageName);
        }
        
        public Builder clear() {
            super.clear();
            fields.clear();
            
            return this;
        }
        
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return getDescriptor();
        }
        
        public Message getDefaultInstanceForType() {
            return new Message(messageName);
        }
        
        public Message build() {
            Message result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }
        
        public Message buildPartial() {
            Message result = new Message(this);
            if (!fields.isEmpty()) {
                result.setFieldValues(fields);
            }
            onBuilt();
            return result;
        }
        
        public Builder addField(String name, Object value) {
            fields.put(name, value);
            return this;
        }
        
        @Override
        public String toString() {
            return "Builder{" +
                    "fields=" + fields +
                    ", messageName='" + messageName + '\'' +
                    '}';
        }
    }
    
    @Override
    public com.google.protobuf.Parser<Message> getParserForType() {
        return new MessageParser(messageName);
    }
    
    public Message getDefaultInstanceForType() {
        throw new UnsupportedOperationException("Default instance is not supported.");
    }
    
}
