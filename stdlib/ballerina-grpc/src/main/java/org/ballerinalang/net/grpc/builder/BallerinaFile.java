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
import org.ballerinalang.net.grpc.builder.components.ClientStubBal;
import org.ballerinalang.net.grpc.builder.components.Connector;
import org.ballerinalang.net.grpc.builder.components.DescriptorBuilder;
import org.ballerinalang.net.grpc.builder.components.SampleClient;
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

import static org.ballerinalang.net.grpc.builder.BalGenConstants.EMPTY_STRING;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.PACKAGE_SEPARATOR_REGEX;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.SAMPLE_FILE_PRIFIX;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.SERVICE_INDEX;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.STUB_FILE_PRIFIX;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.DEFAULT_SAMPLE_DIR;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.DEFAULT_SKELETON_DIR;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.SAMPLE_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.SKELETON_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.writeBallerina;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getMappingBalType;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getTypeName;

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
            
            List<DescriptorProtos.DescriptorProto> messageTypeList = fileDescriptorSet.getMessageTypeList();
            List<DescriptorProtos.MethodDescriptorProto> methodList = fileDescriptorSet
                    .getService(SERVICE_INDEX).getMethodList();
            String methodName;
            String reqMessageName;
            String resMessageName;
            String methodID;
            String packageName = "".equals(fileDescriptorSet.getPackage()) ? BalGenConstants.DEFAULT_PACKAGE :
                    fileDescriptorSet.getPackage() + PACKAGE_SEPARATOR + BalGenConstants.DEFAULT_PACKAGE;
            ClientStubBal clientStubBal = new ClientStubBal(packageName);
            DescriptorBuilder descriptorBuilder;
            String protoPackage = fileDescriptorSet.getPackage();
            if ("".equals(protoPackage)) {
                descriptorBuilder = new DescriptorBuilder(dependentDescriptors,
                        fileDescriptorSet.getName(), clientStubBal);
            } else {
                descriptorBuilder = new DescriptorBuilder(dependentDescriptors,
                        fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName(), clientStubBal);
            }
            descriptorBuilder.setRootDescriptor(rootDescriptor);
            descriptorBuilder.buildMap();
            descriptorBuilder.buildKey();
            Connector blockingConnector = new Connector(fileDescriptorSet.getName(), "blocking");
            Connector nonBlockingConnector = new Connector(fileDescriptorSet.getName(), "non-blocking");
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
                new ActionBuilder(methodName, reqMessageName, resMessageName
                        , methodID, methodType, blockingConnector, nonBlockingConnector).build();
            }
            
            if (blockingConnector.isActionsNotEmpty()) {
                clientStubBal.addConnector(blockingConnector);
            }
            if (nonBlockingConnector.isActionsNotEmpty()) {
                clientStubBal.addConnector(nonBlockingConnector);
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
            
            SampleClient sampleClient = new SampleClient(nonBlockingConnector.isActionsNotEmpty(), blockingConnector
                    .isActionsNotEmpty(), fileDescriptorSet
                    .getService(SERVICE_INDEX).getName(), packageName);
            if (this.balOutPath == null) {
                String path = balOutPathGenerator(packageName + PACKAGE_SEPARATOR + fileDescriptorSet
                        .getService(SERVICE_INDEX).getName());
                writeBallerina(clientStubBal, DEFAULT_SKELETON_DIR,
                        SKELETON_TEMPLATE_NAME, path + STUB_FILE_PRIFIX);
                writeBallerina(sampleClient, DEFAULT_SAMPLE_DIR,
                        SAMPLE_TEMPLATE_NAME, path + STUB_FILE_PRIFIX);
            } else {
                String path = this.balOutPath + FILE_SEPARATOR + fileDescriptorSet
                        .getService(SERVICE_INDEX).getName();
                writeBallerina(clientStubBal, DEFAULT_SKELETON_DIR,
                        SKELETON_TEMPLATE_NAME, path + STUB_FILE_PRIFIX);
                File sampleFile = new File(path + SAMPLE_FILE_PRIFIX);
                if (!sampleFile.isFile()) {
                    Files.createFile(Paths.get(sampleFile.getAbsolutePath()));
                }
                writeBallerina(sampleClient, DEFAULT_SAMPLE_DIR,
                        SAMPLE_TEMPLATE_NAME, path + SAMPLE_FILE_PRIFIX);
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
