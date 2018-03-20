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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for generating ballerina actions.
 */
public class StubBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(StubBuilder.class);
    
    public static void build(ClientBuilder clientStubBal, boolean isBlockingEP) {
        if (isBlockingEP) {
            clientStubBal.addStubFunctionBuilder("Blocking");
            clientStubBal.addStub("Blocking", "blocking");
            clientStubBal.addStubObjects("Blocking", "blocking");
            clientStubBal.addStubFunctionBuilder(null);
            clientStubBal.addStub(null, "non-blocking");
            clientStubBal.addStubObjects(null, null);
        } else {
            clientStubBal.addStubFunctionBuilder(null);
            clientStubBal.addStub(null, "non-blocking");
            clientStubBal.addStubObjects(null, null);
        }
    }
    
}
