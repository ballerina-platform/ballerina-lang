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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.bytesToHex;

/**
 * Class that responsible of generating global constants at .bal stub.
 */
public class DescriptorBuilder {
    private byte[] rootDescriptor;
    private List<byte[]> dependentDescriptors;
    private String key;
    private ClientBuilder clientStubBal;
    
    public DescriptorBuilder(List<byte[]> dependentDescriptors, String key, ClientBuilder clientStubBal) {
        this.dependentDescriptors = dependentDescriptors;
        this.key = key;
        this.clientStubBal = clientStubBal;
    }
    
    public void buildMap() throws IOException {
        InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
        DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                .parseFrom(targetStream);
        BMap<String, BString> descriptorMap = new BMap<>();
        if (!"".equals(fileDescriptorSet.getPackage())) {
            descriptorMap.put(fileDescriptorSet.getPackage() + PACKAGE_SEPARATOR + fileDescriptorSet.getName()
                    , new BString(bytesToHex(rootDescriptor)));
        } else {
            descriptorMap.put(fileDescriptorSet.getName(), new BString(bytesToHex(rootDescriptor)));
        }
        for (byte[] str : dependentDescriptors) {
            if (str.length > 0) {
                targetStream = new ByteArrayInputStream(str);
                fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                        .parseFrom(targetStream);
                descriptorMap.put(fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName(),
                        new BString(bytesToHex(str)));
            }
        }
        int i = 0;
        for (Object key : descriptorMap.keySet()) {
            Descriptor descriptor;
            if (i != descriptorMap.keySet().size() - 1) {
                descriptor = new Descriptor((String) key, descriptorMap.get((String) key)
                        .stringValue(), ",");
            } else {
                descriptor = new Descriptor((String) key, descriptorMap.get((String) key)
                        .stringValue(), null);
            }
            clientStubBal.addDescriptor(descriptor);
            i++;
        }
        
    }
    
    /**
     * Following method build root descriptor key which is packageName+service proto name.
     */
    public void buildKey() {
        clientStubBal.setRootDescriptorKey(key);
    }
    
    public void setRootDescriptor(byte[] rootDescriptor) {
        this.rootDescriptor = new byte[rootDescriptor.length];
        this.rootDescriptor = Arrays.copyOf(rootDescriptor, rootDescriptor.length);
    }
}
