/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.util.grpc.server.helloworld;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Simple WebSocket server for Test cases.
 */
public final class GrpcServer implements Runnable {
    
    private io.grpc.ServerBuilder serverBuilder = null;
    private Server server;
    
    public GrpcServer(int port) {
        serverBuilder = NettyServerBuilder.forPort(port)
                .bossEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                        .availableProcessors()))
                .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                        .availableProcessors() * 2));
        serverBuilder.addService(new HelloWorldService());
        
    }
    
    
    /**
     * Start this gRPC server. This will startup all the gRPC services.
     *
     * @throws RuntimeException exception when there is an error in starting the server.
     */
    public Server getServer() throws RuntimeException {
        return server;
    }
    
    /**
     * Shutdown grpc server.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
    
    @Override
    public void run() {
        this.server = serverBuilder.build();
        if (server != null) {
            try {
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
                server.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error while starting gRPC server", e);
            }
        } else {
            throw new RuntimeException("No gRPC service is registered to getServer. You need to register the service");
        }
    }
}
