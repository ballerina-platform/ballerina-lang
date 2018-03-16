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
    
    private final StreamObserver<Message> requestSender;
    
    public GrpcCallableUnitCallBack(StreamObserver<Message> requestSender) {
        this.requestSender = requestSender;
    }
    
    public GrpcCallableUnitCallBack() {
        this.requestSender = null;
    }
    
    @Override
    public void notifySuccess() {
        //nothing to do, this will get invoked once resource finished execution
    }
    
    @Override
    public void notifyFailure(BStruct error) {
        MessageUtils.handleFailure(requestSender, error);
    }
}
