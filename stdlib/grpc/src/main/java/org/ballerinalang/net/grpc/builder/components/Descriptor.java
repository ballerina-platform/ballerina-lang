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
package org.ballerinalang.net.grpc.builder.components;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.model.values.BString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.bytesToHex;

/**
 * Service descriptor definition.
 *
 * @since 0.982.0
 */
public class Descriptor {
    private String descriptorKey;
    private String descriptorData;
    
    private Descriptor(String descriptorKey, String descriptorData) {
        this.descriptorKey = descriptorKey;
        this.descriptorData = descriptorData;
    }
    
    public String getKey() {
        return descriptorKey;
    }
    
    public String getData() {
        return descriptorData;
    }

    public static Descriptor.Builder newBuilder(byte[] descriptorData) {
        return new Descriptor.Builder(descriptorData);
    }

    /**
     * Service Descriptor.Builder.
     */
    public static class Builder {
        byte[] descriptorData;

        private Builder(byte[] descriptorData) {
            this.descriptorData = descriptorData;
        }

        public Descriptor build() throws IOException {
            try (InputStream targetStream = new ByteArrayInputStream(descriptorData)) {
                DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                        .parseFrom(targetStream);
                return new Descriptor(fileDescriptorSet.getName(), new BString(bytesToHex(descriptorData))
                        .stringValue());
            }
        }
    }
}
