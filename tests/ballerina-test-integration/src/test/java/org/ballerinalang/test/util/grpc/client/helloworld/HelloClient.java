/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.test.util.grpc.client.helloworld;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.ballerinalang.test.util.grpc.helloWorldGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * HelloWorld Client Sample.
 */
public class HelloClient {
    private static final Logger log = LoggerFactory.getLogger(HelloClient.class);
    private final ManagedChannel channel;
    private final helloWorldGrpc.helloWorldBlockingStub blockingStub;
    
    // Construct Client Server Connection.
    public HelloClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }
    
    // Creating Blocking Stub using the existing channel.
    private HelloClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = helloWorldGrpc.newBlockingStub(channel);
    }
    
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    
    // Call Greet Method in Hello server.
    public String greet(String name) {
        StringValue stringValue = StringValue.newBuilder().setValue(name)
                .build();
        StringValue response;
        try {
            response = blockingStub.hello(stringValue);
            log.info("gRPC >> Response Greetings : " + response);
            return response.getValue();
        } catch (StatusRuntimeException e) {
            log.error("Error sending events to blocking stub." , e);
        }
        return "";
    }
}
