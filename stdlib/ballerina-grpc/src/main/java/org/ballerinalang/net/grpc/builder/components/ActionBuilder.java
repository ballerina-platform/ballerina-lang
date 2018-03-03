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

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.BLOCKING_STUB_KEY;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.NON_BLOCKING_STUB_KEY;

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
    private Map<String, String> actionListMap = new HashMap<>();
    
    public ActionBuilder(String methodName, String reqMessageName, String resMessageName, String methodID,
                         MethodType methodType) {
        this.methodName = methodName;
        this.reqMessageName = reqMessageName;
        this.resMessageName = resMessageName;
        this.methodID = methodID;
        this.methodType = methodType;
        this.actionListMap.put(BLOCKING_STUB_KEY, "");
        this.actionListMap.put(NON_BLOCKING_STUB_KEY, "");
    }
    
    public void build() {
        switch (methodType) {
            case UNARY: {
                this.actionListMap.put(BLOCKING_STUB_KEY,
                        new BlockingActionBuilder(methodName, reqMessageName, resMessageName, methodID).build());
                this.actionListMap.put(NON_BLOCKING_STUB_KEY, new NonBlockingActionBuilder(methodName, reqMessageName,
                        resMessageName, methodID).build());
                break;
            }
            case CLIENT_STREAMING: {
                this.actionListMap.put(NON_BLOCKING_STUB_KEY, new NonBlockingActionBuilder(methodName, reqMessageName,
                        resMessageName, methodID).build());
                break;
            }
            case SERVER_STREAMING:
            case BIDI_STREAMING: {
                this.actionListMap.put(NON_BLOCKING_STUB_KEY, new StreamingActionBuilder(methodName, methodID).build());
                break;
            }
            default: {
                throw new BalGenerationException("Invalid action type '" + methodType + "'.");
            }
        }
    }
    
    public String getBlockingAction() {
        return actionListMap.get(BLOCKING_STUB_KEY);
    }
    
    public String getNonBlockingAction() {
        return actionListMap.get(NON_BLOCKING_STUB_KEY);
    }
}
