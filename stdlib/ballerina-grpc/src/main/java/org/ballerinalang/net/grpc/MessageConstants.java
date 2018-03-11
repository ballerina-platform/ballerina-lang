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
import io.grpc.Context;
import io.grpc.Metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * Proto Message Constants Class.
 */
public class MessageConstants {

    public static final Context.Key<String> CONTENT_TYPE_KEY = Context.key("content-type");
    public static final Metadata.Key<String> CONTENT_TYPE_MD_KEY = Metadata.Key.of("content-type",
            ASCII_STRING_MARSHALLER);

    public static final String PROTO_MESSAGE = "proto_message";
    public static final String STREAM_OBSERVER = "stream_observer";
    public static final String PROTOCOL_PACKAGE_GRPC = "ballerina.net.grpc";
    public static final String SERVER_CONNECTION = "ServiceEndpoint";
    public static final String CLIENT_CONNECTION = "ClientConnection";
    public static final String RESPONSE_MESSAGE_DEFINITION = "response_msg_definition";
    public static final String REQUEST_MESSAGE_DEFINITION = "request_msg_definition";
    public static final String CONNECTOR_ERROR = "ConnectorError";
    public static final String ANN_NAME_CONFIG = "serviceConfig";
    public static final String ANN_MESSAGE_LISTENER = "messageListener";

    // Response Message index
    public static final int REQUEST_MESSAGE_INDEX = 1;

    public static final Map<DescriptorProtos.FieldDescriptorProto.Type, Integer> WIRE_TYPE_MAP;
    static {
        Map<DescriptorProtos.FieldDescriptorProto.Type, Integer> wireMap = new HashMap<>();;
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
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, 2);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, 2);
        WIRE_TYPE_MAP = Collections.unmodifiableMap(wireMap);
    }
    // Server Streaming method resources.
    public static final String ON_OPEN_RESOURCE = "onOpen";
    public static final String ON_COMPLETE_RESOURCE = "onComplete";
    public static final String ON_MESSAGE_RESOURCE = "onMessage";
    public static final String ON_ERROR_RESOURCE = "onError";

    public static final String HTTP_TRANSPORT_CONF = "transports.netty.conf";
}
