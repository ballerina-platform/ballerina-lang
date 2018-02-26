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
import java.util.List;

import static org.ballerinalang.net.grpc.builder.BalGeneratorConstants.NEW_LINE_CHARACTER;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.bytesToHex;

/**
 * Class that responsible of generating global constants at .bal stub
 */
public class ConstantBuilder {
    private byte[] rootDescriptor;
    private List<byte[]> dependentDescriptors;
    private String key;
    
    public ConstantBuilder(byte[] rootDescriptor, List<byte[]> dependentDescriptors, String key) {
        this.rootDescriptor = rootDescriptor;
        this.dependentDescriptors = dependentDescriptors;
        this.key = key;
    }
    
    public String buildMap() throws IOException {
        
        InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
        DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                .parseFrom(targetStream);
        BMap<String, BString> descriptorMap = new BMap<String, BString>();
        descriptorMap.put("\"" + fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName() + "\"",
                new BString("\"" + bytesToHex(rootDescriptor) + "\""));
        
        for (byte[] str : dependentDescriptors) {
            if (str.length > 0) {
                targetStream = new ByteArrayInputStream(str);
                fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                        .parseFrom(targetStream);
                descriptorMap.put("\"" + fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName() + "\"",
                        new BString("\"" + bytesToHex(str) + "\""));
            }
        }
        return mapToString(descriptorMap);
    }
    
    /**
     * todo.
     *
     * @param bMap .
     * @return .
     */
    private String mapToString(BMap bMap) {
        
        StringBuilder payload = new StringBuilder();
        for (Object key : bMap.keySet()) {
            payload.append(NEW_LINE_CHARACTER).append(key).append(":").append(bMap.get(key)).append(",");
        }
        return payload.substring(0, payload.length() - 1);
    }
    
    /**
     * todo.
     *
     * @return .
     */
    public String buildKey() {
        return String.format(NEW_LINE_CHARACTER +
                "const string descriptorKey = \"%s\";" + NEW_LINE_CHARACTER, key);
    }
}
