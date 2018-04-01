/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.proto.definition;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File definition builder.
 *
 * @since 1.0.0
 */
public class File {
    private DescriptorProtos.FileDescriptorProto fileDescriptorProto;
    private List<Message> messageList = new ArrayList<>();
    private List<UserDefinedEnumMessage> enumList = new ArrayList<>();
    private List<Service> serviceList = new ArrayList<>();
    private List<String> dependencyList = new ArrayList<>();
    
    private File(DescriptorProtos.FileDescriptorProto fileDescriptorProto) {
        this.fileDescriptorProto = fileDescriptorProto;
    }
    
    void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }
    
    void setEnumList(List<UserDefinedEnumMessage> enumList) {
        this.enumList = enumList;
    }
    
    void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }
    
    void setDependencyList(List<String> dependencyList) {
        this.dependencyList = dependencyList;
    }
    
    public static File.Builder newBuilder(String fileName) {
        return new File.Builder(fileName);
    }
    
    public DescriptorProtos.FileDescriptorProto getFileDescriptorProto() {
        return fileDescriptorProto;
    }
    
    public Descriptors.FileDescriptor getFileDescriptor() throws GrpcServerException {
        try {
            return Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[] {});
        } catch (Descriptors.DescriptorValidationException e) {
            throw new GrpcServerException(e);
        }
    }
    
    public String getFileDefinition() {
        StringBuilder fileDefinition = new StringBuilder();
        
        fileDefinition.append("syntax = \"").append(fileDescriptorProto.getSyntax()).append("\";").append
                (ServiceProtoConstants.NEW_LINE_CHARACTER);
        if (!"".equals(fileDescriptorProto.getPackage())) {
            fileDefinition.append("package ").append(fileDescriptorProto.getPackage()).append(";").append
                    (ServiceProtoConstants.NEW_LINE_CHARACTER);
        }
        for (String dependency : dependencyList) {
            fileDefinition.append("import \"").append(dependency).append("\";").append(ServiceProtoConstants
                    .NEW_LINE_CHARACTER);
        }
        for (Service service : serviceList) {
            fileDefinition.append(service.getServiceDefinition());
        }
        for (Message message : messageList) {
            fileDefinition.append(message.getMessageDefinition());
        }
        for (UserDefinedEnumMessage message : enumList) {
            fileDefinition.append(message.getMessageDefinition());
        }
        return fileDefinition.toString();
    }
    
    /**
     * FieldDefinition.Builder
     */
    public static class Builder {
        
        public File build() {
            File file = new File(fileBuilder.build());
            file.setMessageList(messageList);
            file.setEnumList(enumList);
            file.setServiceList(serviceList);
            file.setDependencyList(dependencyList);
            return file;
        }
        public boolean isEnumExists(String enumId) {
            for (UserDefinedEnumMessage enumMessage : enumList) {
                if (enumMessage.getDescriptorProto().getName().equals(enumId)) {
                    return true;
                }
            }
            return false;
        }
        public Builder setPackage(String packageName) {
            fileBuilder.setPackage(packageName);
            return this;
        }
        
        public Builder setSyntax(String syntax) {
            fileBuilder.setSyntax(syntax);
            return this;
        }
        
        public Builder setService(Service serviceDefinition) {
            fileBuilder.addService(serviceDefinition.getServiceDescriptor());
            serviceList.add(serviceDefinition);
            return this;
        }
        
        public Builder setMessage(Message messageDefinition) {
            fileBuilder.addMessageType((DescriptorProtos.DescriptorProto) messageDefinition.getDescriptorProto());
            messageList.add(messageDefinition);
            return this;
        }
        
        public Builder setEnum(UserDefinedEnumMessage enumDefinition) {
            fileBuilder.addEnumType(enumDefinition.getDescriptorProto());
            enumList.add(enumDefinition);
            return this;
        }
        
        public void setDependency(String dependency) {
            fileBuilder.addDependency(dependency);
            dependencyList.add(dependency);
        }
        
        public List<DescriptorProtos.DescriptorProto> getRegisteredMessages() {
            return Collections.unmodifiableList(fileBuilder.getMessageTypeList());
        }
        
        public List<String> getRegisteredDependencies() {
            return Collections.unmodifiableList(fileBuilder.getDependencyList());
        }

        public List<DescriptorProtos.EnumDescriptorProto> getRegisteredEnums() {
            return Collections.unmodifiableList(fileBuilder.getEnumTypeList());
        }
        
        private Builder(String fileName) {
            fileBuilder = DescriptorProtos.FileDescriptorProto.newBuilder();
            fileBuilder.setName(fileName);
        }
        
        private DescriptorProtos.FileDescriptorProto.Builder fileBuilder;
        private List<Message> messageList = new ArrayList<>();
        private List<UserDefinedEnumMessage> enumList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<String> dependencyList = new ArrayList<>();
    }
}
