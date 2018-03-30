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

import com.google.protobuf.Descriptors;

import java.util.HashMap;
import java.util.Map;

public class StandardDescriptorBuilder {
    private static Map<String, Descriptors.FileDescriptor> standardLibDescriptor;
    
    static {
        standardLibDescriptor = new HashMap<>();
        standardLibDescriptor.put("google/protobuf/empty.proto", com.google.protobuf.EmptyProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/any.proto", com.google.protobuf.AnyProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/api.proto", com.google.protobuf.ApiProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/descriptor.proto", com.google.protobuf.DescriptorProtos
                .getDescriptor());
        standardLibDescriptor.put("google/protobuf/duration.proto", com.google.protobuf.DurationProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/field_mask.proto", com.google.protobuf.FieldMaskProto
                .getDescriptor());
        standardLibDescriptor.put("google/protobuf/source_context.proto", com.google.protobuf.SourceContextProto
                .getDescriptor());
        standardLibDescriptor.put("google/protobuf/wrappers.proto", com.google.protobuf.WrappersProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/struct.proto", com.google.protobuf.StructProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/timestamp.proto", com.google.protobuf.TimestampProto
                .getDescriptor());
        standardLibDescriptor.put("google/protobuf/type.proto", com.google.protobuf.TypeProto.getDescriptor());
        standardLibDescriptor.put("google/protobuf/compiler/plugin.proto", com.google.protobuf.compiler.PluginProtos
                .getDescriptor());
    }
    
    public static Descriptors.FileDescriptor getFileDescriptor(String libName) {
        return standardLibDescriptor.get(libName);
    }
    
    public static Descriptors.FileDescriptor[] getFileDescriptors(Object[] libList) {
        com.google
                .protobuf.Descriptors.FileDescriptor[] fileDescriptors = new com.google
                .protobuf.Descriptors.FileDescriptor[libList.length];
        for (int i = 0; i < libList.length; i++) {
            fileDescriptors[i] = getFileDescriptor((String) libList[i]);
        }
        return fileDescriptors;
    }
}
