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
 *
 * @since 1.0.0
 */
public class GrpcConstants {
    public static final int MAX_MESSAGE_SIZE = 16 * 1024 * 1024;
    public static final Context.Key<String> CONTENT_TYPE_KEY = Context.key("content-type");
    public static final Metadata.Key<String> CONTENT_TYPE_MD_KEY = Metadata.Key.of("content-type",
            ASCII_STRING_MARSHALLER);
    
    //gRPC package name.
    public static final String PROTOCOL_PACKAGE_GRPC = "grpc";
    public static final String PROTOCOL_STRUCT_PACKAGE_GRPC = "ballerina.grpc";
    public static final String ORG_NAME = "ballerina";
    //server side endpoint constants.
    public static final String SERVICE_BUILDER = "SERVICE_BUILDER";
    public static final String GRPC_SERVER = "SERVER";
    public static final String SERVICE_ENDPOINT_TYPE = "Service";
    public static final String CLIENT_RESPONDER = "ClientResponder";
    public static final String MESSAGE_CONTEXT = "MessageHeaders";
    public static final String RESPONSE_OBSERVER = "RESPONSE_OBSERVER";
    public static final String RESPONSE_MESSAGE_DEFINITION = "RESPONSE_DEFINITION";
    public static final int CLIENT_RESPONDER_REF_INDEX = 0;
    public static final int RESPONSE_MESSAGE_REF_INDEX = 1;
    
    //client side endpoint constants
    public static final String CLIENT_ENDPOINT_TYPE = "Client";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String CHANNEL_KEY = "channel";
    public static final String ENUM_KEY = "enum";
    public static final String SERVICE_STUB = "ServiceStub";
    public static final String METHOD_DESCRIPTORS = "MethodDescriptors";
    public static final int SERVICE_STUB_REF_INDEX = 0;
    public static final int CLIENT_ENDPOINT_REF_INDEX = 1;
    public static final int DESCRIPTOR_MAP_REF_INDEX = 2;
    public static final int STUB_TYPE_STRING_INDEX = 0;
    public static final int DESCRIPTOR_KEY_STRING_INDEX = 1;
    public static final String BLOCKING_TYPE = "blocking";
    public static final String NON_BLOCKING_TYPE = "non-blocking";
    public static final String REQUEST_SENDER = "REQUEST_SENDER";
    public static final String CLIENT_CONNECTION = "ClientConnection";
    public static final String REQUEST_MESSAGE_DEFINITION = "REQUEST_DEFINITION";
    
    // Names of error structs registered in gRPC package.
    public static final String CONNECTOR_ERROR = "ConnectorError";
    public static final String SERVER_ERROR = "ServerError";
    public static final String CLIENT_ERROR = "ClientError";
    public static final String CLIENT = "Client";
    public static final String ANN_RESOURCE_CONFIG = "resourceConfig";
    public static final String EMPTY_STRING = "";
    public static final String TRUST_FILE = "trustStoreFile";
    public static final String KEY_FILE = "keyStoreFile";
    public static final String ANN_ATTR_RESOURCE_SERVER_STREAM = "streaming";
    // Request Message Param index in service resource.
    public static final int REQUEST_MESSAGE_PARAM_INDEX = 1;
    
    // Response Message Param index in callback service
    public static final int CALLBACK_MESSAGE_PARAM_INDEX = 0;
    
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
    
    public static final String COMPONENT_IDENTIFIER = "grpc";
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
    public static final String BLOB = "blob";
    
    //stub template builder constants
    public static final String COMMA = ",";
    public static final String INPUT_TYPE_NAME = "req";
    public static final String RESULT_TYPE_NAME = "result";
    public static final String IGNORE_CAST = "_";
    public static final String INIT_EMPTY_STUB = "Empty req = {};";
    public static final String EMPTY_DATATYPE_NAME = "Empty";
    public static final String DIAMOND_CAST_OPEN = "<";
    public static final String DIAMOND_CAST_CLOSE = ">";
    public static final String BRACKET_OPEN = "(";
    public static final String BRACKET_CLOSE = ")";
    public static final String SPACE = " ";
    
    //Service Endpoint Config
    public static final String ENDPOINT_CONFIG_HOST = "host";
    public static final String ENDPOINT_CONFIG_TRANSFER_ENCODING = "transferEncoding";
    public static final String ENDPOINT_CONFIG_PORT = "port";
    public static final String ENDPOINT_CONFIG_KEEP_ALIVE = "keepAlive";
    public static final String ENDPOINT_CONFIG_CHUNKING = "chunking";
    public static final String ENDPOINT_CONFIG_VERSION = "httpVersion";
    public static final String ENDPOINT_REQUEST_LIMITS = "requestLimits";
    public static final String REQUEST_LIMITS_MAXIMUM_URL_LENGTH = "maxUriLength";
    public static final String REQUEST_LIMITS_MAXIMUM_HEADER_SIZE = "maxHeaderSize";
    public static final String REQUEST_LIMITS_MAXIMUM_ENTITY_BODY_SIZE = "maxEntityBodySize";
    public static final String ENDPOINT_CONFIG_SECURE_SOCKET = "secureSocket";
    public static final String TARGET_SERVICES = "targets";
    public static final String SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";
    public static final int GRPC_DEFAULT_PORT = 9090;
    
    public static final String ENDPOINT_CONFIG_TRUST_STORE = "trustStore";
    public static final String FILE_PATH = "filePath";
    public static final String PASSWORD = "password";
    public static final String PROTOCOL_VERSION = "name";
    public static final String ENABLED_PROTOCOLS = "versions";
    public static final String ENABLE = "enable";
    public static final String ENDPOINT_CONFIG_OCSP_STAPLING = "ocspStapling";
    public static final String ENDPOINT_CONFIG_KEY_STORE = "keyStore";
    public static final String ENDPOINT_CONFIG_PROTOCOLS = "protocol";
    public static final String ENDPOINT_CONFIG_VALIDATE_CERT = "certValidation";
    
    //SslConfiguration indexes
    public static final String SSL_CONFIG_SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String SSL_CONFIG_CIPHERS = "ciphers";
    public static final String SSL_CONFIG_CACHE_SIZE = "cacheSize";
    public static final String SSL_CONFIG_CACHE_VALIDITY_PERIOD = "cacheValidityPeriod";
    public static final String SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED = "verifyHostname";
    public static final String SSL_CONFIG_ENABLE_SESSION_CREATION = "shareSession";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final String CIPHERS = "ciphers";
    public static final String PKCS_STORE_TYPE = "PKCS12";
    
    //Proxy Indexed
    public static final String PROXY_STRUCT_REFERENCE = "proxy";
    public static final String PROXY_HOST = "host";
    public static final String PROXY_PORT = "port";
    public static final String PROXY_USERNAME = "userName";
    public static final String PROXY_PASSWORD = "password";
    public static final String CLIENT_EP_HTTP_VERSION = "httpVersion";
    
    public static final int CLIENT_ENDPOINT_HOST_INDEX = 0;
    public static final int CLIENT_ENDPOINT_PORT_INDEX = 0;
    
}
