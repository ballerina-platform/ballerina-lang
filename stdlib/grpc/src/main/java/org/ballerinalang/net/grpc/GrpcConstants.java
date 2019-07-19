/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import com.google.protobuf.DescriptorProtos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.util.BLangConstants.ORG_NAME_SEPARATOR;

/**
 * Proto Message Constants Class.
 *
 * @since 1.0.0
 */
public class GrpcConstants {
    //gRPC package name.
    public static final String PROTOCOL_PACKAGE_GRPC = "grpc";
    public static final String ORG_NAME = "ballerina";
    public static final String PROTOCOL_STRUCT_PACKAGE_GRPC = ORG_NAME + ORG_NAME_SEPARATOR + "grpc";

    public static final String HTTPS_ENDPOINT_STARTED = "[ballerina/grpc] started HTTPS/WSS endpoint ";
    public static final String HTTP_ENDPOINT_STARTED = "[ballerina/grpc] started HTTP/WS endpoint ";
    public static final String HTTPS_ENDPOINT_STOPPED = "[ballerina/grpc] stopped HTTPS/WSS endpoint ";
    public static final String HTTP_ENDPOINT_STOPPED = "[ballerina/grpc] stopped HTTP/WS endpoint ";

    //server side endpoint constants.
    public static final String SERVICE_REGISTRY_BUILDER = "SERVICE_REGISTRY_BUILDER";
    public static final String SERVER_CONNECTOR = "SERVER_CONNECTOR";
    public static final String CONNECTOR_STARTED = "CONNECTOR_STARTED";
    public static final String LISTENER = "Listener";
    public static final String CALLER = "Caller";
    public static final String RESPONSE_OBSERVER = "RESPONSE_OBSERVER";
    public static final String RESPONSE_MESSAGE_DEFINITION = "RESPONSE_DEFINITION";
    public static final String CALLER_ID = "instanceId";

    // Service Descriptor Annotation
    public static final String DESCRIPTOR_MAP = "getDescriptorMap";
    public static final String ROOT_DESCRIPTOR = "ROOT_DESCRIPTOR";
    public static final String ANN_SERVICE_DESCRIPTOR = "ServiceDescriptor";
    public static final String ANN_RECORD_DESCRIPTOR_DATA = "ServiceDescriptorData";
    public static final String ANN_FIELD_DESCRIPTOR = "descriptor";
    public static final String ANN_FIELD_DESC_MAP = "descMap";
    
    //client side endpoint constants
    public static final String CLIENT_ENDPOINT_TYPE = "Client";
    public static final String CLIENT_CONNECTOR = "ClientConnector";
    public static final String ENDPOINT_URL = "url";
    public static final String MESSAGE_HEADERS = "MessageHeaders";

    public static final String SERVICE_STUB = "Stub";
    public static final String METHOD_DESCRIPTORS = "MethodDescriptors";
    public static final String BLOCKING_TYPE = "blocking";
    public static final String NON_BLOCKING_TYPE = "non-blocking";
    public static final String REQUEST_SENDER = "REQUEST_SENDER";
    public static final String REQUEST_MESSAGE_DEFINITION = "REQUEST_DEFINITION";
    public static final String REGEX_DOT_SEPERATOR = "\\.";
    public static final String DOT = ".";
    public static final String NO_PACKAGE = ".";

    public static final String CLIENT = "Client";
    public static final String STREAMING_CLIENT = "StreamingClient";
    public static final String HEADERS = "Headers";
    public static final String ANN_RESOURCE_CONFIG = "ResourceConfig";
    public static final String ANN_ATTR_RESOURCE_SERVER_STREAM = "streaming";
    
    public static final Map<DescriptorProtos.FieldDescriptorProto.Type, Integer> WIRE_TYPE_MAP;

    static {
        Map<DescriptorProtos.FieldDescriptorProto.Type, Integer> wireMap = new HashMap<>();
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE, 1);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT, 5);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32, 5);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64, 1);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32, 5);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64, 1);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, 2);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, 2);
        WIRE_TYPE_MAP = Collections.unmodifiableMap(wireMap);
    }

    // proto wrapper message constants
    public static final String WRAPPER_DOUBLE_MESSAGE = "DoubleValue";
    public static final String WRAPPER_FLOAT_MESSAGE = "FloatValue";
    public static final String WRAPPER_INT64_MESSAGE = "Int64Value";
    public static final String WRAPPER_UINT64_MESSAGE = "UInt64Value";
    public static final String WRAPPER_INT32_MESSAGE = "Int32Value";
    public static final String WRAPPER_UINT32_MESSAGE = "UInt32Value";
    public static final String WRAPPER_BOOL_MESSAGE = "BoolValue";
    public static final String WRAPPER_STRING_MESSAGE = "StringValue";
    public static final String WRAPPER_BYTES_MESSAGE = "BytesValue";

    // Server Streaming method resources.
    public static final String ON_OPEN_RESOURCE = "onOpen";
    public static final String ON_COMPLETE_RESOURCE = "onComplete";
    public static final String ON_MESSAGE_RESOURCE = "onMessage";
    public static final String ON_ERROR_RESOURCE = "onError";
    
    public static final String STRING = "string";
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String BOOLEAN = "boolean";
    public static final String BYTE = "byte";

    //stub template builder constants
    public static final String EMPTY_DATATYPE_NAME = "Empty";

    //Header keys
    static final String GRPC_STATUS_KEY = "grpc-status";
    static final String GRPC_MESSAGE_KEY = "grpc-message";
    static final String CONTENT_TYPE_KEY = "content-type";
    static final String TE_KEY = "te";
    static final String TO_HEADER = "TO";
    public static final String SCHEME_HEADER = "scheme";
    public static final String AUTHORITY = "authority";

    //Content-Type used for GRPC-over-HTTP/2.
    public static final String CONTENT_TYPE_GRPC = "application/grpc";

    //The HTTP method used for GRPC requests.
    public static final String HTTP_METHOD = "POST";


    //The TE (transport encoding) header for requests over HTTP/2.
    public static final String TE_TRAILERS = "trailers";

    //The message encoding (i.e. compression) that can be used in the stream.
    static final String MESSAGE_ENCODING = "grpc-encoding";

    //The accepted message encodings (i.e. compression) that can be used in the stream.
    static final String MESSAGE_ACCEPT_ENCODING = "grpc-accept-encoding";

    //The content-encoding used to compress the full gRPC stream.
    static final String CONTENT_ENCODING = "content-encoding";

    //The default maximum uncompressed size (in bytes) for inbound messages. Defaults to 4 MiB.
    static final int DEFAULT_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;

    private GrpcConstants() {
    }

    // Listener struct fields
    public static final String LISTENER_ID_FIELD = "id";
    public static final String LISTENER_CONNECTION_FIELD = "conn";

}
