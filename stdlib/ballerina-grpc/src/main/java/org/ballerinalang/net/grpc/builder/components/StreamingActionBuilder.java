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

import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;

/**
 * This class is responsible for generating ballerina actions.
 */
public class StreamingActionBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(StreamingActionBuilder.class);
    private String methodName;
    private String methodID;
    
    public StreamingActionBuilder(String methodName, String methodID) {
        this.methodName = methodName;
        this.methodID = methodID;
    }
    
    public String build() {
        String actionTemplate =
                "    action %s (string serviceName) (grpc:ClientConnection, error) {" + NEW_LINE_CHARACTER +
                        "        var res, err1 = ep.streamingExecute(\"%s\", serviceName);" + NEW_LINE_CHARACTER +
                        "        if (err1 != null && err1.message != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err1.message};" + NEW_LINE_CHARACTER +
                        "            return null, e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        var response, err2 = (grpc:ClientConnection)res;" + NEW_LINE_CHARACTER +
                        "        if (err2 != null && err2.message != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err2.message};" + NEW_LINE_CHARACTER +
                        "            return null,e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        return response,null;" + NEW_LINE_CHARACTER +
                        "    }";
            return String.format(actionTemplate, methodName, methodID);
    }
}
