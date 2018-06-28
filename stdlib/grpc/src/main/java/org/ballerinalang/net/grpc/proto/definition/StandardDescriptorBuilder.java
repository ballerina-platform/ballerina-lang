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

/**
 * Provides protobuf descriptor for well known dependency.
 */
public class StandardDescriptorBuilder {

    private static Map<String, Descriptors.FileDescriptor> standardLibDescriptor;

    private static final String EMPTY_PROTO_PACKAGE_KEY = "google/protobuf/empty.proto";
    private static final String ANY_PROTO_PACKAGE_KEY = "google/protobuf/any.proto";
    private static final String API_PROTO_PACKAGE_KEY = "google/protobuf/api.proto";
    private static final String DESCRIPTOR_PROTO_PACKAGE_KEY = "google/protobuf/descriptor.proto";
    private static final String DURATION_PROTO_PACKAGE_KEY = "google/protobuf/duration.proto";
    private static final String FIELD_MASK_PROTO_PACKAGE_KEY = "google/protobuf/field_mask.proto";
    private static final String SOURCE_CONTEXT_PROTO_PACKAGE_KEY = "google/protobuf/source_context.proto";
    private static final String WRAPPERS_PROTO_PACKAGE_KEY = "google/protobuf/wrappers.proto";
    private static final String STRUCT_PROTO_PACKAGE_KEY = "google/protobuf/struct.proto";
    private static final String TIMESTAMP_PROTO_PACKAGE_KEY = "google/protobuf/timestamp.proto";
    private static final String TYPE_PROTO_PACKAGE_KEY = "google/protobuf/type.proto";
    private static final String COMPILER_PLUGIN_PROTO_PACKAGE_KEY = "google/protobuf/compiler/plugin.proto";
    public static final String GOOGLE_PROTOBUF_PACKAGE_PREFIX = "google/protobuf/";

    static {
        standardLibDescriptor = new HashMap<>();
        standardLibDescriptor.put(EMPTY_PROTO_PACKAGE_KEY, com.google.protobuf.EmptyProto.getDescriptor());
        standardLibDescriptor.put(ANY_PROTO_PACKAGE_KEY, com.google.protobuf.AnyProto.getDescriptor());
        standardLibDescriptor.put(API_PROTO_PACKAGE_KEY, com.google.protobuf.ApiProto.getDescriptor());
        standardLibDescriptor.put(DESCRIPTOR_PROTO_PACKAGE_KEY, com.google.protobuf.DescriptorProtos
                .getDescriptor());
        standardLibDescriptor.put(DURATION_PROTO_PACKAGE_KEY, com.google.protobuf.DurationProto.getDescriptor());
        standardLibDescriptor.put(FIELD_MASK_PROTO_PACKAGE_KEY, com.google.protobuf.FieldMaskProto
                .getDescriptor());
        standardLibDescriptor.put(SOURCE_CONTEXT_PROTO_PACKAGE_KEY, com.google.protobuf.SourceContextProto
                .getDescriptor());
        standardLibDescriptor.put(WRAPPERS_PROTO_PACKAGE_KEY, com.google.protobuf.WrappersProto.getDescriptor());
        standardLibDescriptor.put(STRUCT_PROTO_PACKAGE_KEY, com.google.protobuf.StructProto.getDescriptor());
        standardLibDescriptor.put(TIMESTAMP_PROTO_PACKAGE_KEY, com.google.protobuf.TimestampProto
                .getDescriptor());
        standardLibDescriptor.put(TYPE_PROTO_PACKAGE_KEY, com.google.protobuf.TypeProto.getDescriptor());
        standardLibDescriptor.put(COMPILER_PLUGIN_PROTO_PACKAGE_KEY, com.google.protobuf.compiler.PluginProtos
                .getDescriptor());
    }

    public static Descriptors.FileDescriptor getFileDescriptor(String libName) {
        return standardLibDescriptor.get(libName);
    }

    public static Descriptors.FileDescriptor[] getFileDescriptors(Object[] libList) {
        Descriptors.FileDescriptor[] fileDescriptors = new Descriptors.FileDescriptor[libList.length];
        for (int i = 0; i < libList.length; i++) {
            fileDescriptors[i] = getFileDescriptor((String) libList[i]);
        }
        return fileDescriptors;
    }
}
