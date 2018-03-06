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
import org.ballerinalang.net.grpc.builder.components.ActionBuilder;
import org.ballerinalang.net.grpc.builder.components.ConnectorBuilder;
import org.ballerinalang.net.grpc.builder.components.ConstantBuilder;
import org.ballerinalang.net.grpc.builder.components.PackageBuilder;
import org.ballerinalang.net.grpc.builder.components.SampleClientGenerator;
import org.ballerinalang.net.grpc.builder.components.StructBuilder;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.ballerinalang.net.grpc.utils.MessageUtil;
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

import static org.ballerinalang.net.grpc.builder.BalGenConstants.EMPTY_STRING;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.PACKAGE_SEPARATOR_REGEX;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.SAMPLE_FILE_PRIFIX;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.SERVICE_INDEX;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.STUB_FILE_PRIFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getMappingBalType;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getTypeName;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.writeFile;

/**
 * Class is responsible of generating the ballerina stub which is mapping proto definition.
 */
public class BallerinaFile {
    
    public static final Logger LOG = LoggerFactory.getLogger(BallerinaFile.class);
    private byte[] rootDescriptor;
    private List<byte[]> dependentDescriptors;
    private String balOutPath;
    
    public BallerinaFile(List<byte[]> dependentDescriptors) {
        this.dependentDescriptors = dependentDescriptors;
    }
    
    public BallerinaFile(List<byte[]> dependentDescriptors, String balOutPath) {
        this.dependentDescriptors = dependentDescriptors;
        this.balOutPath = balOutPath;
    }
    
    public void build() {
        try {
            InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
            DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                    .parseFrom(targetStream);
            ConstantBuilder constantBuilder = new ConstantBuilder(dependentDescriptors,
                    fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName());
            constantBuilder.setRootDescriptor(rootDescriptor);
            List<DescriptorProtos.DescriptorProto> messageTypeList = fileDescriptorSet.getMessageTypeList();
            List<DescriptorProtos.MethodDescriptorProto> methodList = fileDescriptorSet
                    .getService(SERVICE_INDEX).getMethodList();
            String descriptorMapString = constantBuilder.buildMap();
            String descriptorKey = constantBuilder.buildKey();
            StringBuilder nonBlockingActionList = new StringBuilder(EMPTY_STRING);
            StringBuilder blockingActionList = new StringBuilder(EMPTY_STRING);
            int i = 0;
            String methodName;
            String reqMessageName;
            String resMessageName;
            String methodID;
            ActionBuilder actionBuilder;
            for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                MethodDescriptor.MethodType methodType = MessageUtil.getMethodType(methodDescriptorProto);
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
                actionBuilder = new ActionBuilder(methodName, reqMessageName, resMessageName
                        , methodID, methodType);
                actionBuilder.build();
                blockingActionList = blockingActionList.append(NEW_LINE_CHARACTER).append(actionBuilder
                        .getBlockingAction());
                nonBlockingActionList = nonBlockingActionList.append(NEW_LINE_CHARACTER).append(actionBuilder
                        .getNonBlockingAction());
                i++;
            }
            
            StringBuilder structList = new StringBuilder();
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
                StructBuilder structBuilder = new StructBuilder(descriptorProto.getName());
                structBuilder.setAttributesNameArr(attributesNameArr);
                structBuilder.setAttributesTypeArr(attributesTypeArr);
                String struct = structBuilder.buildStructs();
                structList = structList.append(NEW_LINE_CHARACTER).append(struct);
            }
            String packageName = "".equals(fileDescriptorSet.getPackage()) ? BalGenConstants.DEFAULT_PACKAGE :
                    fileDescriptorSet.getPackage();
            String filePackageData = new PackageBuilder(packageName).build();
            String blockingConnectorList = new ConnectorBuilder(blockingActionList.toString(),
                    fileDescriptorSet.getService(SERVICE_INDEX).getName() + "BlockingStub",
                    "blocking").build();
            String streamingConnectorList = new ConnectorBuilder(nonBlockingActionList.toString(),
                    fileDescriptorSet.getService(
                            SERVICE_INDEX).getName() + "NonBlockingStub", "non-blocking").build();
            String balPayload = filePackageData + blockingConnectorList + NEW_LINE_CHARACTER +
                    streamingConnectorList + structList + NEW_LINE_CHARACTER + descriptorKey +
                    String.format("map descriptorMap ={%s};", descriptorMapString) + NEW_LINE_CHARACTER;
            boolean generateBlocking = !blockingActionList.toString().equals(EMPTY_STRING);
            boolean generateNonBlocking = !nonBlockingActionList.toString().equals(EMPTY_STRING);
            String samplePayload = new SampleClientGenerator(packageName, fileDescriptorSet
                    .getService(SERVICE_INDEX).getName(), generateBlocking, generateNonBlocking).build();
            if (this.balOutPath == null) {
                String path = balOutPathGenerator(packageName + PACKAGE_SEPARATOR + fileDescriptorSet
                        .getService(SERVICE_INDEX).getName());
                Path stubBalOutPath = Paths.get(path + STUB_FILE_PRIFIX);
                Path clientBalOutPath = Paths.get(path + SAMPLE_FILE_PRIFIX);
                writeFile(balPayload, stubBalOutPath);
                writeFile(samplePayload, clientBalOutPath);
            } else {
                String path = this.balOutPath + FILE_SEPARATOR + fileDescriptorSet
                        .getService(SERVICE_INDEX).getName();
                writeFile(balPayload, Paths.get(path + STUB_FILE_PRIFIX));
                File sampleFile = new File(path + SAMPLE_FILE_PRIFIX);
                if (!sampleFile.isFile()) {
                    Files.createFile(Paths.get(sampleFile.getAbsolutePath()));
                }
                writeFile(balPayload, Paths.get(path + STUB_FILE_PRIFIX));
                writeFile(samplePayload, Paths.get(path + SAMPLE_FILE_PRIFIX));
            }
        } catch (IOException e) {
            throw new BalGenerationException("Error while generating .bal file.", e);
        }
    }
    
    private String balOutPathGenerator(String packageName) throws IOException {
        String pathString = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);
        File stubFile = new File(pathString + STUB_FILE_PRIFIX);
        File sampleFile = new File(pathString + SAMPLE_FILE_PRIFIX);
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
