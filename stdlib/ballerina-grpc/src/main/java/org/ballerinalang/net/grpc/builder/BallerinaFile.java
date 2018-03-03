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
import org.ballerinalang.net.grpc.builder.components.StructBuilder;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.ballerinalang.net.grpc.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.SERVICE_INDEX;
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
    private Path balOutPath;
    
    public BallerinaFile(List<byte[]> dependentDescriptors, Path balOutPath) {
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
            StringBuilder nonBlockingActionList = new StringBuilder();
            StringBuilder blockingActionList = new StringBuilder();
            int i = 0;
            String methodName;
            String reqMessageName;
            String resMessageName;
            String methodID;
            ActionBuilder actionBuilder;
            for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                MethodDescriptor.MethodType methodType = MessageUtil.getMethodType(methodDescriptorProto);
                String[] outputTypes = methodDescriptorProto.getOutputType().split("\\.");
                String typeOut = outputTypes[outputTypes.length - 1];
                String[] inputTypes = methodDescriptorProto.getInputType().split("\\.");
                String typeIn = inputTypes[inputTypes.length - 1];
                methodName = methodDescriptorProto.getName();
                methodID = fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getService(SERVICE_INDEX)
                        .getName() + FILE_SEPARATOR + methodName;
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
                            .getTypeName().split("\\.")[fieldDescriptorProto.getTypeName().split("\\.")
                            .length - 1] : getTypeName(fieldDescriptorProto.getType().getNumber());
                    j++;
                }
                StructBuilder structBuilder = new StructBuilder(descriptorProto.getName());
                structBuilder.setAttributesNameArr(attributesNameArr);
                structBuilder.setAttributesTypeArr(attributesTypeArr);
                String struct = structBuilder.buildStructs();
                structList = structList.append(NEW_LINE_CHARACTER).append(struct);
            }
            String packageName = new PackageBuilder(fileDescriptorSet.getPackage()).build();
            String blockingConnectorList = new ConnectorBuilder(blockingActionList.toString(),
                    fileDescriptorSet.getService(SERVICE_INDEX).getName() + "BlockingStub",
                    "blocking").build();
            String streamingConnectorList = new ConnectorBuilder(nonBlockingActionList.toString(),
                    fileDescriptorSet.getService(
                            SERVICE_INDEX).getName() + "NonBlockingStub", "non-blocking").build();
            String balPayload = packageName + blockingConnectorList + NEW_LINE_CHARACTER +
                    streamingConnectorList + structList + NEW_LINE_CHARACTER + descriptorKey +
                    String.format("map descriptorMap ={%s};", descriptorMapString) + NEW_LINE_CHARACTER;
            writeFile(balPayload,
                    fileDescriptorSet.getService(SERVICE_INDEX).getName() + ".pb.bal",
                    balOutPath);
        } catch (IOException e) {
            throw new BalGenerationException("Error while generating .bal file.", e);
        }
    }
    
    public void setRootDescriptor(byte[] rootDescriptor) {
        this.rootDescriptor = new byte[rootDescriptor.length];
        this.rootDescriptor = Arrays.copyOf(rootDescriptor, rootDescriptor.length);
    }
}
