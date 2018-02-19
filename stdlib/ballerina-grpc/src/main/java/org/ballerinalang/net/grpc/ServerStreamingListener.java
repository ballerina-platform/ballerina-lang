/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.net.grpc;

import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

/**
 * This is Server Streaming Method Implementation for gRPC Service Call.
 */
public class ServerStreamingListener implements ServerCalls.ServerStreamingMethod {

    @Override
    public void invoke(Object request, StreamObserver responseObserver) {

    }
}
