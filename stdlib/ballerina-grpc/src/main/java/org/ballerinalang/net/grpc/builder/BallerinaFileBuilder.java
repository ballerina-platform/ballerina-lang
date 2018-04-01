/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.grpc.builder;

import com.google.protobuf.DescriptorProtos;
import io.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.builder.components.ActionBuilder;
import org.ballerinalang.net.grpc.builder.components.ClientBuilder;
import org.ballerinalang.net.grpc.builder.components.ClientStruct;
import org.ballerinalang.net.grpc.builder.components.DescriptorBuilder;
import org.ballerinalang.net.grpc.builder.components.StubBuilder;
import org.ballerinalang.net.grpc.builder.utils.BalGenConstants;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.EMPTY_DATA_TYPE;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.EMPTY_STRING;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR_REGEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SERVICE_INDEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.STUB_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.DEFAULT_SAMPLE_DIR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.DEFAULT_SKELETON_DIR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.SAMPLE_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.SKELETON_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getMappingBalType;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getTypeName;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.writeBallerina;

/**
 * Class is responsible of generating the ballerina stub which is mapping proto definition.
 */
public class BallerinaFileBuilder {
    
    public static final Logger LOG = LoggerFactory.getLogger(BallerinaFileBuilder.class);
    private byte[] rootDescriptor;
    private List<byte[]> dependentDescriptors;
    private String balOutPath;
    
    public BallerinaFileBuilder(List<byte[]> dependentDescriptors) {
        this.dependentDescriptors = dependentDescriptors;
    }
    
    public BallerinaFileBuilder(List<byte[]> dependentDescriptors, String balOutPath) {
        this.dependentDescriptors = dependentDescriptors;
        this.balOutPath = balOutPath;
    }
    
    public void build() {
        try {
            InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
            DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                    .parseFrom(targetStream);
            
            List<DescriptorProtos.DescriptorProto> messageTypeList = fileDescriptorSet.getMessageTypeList();
            List<DescriptorProtos.MethodDescriptorProto> methodList = fileDescriptorSet
                    .getService(SERVICE_INDEX).getMethodList();
            List<DescriptorProtos.EnumDescriptorProto> enumDescriptorProtos = fileDescriptorSet.getEnumTypeList();
            String methodName;
            String reqMessageName;
            String resMessageName;
            String methodID;
            String packageName = EMPTY_STRING.equals(fileDescriptorSet.getPackage()) ? BalGenConstants.DEFAULT_PACKAGE :
                    fileDescriptorSet.getPackage() + PACKAGE_SEPARATOR + BalGenConstants.DEFAULT_PACKAGE;
            ClientBuilder clientStubBal = new ClientBuilder(packageName, fileDescriptorSet
                    .getService(SERVICE_INDEX).getName());
            DescriptorBuilder descriptorBuilder;
            String protoPackage = fileDescriptorSet.getPackage();
            if (EMPTY_STRING.equals(protoPackage)) {
                descriptorBuilder = new DescriptorBuilder(dependentDescriptors,
                        fileDescriptorSet.getName(), clientStubBal);
            } else {
                descriptorBuilder = new DescriptorBuilder(dependentDescriptors,
                        fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName(), clientStubBal);
            }
            descriptorBuilder.setRootDescriptor(rootDescriptor);
            descriptorBuilder.buildMap();
            descriptorBuilder.buildKey();
            for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                MethodDescriptor.MethodType methodType = MessageUtils.getMethodType(methodDescriptorProto);
                String[] outputTypes = methodDescriptorProto.getOutputType().split(PACKAGE_SEPARATOR_REGEX);
                String typeOut = outputTypes[outputTypes.length - 1];
                String[] inputTypes = methodDescriptorProto.getInputType().split(PACKAGE_SEPARATOR_REGEX);
                String typeIn = inputTypes[inputTypes.length - 1];
                methodName = methodDescriptorProto.getName();
                if (!EMPTY_STRING.equals(fileDescriptorSet.getPackage())) {
                    methodID = fileDescriptorSet.getPackage() + PACKAGE_SEPARATOR + fileDescriptorSet
                            .getService(SERVICE_INDEX).getName() + FILE_SEPARATOR + methodName;
                } else {
                    methodID = fileDescriptorSet.getService(SERVICE_INDEX).getName() + FILE_SEPARATOR + methodName;
                }
                reqMessageName = getMappingBalType(typeIn);
                resMessageName = getMappingBalType(typeOut);
                if ((EMPTY_DATA_TYPE.equals(reqMessageName) || EMPTY_DATA_TYPE.equals(resMessageName))
                        && !(clientStubBal.isStructContains(EMPTY_DATA_TYPE))) {
                    clientStubBal.addStruct(EMPTY_DATA_TYPE, new String[0], new String[0]);
                }
                ActionBuilder.build(methodName, reqMessageName, resMessageName
                        , methodID, methodType, clientStubBal);
            }
            
            for (DescriptorProtos.DescriptorProto descriptorProto : messageTypeList) {
                String[] attributesNameArr = new String[descriptorProto.getFieldCount()];
                String[] attributesTypeArr = new String[descriptorProto.getFieldCount()];
                int j = 0;
                for (DescriptorProtos.FieldDescriptorProto fieldDescriptorProto : descriptorProto
                        .getFieldList()) {
                    attributesNameArr[j] = fieldDescriptorProto.getName();
                    attributesTypeArr[j] = !fieldDescriptorProto.getTypeName().equals("") ? fieldDescriptorProto
                            .getTypeName().split(PACKAGE_SEPARATOR_REGEX)[fieldDescriptorProto.getTypeName()
                            .split(PACKAGE_SEPARATOR_REGEX).length - 1] : getTypeName(fieldDescriptorProto.getType()
                            .getNumber());
                    j++;
                }
                clientStubBal.addStruct(descriptorProto.getName(), attributesNameArr, attributesTypeArr);
            }
            for (DescriptorProtos.EnumDescriptorProto descriptorProto : enumDescriptorProtos) {
                String[] attributesNameArr = new String[descriptorProto.getValueCount()];
                int j = 0;
                for (DescriptorProtos.EnumValueDescriptorProto fieldDescriptorProto : descriptorProto.getValueList()) {
                    attributesNameArr[j] = fieldDescriptorProto.getName();
                    j++;
                }
                clientStubBal.addEnum(descriptorProto.getName(), attributesNameArr);
            }
            StubBuilder.build(clientStubBal, clientStubBal.isFunctionsUnaryNotEmpty());
            ClientStruct sampleClient = new ClientStruct(clientStubBal.isFunctionsStreamingNotEmpty(), clientStubBal
                    .isFunctionsUnaryNotEmpty(), fileDescriptorSet.getService(SERVICE_INDEX).getName(), packageName);
            if (this.balOutPath == null) {
                String path = balOutPathGenerator(packageName + PACKAGE_SEPARATOR + fileDescriptorSet
                        .getService(SERVICE_INDEX).getName());
                writeBallerina(clientStubBal, DEFAULT_SKELETON_DIR,
                        SKELETON_TEMPLATE_NAME, path + STUB_FILE_PREFIX);
                writeBallerina(sampleClient, DEFAULT_SAMPLE_DIR,
                        SAMPLE_TEMPLATE_NAME, path + SAMPLE_FILE_PREFIX);
            } else {
                String path = this.balOutPath + FILE_SEPARATOR + fileDescriptorSet
                        .getService(SERVICE_INDEX).getName();
                writeBallerina(clientStubBal, DEFAULT_SKELETON_DIR,
                        SKELETON_TEMPLATE_NAME, path + STUB_FILE_PREFIX);
                File sampleFile = new File(path + SAMPLE_FILE_PREFIX);
                if (!sampleFile.isFile()) {
                    Files.createFile(Paths.get(sampleFile.getAbsolutePath()));
                }
                writeBallerina(sampleClient, DEFAULT_SAMPLE_DIR,
                        SAMPLE_TEMPLATE_NAME, path + SAMPLE_FILE_PREFIX);
            }
        } catch (IOException e) {
            throw new BalGenerationException("Error while generating .bal file.", e);
        }
    }
    
    private String balOutPathGenerator(String packageName) throws IOException {
        String pathString = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);
        File stubFile = new File(pathString + STUB_FILE_PREFIX);
        File sampleFile = new File(pathString + SAMPLE_FILE_PREFIX);
        Path path = Paths.get(stubFile.getAbsolutePath()).getParent();
        if (path != null) {
            Files.createDirectories(path);
        }
        if (!stubFile.isFile()) {
            Files.createFile(Paths.get(stubFile.getAbsolutePath()));
        }
        if (!sampleFile.isFile()) {
            Files.createFile(Paths.get(sampleFile.getAbsolutePath()));
        }
        return pathString;
    }
    
    public void setRootDescriptor(byte[] rootDescriptor) {
        this.rootDescriptor = new byte[rootDescriptor.length];
        this.rootDescriptor = Arrays.copyOf(rootDescriptor, rootDescriptor.length);
    }
}
