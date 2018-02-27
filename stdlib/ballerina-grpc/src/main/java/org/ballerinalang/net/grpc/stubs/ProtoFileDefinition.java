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
package org.ballerinalang.net.grpc.stubs;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class contains proto descriptors of the service.
 */
public final class ProtoFileDefinition {

    private byte[] rootDescriptorData;
    private List<byte[]> dependentDescriptorData;
    private Descriptors.FileDescriptor fileDescriptor;

    public ProtoFileDefinition(byte[] descriptorData, List<byte[]> depDescriptorData) {
        rootDescriptorData = descriptorData;
        dependentDescriptorData = depDescriptorData;
    }

    /**.
     * Returns file descriptor of the gRPC service.
     *
     * @return file descriptor of the service.
     */
    public Descriptors.FileDescriptor getDescriptor() {
        if (fileDescriptor != null) {
            return fileDescriptor;
        }
        Descriptors.FileDescriptor[] depSet = new Descriptors.FileDescriptor[dependentDescriptorData.size()];
        int i = 0;
        for (byte[] dis : dependentDescriptorData) {
            try {
                DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                        .parseFrom(dis);
                depSet[i] = Descriptors.FileDescriptor.buildFrom(fileDescriptorSet, new Descriptors.FileDescriptor[]{});
                i++;
            } catch (InvalidProtocolBufferException | Descriptors.DescriptorValidationException e) {
                throw new RuntimeException("Error while gen erating depend descriptors. ", e);
            }
        }

        try (InputStream targetStream = new ByteArrayInputStream(rootDescriptorData)) {
            DescriptorProtos.FileDescriptorProto descriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom
                    (targetStream);
            return fileDescriptor = Descriptors.FileDescriptor.buildFrom(descriptorProto, depSet);
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new RuntimeException("Error while generating service descriptor : ", e);
        }
    }

}
