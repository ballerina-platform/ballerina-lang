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
import org.ballerinalang.net.grpc.builder.components.ActionBuilder;
import org.ballerinalang.net.grpc.builder.components.ConnectorBuilder;
import org.ballerinalang.net.grpc.builder.components.ConstantBuilder;
import org.ballerinalang.net.grpc.builder.components.StructBuilder;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.builder.BalGeneratorConstants.NEW_LINE_CHARACTER;
import static org.ballerinalang.net.grpc.builder.BalGeneratorConstants.SERVICE_INDEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.attributeListToMap;
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
    
    public BallerinaFile(byte[] rootDescriptor, List<byte[]> dependentDescriptors, Path balOutPath) {
        this.rootDescriptor = rootDescriptor;
        this.dependentDescriptors = dependentDescriptors;
        this.balOutPath = balOutPath;
    }
    
    public void build() {
        try {
            InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
            DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                    .parseFrom(targetStream);
            ConstantBuilder constantBuilder = new ConstantBuilder(rootDescriptor, dependentDescriptors,
                    fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName());
            List<DescriptorProtos.DescriptorProto> messageTypeList = fileDescriptorSet.getMessageTypeList();
            List<DescriptorProtos.MethodDescriptorProto> methodList = fileDescriptorSet
                    .getService(SERVICE_INDEX).getMethodList();
            Map<String, DescriptorProtos.DescriptorProto> descriptorProtoMap = attributeListToMap(fileDescriptorSet
                    .getMessageTypeList());
            
            String descriptorMapString = constantBuilder.buildMap();
            String descriptorKey = constantBuilder.buildKey();
            StringBuilder actionList = new StringBuilder();
            int i = 0;
            String methodName = null;
            String reqMessageName;
            String resMessageName;
            String methodID = null;
            String reqStructFieldName = null;
            String reqStructFieldType = null;
            String resStructFieldName = null;
            String resStructFieldType = null;
            ActionBuilder actionBuilder;
            for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                String[] outputTypes = methodDescriptorProto.getOutputType().split("\\.");
                String typeOut = outputTypes[outputTypes.length - 1];
                String[] inputTypes = methodDescriptorProto.getInputType().split("\\.");
                String typeIn = inputTypes[inputTypes.length - 1];
                methodName = methodDescriptorProto.getName();
                methodID = fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getService(SERVICE_INDEX)
                        .getName() + "/" + methodName;
                reqMessageName = getMappingBalType(typeIn);
                resMessageName = getMappingBalType(typeOut);
                if ((descriptorProtoMap.get(typeIn) != null) && (descriptorProtoMap.get(typeOut) != null) &&
                        (descriptorProtoMap.get(typeIn).getFieldList().size() == 0) &&
                        (descriptorProtoMap.get(typeOut).getFieldList().size() == 0)) {
                    reqStructFieldName = descriptorProtoMap.get(typeIn).getFieldList().get(0).getName();
                    reqStructFieldType = getTypeName(descriptorProtoMap.get(typeIn).getFieldList().get(0)
                            .getType().getNumber());
                    resStructFieldName = descriptorProtoMap.get(typeOut).getFieldList().get(0)
                            .getName();
                    resStructFieldType = getTypeName(descriptorProtoMap.get(typeOut).getFieldList().get(0)
                            .getType().getNumber());
                } else if ((descriptorProtoMap.get(typeOut) != null) &&
                        (descriptorProtoMap.get(typeOut).getFieldList().size() != 0)) {
                    reqMessageName = getMappingBalType(typeIn);
                    resMessageName = getMappingBalType(typeOut);
                    resStructFieldName = descriptorProtoMap.get(typeOut).getFieldList().get(0)
                            .getName();
                    resStructFieldType = getTypeName(descriptorProtoMap.get(typeOut).getFieldList().get(0)
                            .getType().getNumber());
                } else if ((descriptorProtoMap.get(typeIn) != null) &&
                        (descriptorProtoMap.get(typeIn).getFieldList().size() != 0)) {
                    reqMessageName = getMappingBalType(typeIn);
                    resMessageName = getMappingBalType(typeOut);
                    reqStructFieldName = descriptorProtoMap.get(typeIn).getFieldList().get(0).getName();
                    reqStructFieldType = getTypeName(descriptorProtoMap.get(typeIn).getFieldList().get(0)
                            .getType().getNumber());
                } else {
                    reqMessageName = getMappingBalType(typeIn);
                    resMessageName = getMappingBalType(typeOut);
                }
                actionBuilder = new ActionBuilder(methodName, reqMessageName, resMessageName
                        , methodID, reqStructFieldName, reqStructFieldType, resStructFieldName, resStructFieldType);
                actionList = actionList.append(NEW_LINE_CHARACTER).append(actionBuilder.build());
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
                            .length - 1] :
                            getTypeName(fieldDescriptorProto.getType().getNumber());
                    j++;
                }
                StructBuilder structBuilder = new StructBuilder(attributesNameArr, attributesTypeArr,
                        descriptorProto.getName());
                String structs = structBuilder.buildStructs();
                structList = structList.append(NEW_LINE_CHARACTER).append(structs);
            }
            
            String connectorList = new ConnectorBuilder(actionList.toString(), fileDescriptorSet.getPackage(),
                    fileDescriptorSet.getService(SERVICE_INDEX).getName() + "Stub{{stubType}}").build();
            String balPayload = connectorList + structList + NEW_LINE_CHARACTER + descriptorKey +
                    String.format("map descriptorMap ={%s};", descriptorMapString) + NEW_LINE_CHARACTER;
            writeFile(balPayload.replace("{{stubType}}", "Blocking"),
                    fileDescriptorSet.getService(SERVICE_INDEX).getName() + "." + "blocking" + ".pb.bal",
                    balOutPath);
            writeFile(balPayload.replace("{{stubType}}", "NonBlocking"),
                    fileDescriptorSet.getService(SERVICE_INDEX).getName() + "." + "nonBlocking" + ".pb.bal",
                    balOutPath);
        } catch (IOException e) {
            throw new BalGenerationException("Error while generating .bal file.", e);
        }
    }
}
