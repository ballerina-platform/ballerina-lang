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
package org.ballerinalang.net.grpc;

import org.ballerinalang.net.grpc.listener.ServerCallHandler;

/**
 * Definition of a service method.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 *
 * @since 0.980.0
 */
public final class ServerMethodDefinition {

    private final MethodDescriptor method;
    private final ServerCallHandler handler;

    private ServerMethodDefinition(MethodDescriptor method, ServerCallHandler handler) {
        this.method = method;
        this.handler = handler;
    }

    /**
     * Create a new instance with method descriptor and server handler.
     *
     * @param method  method descriptor for this method.
     * @param handler server handler to dispatch calls.
     * @return a new instance.
     */
    public static ServerMethodDefinition create(MethodDescriptor method, ServerCallHandler handler) {
        return new ServerMethodDefinition(method, handler);
    }

    /**
     * Returns method descriptor.
     * @return Method descriptor
     */
    public MethodDescriptor getMethodDescriptor() {
        return method;
    }

    /**
     * Returns server handler registered for method.
     *
     * @return server call handler
     */
    public ServerCallHandler getServerCallHandler() {
        return handler;
    }

}
