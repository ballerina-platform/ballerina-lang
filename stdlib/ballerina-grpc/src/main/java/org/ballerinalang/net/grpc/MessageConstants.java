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

import io.grpc.Context;
import io.grpc.Metadata;

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
    public static final String CONNECTION = "Connection";
    public static final String RESPONSE_MESSAGE_DEFINITION = "response_msg_definition";
}
