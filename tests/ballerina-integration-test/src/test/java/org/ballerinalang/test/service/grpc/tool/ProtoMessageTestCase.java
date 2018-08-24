/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.service.grpc.tool;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.ProtoUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for Proto message.
 *
 * @since 0.982.0
 */
public class ProtoMessageTestCase {

    private File compilerFile;

    @BeforeClass
    private void setup() throws Exception {
        compilerFile = ProtoDescriptorUtils.getProtocCompiler();
        Path resourceDir = Paths.get(new File(ProtoMessageTestCase.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI().getPath()).getAbsolutePath());
        Path protoPath = resourceDir.resolve(Paths.get("grpc", "tool", "testMessage.proto"));
        //read message descriptor from proto file.
        readMessageDescriptor(protoPath);
    }

    @Test(description = "Test case for parsing proto message with string field")
    public void testStringTypeProtoMessage() throws Exception {
        // convert message to byte array.
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("name", "John");
        Message message = createMessageInstance("Test1");
        message = addMessageFieldValues(message, fieldsMap);
        Assert.assertEquals(message.getSerializedSize(), 6);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(createMessageInstance("Test1")).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
        Assert.assertFalse(message1.isError());
    }

    @Test(description = "Test case for parsing proto message with primitive field")
    public void testPrimitiveTypeProtoMessage() throws Exception {
        // convert message to byte array.
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("a", "John");
        fieldsMap.put("b", 1.2);
        fieldsMap.put("c", 2.5F);
        fieldsMap.put("d", 1);
        fieldsMap.put("e", 2L);
        fieldsMap.put("f", 3L);
        fieldsMap.put("g", 4);
        fieldsMap.put("h", 5L);
        Message message = createMessageInstance("Test2");
        message = addMessageFieldValues(message, fieldsMap);
        Assert.assertEquals(message.getSerializedSize(), 40);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(createMessageInstance("Test2")).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
        Assert.assertFalse(message1.isError());
    }

    @Test(description = "Test case for parsing proto message with array field")
    public void testArrayFieldTypeProtoMessage() throws Exception {
        // convert message to byte array.
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("a", new String[] {"John"});
        fieldsMap.put("b", new Double[]{1.2});
        fieldsMap.put("c", new Float[] {2.5F});
        fieldsMap.put("d", new Integer[]{1});
        fieldsMap.put("e", new Long[]{2L});
        fieldsMap.put("f", new Long[]{3L});
        fieldsMap.put("g", new Integer[]{4});
        fieldsMap.put("h", new Long[]{5L});
        Message message = createMessageInstance("Test3");
        message = addMessageFieldValues(message, fieldsMap);
        Assert.assertEquals(message.getSerializedSize(), 40);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(createMessageInstance("Test3")).parse(messageStream);
        Assert.assertEquals(message1.getFields().size(), message.getFields().size());
        Assert.assertFalse(message1.isError());
    }

    private void readMessageDescriptor(Path protoPath) throws IOException, Descriptors.DescriptorValidationException {
        DescriptorProtos.FileDescriptorSet descriptorSet = ProtoDescriptorUtils.getProtoFileDescriptor(compilerFile,
                protoPath.toString());
        DescriptorProtos.FileDescriptorProto fileDescriptorProto = descriptorSet.getFile(0);
        Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new
                Descriptors.FileDescriptor[0]);
        Assert.assertEquals(fileDescriptor.getMessageTypes().size(), 3);
        MessageRegistry messageRegistry = MessageRegistry.getInstance();
        for (Descriptors.Descriptor messageDescriptor : fileDescriptor.getMessageTypes()) {
            messageRegistry.addMessageDescriptor(messageDescriptor.getName(), messageDescriptor);
        }
    }

    private Message createMessageInstance(String messageName) throws Exception {
        Constructor<Message> constructor = Message.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(messageName);
    }

    private Message addMessageFieldValues(Message message, Map<String, Object> fieldValues) throws
            NoSuchMethodException {
        Method addFieldMethod = Message.class.getDeclaredMethod("addField", String.class, Object.class);
        addFieldMethod.setAccessible(true);
        fieldValues.forEach((key, value) -> {
            try {
                addFieldMethod.invoke(message, key, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error while adding field values to message object.");
            }
        });
        return message;
    }

    @AfterClass
    private void clear() throws Exception {
        Files.deleteIfExists(compilerFile.toPath());
    }
}
