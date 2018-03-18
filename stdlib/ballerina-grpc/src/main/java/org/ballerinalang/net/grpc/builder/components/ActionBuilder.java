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
package org.ballerinalang.net.grpc.builder.components;

import io.grpc.MethodDescriptor.MethodType;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for generating ballerina actions.
 */
public class ActionBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(ActionBuilder.class);
    private String methodName;
    private String reqMessageName;
    private String resMessageName;
    private String methodID;
    private MethodType methodType;
    private ClientStubBal clientStubBal;
    
    public ActionBuilder(String methodName, String reqMessageName, String resMessageName, String methodID,
                         MethodType methodType, ClientStubBal clientStubBal) {
        this.clientStubBal= clientStubBal;
        this.methodName = methodName;
        this.reqMessageName = reqMessageName;
        this.resMessageName = resMessageName;
        this.methodID = methodID;
        this.methodType = methodType;
    }
    
    public void build() {
        switch (methodType) {
            case UNARY: {
                clientStubBal.addBlockingFunction(methodName,reqMessageName, resMessageName, methodID);
                clientStubBal.addStubObjectsGetter("Blocking");
                clientStubBal.addStub("Blocking","blocking");
                clientStubBal.addStubObjects("Blocking","blocking");
                clientStubBal.addNonBlockingFunction(methodName, reqMessageName, methodID);
                clientStubBal.addStubObjectsGetter(null);
                clientStubBal.addStub(null,"non-blocking");
                clientStubBal.addStubObjects(null,null);
                break;
            }
            case SERVER_STREAMING: {
                clientStubBal.addNonBlockingFunction(methodName, reqMessageName, methodID);
                clientStubBal.addStubObjectsGetter(null);
                clientStubBal.addStub(null,"non-blocking");
                clientStubBal.addStubObjects(null,null);
                break;
            }
            case CLIENT_STREAMING:
            case BIDI_STREAMING: {
                clientStubBal.addStreamingFunction(methodName,reqMessageName, methodID);
                clientStubBal.addStubObjectsGetter(null);
                clientStubBal.addStubObjects(null,null);
                clientStubBal.addStub(null,"non-blocking");
                break;
            }
            default: {
                throw new BalGenerationException("Invalid action type '" + methodType + "'.");
            }
        }
    }
    
}
