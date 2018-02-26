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
package org.ballerinalang.net.grpc.stubs;

import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.net.grpc.BallerinaGrpcServerConnector;
import org.ballerinalang.net.grpc.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is Stream Observer Implementation for gRPC Client Call.
 */
public class DefaultStreamObserver implements StreamObserver<com.google.protobuf.Message> {
    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultStreamObserver.class);
    
    public DefaultStreamObserver(Context context) {
        BallerinaGrpcServerConnector grpcServerConnector = (BallerinaGrpcServerConnector) ConnectorUtils.
                getBallerinaServerConnector(context, MessageConstants.PROTOCOL_PACKAGE_GRPC);
    }
    
    @Override
    public void onNext(com.google.protobuf.Message value) {
        LOGGER.info(value.toString());

    }
    
    @Override
    public void onError(Throwable t) {
        LOGGER.info("Err 2");
    }
    
    @Override
    public void onCompleted() {
        LOGGER.info("Done...2");
    }
}
