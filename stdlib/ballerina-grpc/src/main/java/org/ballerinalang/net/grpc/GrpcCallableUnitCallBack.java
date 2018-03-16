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

import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BStruct;

/**
 * Created by daneshk on 3/12/18.
 */
public class GrpcCallableUnitCallBack implements CallableUnitCallback {

    public static final String EMPTY_MSG_NAME = "empty";
    private final StreamObserver<Message> requestSender;
    private boolean emptyResponse;
    
    public GrpcCallableUnitCallBack(StreamObserver<Message> requestSender, boolean isEmptyResponse) {
        this.requestSender = requestSender;
        this.emptyResponse = isEmptyResponse;
    }

    public GrpcCallableUnitCallBack(StreamObserver<Message> requestSender) {
        this.requestSender = requestSender;
        this.emptyResponse = false;
    }
    
    @Override
    public void notifySuccess() {
        // notify success only if response message is empty. Service impl doesn't send empty message. Empty response
        // scenarios handles here.
        if (emptyResponse) {
            requestSender.onNext(Message.newBuilder(EMPTY_MSG_NAME).build());
        }
    }
    
    @Override
    public void notifyFailure(BStruct error) {
        MessageUtils.handleFailure(requestSender, error);
    }
}
