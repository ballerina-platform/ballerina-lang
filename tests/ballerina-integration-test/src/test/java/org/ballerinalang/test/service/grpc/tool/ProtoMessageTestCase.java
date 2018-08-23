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
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.ProtoUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for Proto message.
 */
public class ProtoMessageTestCase {

    private File compilerFile;
    private Path resourceDir;

    @BeforeClass
    private void setup() throws Exception {
        compilerFile = ProtoDescriptorUtils.getProtocCompiler();
        resourceDir = Paths.get(new File(ProtoMessageTestCase.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI().getPath()).getAbsolutePath());
        Path protoPath = resourceDir.resolve(Paths.get("grpc", "tool", "sampleMessage.proto"));
        //read message descriptor from proto file.
        readMessageDescriptor(protoPath);
    }

    @Test(description = "Test case for parsing proto message to byte array")
    public void testParseProtoMessage() throws Exception {
        // convert message to byte array.
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("name", "John");
        Message message = createMessageInstance("HelloRequest");
        message = addMessageFieldValues(message, fieldsMap);
        Assert.assertEquals(message.getSerializedSize(), 6);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(createMessageInstance("HelloRequest")).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
    }

    private void readMessageDescriptor(Path protoPath) throws IOException {
        DescriptorProtos.FileDescriptorSet descriptorSet = ProtoDescriptorUtils.getProtoFileDescriptor(compilerFile,
                protoPath.toString());
        DescriptorProtos.FileDescriptorProto fileDescriptor = descriptorSet.getFile(0);
        Assert.assertEquals(fileDescriptor.getMessageTypeCount(), 1);
        MessageRegistry messageRegistry = MessageRegistry.getInstance();
        messageRegistry.addMessageDescriptor(fileDescriptor.getMessageType(0).getName(),
                fileDescriptor.getMessageType(0).getDescriptorForType());
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
}
