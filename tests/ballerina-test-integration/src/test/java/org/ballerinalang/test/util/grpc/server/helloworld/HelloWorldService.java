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

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.test.util.grpc.helloWorldGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldService extends helloWorldGrpc.helloWorldImplBase {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldService.class);
    private static String name;
    
    @Override
    public void hello(StringValue request, StreamObserver<StringValue> responseObserver) {
        name = request.getValue();
        log.info(COMPONENT_IDENTIFIER + " >> Server receive request '" + name + "'");
        StringValue response = StringValue.newBuilder().setValue("Get message from: " + name).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    public static String getName() {
        return name;
    }
}

