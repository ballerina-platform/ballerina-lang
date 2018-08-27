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
package org.ballerinalang.net.grpc.builder;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.builder.components.ClientFile;
import org.ballerinalang.net.grpc.builder.components.Descriptor;
import org.ballerinalang.net.grpc.builder.components.EnumMessage;
import org.ballerinalang.net.grpc.builder.components.Message;
import org.ballerinalang.net.grpc.builder.components.Method;
import org.ballerinalang.net.grpc.builder.components.ServiceStub;
import org.ballerinalang.net.grpc.builder.components.StubFile;
import org.ballerinalang.net.grpc.builder.utils.BalGenConstants;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.EmptyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.DEFAULT_SAMPLE_DIR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.DEFAULT_SKELETON_DIR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.EMPTY_DATA_TYPE;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SERVICE_INDEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SKELETON_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.STUB_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.writeBallerina;

/**
 * Class is responsible of generating the ballerina stub which is mapping proto definition.
 */
public class BallerinaFileBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(BallerinaFileBuilder.class);
    private byte[] rootDescriptor;
    private List<byte[]> dependentDescriptors;
    private String balOutPath;
    
    public BallerinaFileBuilder(byte[] rootDescriptor, List<byte[]> dependentDescriptors) {
        setRootDescriptor(rootDescriptor);
        this.dependentDescriptors = dependentDescriptors;
    }
    
    public BallerinaFileBuilder(byte[] rootDescriptor, List<byte[]> dependentDescriptors, String balOutPath) {
        setRootDescriptor(rootDescriptor);
        this.dependentDescriptors = dependentDescriptors;
        this.balOutPath = balOutPath;
    }
    
    public void build() {
        try (InputStream targetStream = new ByteArrayInputStream(rootDescriptor)) {
            DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                    .parseFrom(targetStream);
            List<DescriptorProtos.DescriptorProto> messageTypeList = fileDescriptorSet.getMessageTypeList();
            List<DescriptorProtos.EnumDescriptorProto> enumDescriptorProtos = fileDescriptorSet.getEnumTypeList();
            String filename = new File(fileDescriptorSet.getName()).getName().replace(".proto", "");
            String filePackage = fileDescriptorSet.getPackage();
            StubFile stubFileObject = new StubFile(filename);
            ClientFile clientFileObject = null;
            // Add root descriptor.
            Descriptor rootDesc = Descriptor.newBuilder(rootDescriptor).build();
            stubFileObject.setRootDescriptorKey(rootDesc.getKey());
            stubFileObject.addDescriptor(rootDesc);

            // Add dependent descriptors.
            for (byte[] descriptorData : dependentDescriptors) {
                Descriptor descriptor = Descriptor.newBuilder(descriptorData).build();
                stubFileObject.addDescriptor(descriptor);
            }

            if (fileDescriptorSet.getServiceCount() > 1) {
                throw new BalGenerationException("Protobuf tool doesn't support more than one service " +
                        "definition. but provided proto file contains " + fileDescriptorSet.getServiceCount() +
                        "service definitions");
            }

            if (fileDescriptorSet.getServiceCount() == 1) {
                DescriptorProtos.ServiceDescriptorProto serviceDescriptor = fileDescriptorSet.getService(SERVICE_INDEX);
                ServiceStub.Builder serviceBuilder = ServiceStub.newBuilder(serviceDescriptor.getName());
                List<DescriptorProtos.MethodDescriptorProto> methodList = serviceDescriptor.getMethodList();

                boolean isUnaryContains = false;
                for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                    String methodID;
                    if (filePackage != null && !filePackage.isEmpty()) {
                        methodID = filePackage + PACKAGE_SEPARATOR + fileDescriptorSet.getService(SERVICE_INDEX).getName
                                () + "/" + methodDescriptorProto.getName();
                    } else {
                        methodID = fileDescriptorSet.getService(SERVICE_INDEX).getName() + "/" + methodDescriptorProto
                                .getName();
                    }
                    Method method = Method.newBuilder(methodID).setMethodDescriptor(methodDescriptorProto).build();
                    serviceBuilder.addMethod(method);
                    if (MethodDescriptor.MethodType.UNARY.equals(method.getMethodType())) {
                        isUnaryContains = true;
                    }

                    if (method.containsEmptyType() && !(stubFileObject.messageExists(EMPTY_DATA_TYPE))) {
                        Message message = Message.newBuilder(EmptyMessage.newBuilder().getDescriptor().toProto())
                                .build();
                        stubFileObject.addMessage(message);
                    }
                }
                if (isUnaryContains) {
                    serviceBuilder.setType(ServiceStub.StubType.BLOCKING);
                    stubFileObject.addServiceStub(serviceBuilder.build());
                }
                serviceBuilder.setType(ServiceStub.StubType.NONBLOCKING);
                stubFileObject.addServiceStub(serviceBuilder.build());

                clientFileObject = new ClientFile(serviceDescriptor.getName(), isUnaryContains);
            }

            for (DescriptorProtos.DescriptorProto descriptorProto : messageTypeList) {
                Message message = Message.newBuilder(descriptorProto).build();
                stubFileObject.addMessage(message);
            }

            for (DescriptorProtos.EnumDescriptorProto descriptorProto : enumDescriptorProtos) {
                EnumMessage enumMessage = EnumMessage.newBuilder(descriptorProto).build();
                stubFileObject.addEnumMessage(enumMessage);
            }

            if (this.balOutPath == null) {
                this.balOutPath = BalGenConstants.DEFAULT_PACKAGE;
            }
            String stubFilePath = generateOutputFile(this.balOutPath, filename + STUB_FILE_PREFIX);
            writeBallerina(stubFileObject, DEFAULT_SKELETON_DIR, SKELETON_TEMPLATE_NAME, stubFilePath);
            if (clientFileObject != null) {
                String clientFilePath = generateOutputFile(this.balOutPath, filename + SAMPLE_FILE_PREFIX);
                writeBallerina(clientFileObject, DEFAULT_SAMPLE_DIR, SAMPLE_TEMPLATE_NAME, clientFilePath);
            }
        } catch (IOException | GrpcServerException e) {
            throw new BalGenerationException("Error while generating .bal file.", e);
        }
    }
    
    private String generateOutputFile(String outputDir, String fileName) throws IOException {
        if (outputDir != null) {
            Files.createDirectories(Paths.get(outputDir));
        }

        File file = new File(outputDir + FILE_SEPARATOR + fileName);
        if (!file.isFile()) {
            Files.createFile(Paths.get(file.getAbsolutePath()));
        }
        return file.getAbsolutePath();
    }
    
    private void setRootDescriptor(byte[] rootDescriptor) {
        this.rootDescriptor = new byte[rootDescriptor.length];
        this.rootDescriptor = Arrays.copyOf(rootDescriptor, rootDescriptor.length);
    }
}
