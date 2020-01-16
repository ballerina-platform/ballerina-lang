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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageParser;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.ProtoUtils;
import org.ballerinalang.protobuf.exception.CodeGeneratorException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.jvm.util.BLangConstants.ANON_ORG;
import static org.ballerinalang.jvm.util.BLangConstants.DOT;

/**
 * Test class for Proto message.
 *
 * @since 0.982.0
 */
public class ProtoMessageTestCase {

    private File compilerFile;
    private CompileResult result;
    private BPackage defaultPkg = new BPackage(ANON_ORG, DOT);

    @BeforeClass
    private void setup() throws Exception {
        compilerFile = ProtoDescriptorUtils.getProtocCompiler();
        Path resourceDir = Paths.get("src", "test", "resources").toAbsolutePath();
        Path protoPath = resourceDir.resolve(Paths.get("grpc", "src", "tool", "testMessage.proto"));
        //read message descriptor from proto file.
        readMessageDescriptor(protoPath);

        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "tool", "testMessage_pb.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test case for parsing proto message with string field")
    public void testStringTypeProtoMessage() {
        // convert message to byte array.
        MapValue<String, Object> bMapValue = BallerinaValues.createRecordValue(defaultPkg, "Test1");
        bMapValue.put("name", "John");
        Message message = new Message("Test1", bMapValue);
        Assert.assertEquals(message.getSerializedSize(), 6);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(new MessageParser("Test1", new BRecordType("Test1",
                new BPackage(null, null, null), 0, false, 0))).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
        Assert.assertFalse(message1.isError());
    }

    @Test(description = "Test case for parsing proto message with primitive field")
    public void testPrimitiveTypeProtoMessage() {
        // convert message to byte array.
        MapValue<String, Object> bMapValue = BallerinaValues.createRecordValue(defaultPkg, "Test2");
        bMapValue.put("a", "John");
        bMapValue.put("b", 1.2D);
        bMapValue.put("c", 2.5D);
        bMapValue.put("d", 1);
        bMapValue.put("e", 2L);
        bMapValue.put("f", 3L);
        bMapValue.put("g", 4);
        bMapValue.put("h", 5L);

        Message message = new Message("Test2", bMapValue);
        Assert.assertEquals(message.getSerializedSize(), 40);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(new MessageParser("Test2", new BRecordType("Test2",
                new BPackage(null, null, null), 0, false, 0))).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
        Assert.assertFalse(message1.isError());
    }

    @Test(description = "Test case for parsing proto message with array field")
    public void testArrayFieldTypeProtoMessage() {
        // convert message to byte array.
        MapValue<String, Object> bMapValue = BallerinaValues.createRecordValue(defaultPkg, "Test3");
        bMapValue.put("a", new ArrayValueImpl(new String[]{"John"}));
        bMapValue.put("b", new ArrayValueImpl(new double[]{1.2}));
        bMapValue.put("c", new ArrayValueImpl(new double[]{2.5F}));
        bMapValue.put("d", new ArrayValueImpl(new long[]{1}));
        bMapValue.put("e", new ArrayValueImpl(new long[]{2L}));
        bMapValue.put("f", new ArrayValueImpl(new long[]{3L}));
        bMapValue.put("g", new ArrayValueImpl(new long[]{4}));
        bMapValue.put("h", new ArrayValueImpl(new long[]{5L}));
        Message message = new Message("Test3", bMapValue);
        Assert.assertEquals(message.getSerializedSize(), 40);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(new MessageParser("Test3", new BRecordType("Test3",
                new BPackage(null, null, null), 0, false, 0))).parse(messageStream);
        Assert.assertEquals(((MapValue<String, Object>) message1.getbMessage()).size(), bMapValue.size());
        Assert.assertFalse(message1.isError());
    }

    private void readMessageDescriptor(Path protoPath)
            throws IOException, Descriptors.DescriptorValidationException, CodeGeneratorException {
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

    @AfterClass
    private void clear() throws Exception {
        Files.deleteIfExists(compilerFile.toPath());
    }
}
