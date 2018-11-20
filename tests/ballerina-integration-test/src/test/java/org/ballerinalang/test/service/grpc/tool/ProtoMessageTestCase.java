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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageParser;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for Proto message.
 *
 * @since 0.982.0
 */
public class ProtoMessageTestCase {

    private File compilerFile;
    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        compilerFile = ProtoDescriptorUtils.getProtocCompiler();
        Path resourceDir = Paths.get(new File(ProtoMessageTestCase.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI().getPath()).getAbsolutePath());
        Path protoPath = resourceDir.resolve(Paths.get("grpc", "tool", "testMessage.proto"));
        //read message descriptor from proto file.
        readMessageDescriptor(protoPath);

        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "tool", "testMessage_pb.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test case for parsing proto message with string field")
    public void testStringTypeProtoMessage() {
        // convert message to byte array.
        BStructureType structureType = result.getProgFile().getEntryPackage().getStructInfo("Test1").getType();
        BMap<String, BValue> bMapValue = BLangConnectorSPIUtil.createBStruct(result.getProgFile(), structureType
                .getPackagePath(), structureType.getName());
        bMapValue.put("name", new BString("John"));
        Message message = new Message("Test1", bMapValue);
        Assert.assertEquals(message.getSerializedSize(), 6);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(new MessageParser("Test1", result.getProgFile(),
                structureType)).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
        Assert.assertFalse(message1.isError());
    }

    @Test(description = "Test case for parsing proto message with primitive field")
    public void testPrimitiveTypeProtoMessage() throws Exception {
        // convert message to byte array.
        BStructureType structureType = result.getProgFile().getEntryPackage().getStructInfo("Test2").getType();
        BMap<String, BValue> bMapValue = BLangConnectorSPIUtil.createBStruct(result.getProgFile(), structureType
                .getPackagePath(), structureType.getName());
        bMapValue.put("a", new BString("John"));
        bMapValue.put("b", new BFloat(1.2D));
        bMapValue.put("c", new BFloat(2.5F));
        bMapValue.put("d", new BInteger(1));
        bMapValue.put("e", new BInteger(2L));
        bMapValue.put("f", new BInteger(3L));
        bMapValue.put("g", new BInteger(4));
        bMapValue.put("h", new BInteger(5L));

        Message message = new Message("Test2", bMapValue);
        Assert.assertEquals(message.getSerializedSize(), 40);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(new MessageParser("Test2", result.getProgFile(),
                structureType)).parse(messageStream);
        Assert.assertEquals(message1.toString(), message.toString());
        Assert.assertFalse(message1.isError());
    }

    @Test(description = "Test case for parsing proto message with array field")
    public void testArrayFieldTypeProtoMessage() throws Exception {
        // convert message to byte array.
        BStructureType structureType = result.getProgFile().getEntryPackage().getStructInfo("Test3").getType();
        BMap<String, BValue> bMapValue = BLangConnectorSPIUtil.createBStruct(result.getProgFile(), structureType
                .getPackagePath(), structureType.getName());
        bMapValue.put("a", new BStringArray(new String[]{"John"}));
        bMapValue.put("b", new BFloatArray(new double[]{1.2}));
        bMapValue.put("c", new BFloatArray(new double[]{2.5F}));
        bMapValue.put("d", new BIntArray(new long[]{1}));
        bMapValue.put("e", new BIntArray(new long[]{2L}));
        bMapValue.put("f", new BIntArray(new long[]{3L}));
        bMapValue.put("g", new BIntArray(new long[]{4}));
        bMapValue.put("h", new BIntArray(new long[]{5L}));
        Message message = new Message("Test3", bMapValue);
        Assert.assertEquals(message.getSerializedSize(), 40);
        byte[] msgArray = message.toByteArray();
        //convert byte array back to message object.
        InputStream messageStream = new ByteArrayInputStream(msgArray);
        Message message1 = ProtoUtils.marshaller(new MessageParser("Test3", result.getProgFile(), structureType)).parse
                (messageStream);
        Assert.assertEquals(((BMap<String, BValue>) message1.getbMessage()).getMap().size(), bMapValue.size());
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

    @AfterClass
    private void clear() throws Exception {
        Files.deleteIfExists(compilerFile.toPath());
    }
}
