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

/**
 * Receives notifications from an observable stream of messages.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * <p>It is used by both the client stubs and service implementations for sending or receiving
 * stream messages. It is used for all {@link MethodDescriptor.MethodType}.
 *
 * @since 0.980.0
 */
public interface StreamObserver {

    /**
     * Receives a value from the stream.
     * <p>
     * <p>For unary, called only one time in both client and server side.<p>
     * <p>For server streaming, called only one time in server side and multiple times in client side.<p>
     * <p>For client streaming, called only one time in client side and multiple times in server side.<p>
     * <p>For bidirectional streaming, called multiple times in both client and server side.
     *
     * @param value Request/Response value.
     */
    void onNext(Message value);

    /**
     * Receives a terminating error from the stream.
     * <p>
     * <p>Called when there is an error processing. once called, connection is closed.
     *
     * @param t the error occurred on the stream
     */
    void onError(Message t);

    /**
     * Receives a notification of successful stream completion.
     */
    void onCompleted();
}
