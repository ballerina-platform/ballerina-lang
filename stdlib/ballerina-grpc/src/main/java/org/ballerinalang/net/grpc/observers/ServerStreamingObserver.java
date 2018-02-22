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
package org.ballerinalang.net.grpc.observers;

import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.net.grpc.builder.BalGenerate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is Server Streaming Method Implementation for gRPC Service Call.
 */
public class ServerStreamingObserver implements StreamObserver<com.google.protobuf.Message> {
    public static final Logger LOG = LoggerFactory.getLogger(BalGenerate.class);
    private Context context;
    
    public ServerStreamingObserver(Context context) {
        this.context = context;
    }
    
    
    @Override
    public void onNext(com.google.protobuf.Message value) {
        LOG.info(value.toString());
//        BallerinaGrpcServerConnector grpcServerConnector = (BallerinaGrpcServerConnector) ConnectorUtils.
//                getBallerinaServerConnector(context, "ballerina.net.grpc");
    }
    
    @Override
    public void onError(Throwable t) {
        LOG.info("Err 3");
    }
    
    @Override
    public void onCompleted() {
        LOG.info("Done...3");
    }
}
