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

import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;

/**
 * This class is responsible for generating ballerina actions.
 */
public class ActionBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(ActionBuilder.class);
    private String methodName;
    private String reqMessageName;
    private String resMessageName;
    private String methodID;
    private MethodDescriptor.MethodType methodType;
    
    
    public ActionBuilder(String methodName, String reqMessageName, String resMessageName, String methodID,
                         MethodDescriptor.MethodType isStream) {
        this.methodName = methodName;
        this.reqMessageName = reqMessageName;
        this.resMessageName = resMessageName;
        this.methodID = methodID;
        this.methodType = isStream;
    }
    
    
    public String build() {
        String unaryTemplate =
                "    action %s (%s req) (%s, error) {" + NEW_LINE_CHARACTER +
                        "        %s" + NEW_LINE_CHARACTER +
                        "        var res, err1 = ep.execute(%s, \"%s\", \"\");" + NEW_LINE_CHARACTER +
                        "        if (err1 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err1.message};" + NEW_LINE_CHARACTER +
                        "            return null, e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        var response, err2 = (%s)res;" + NEW_LINE_CHARACTER +
                        "        if (err2 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err2.message};" + NEW_LINE_CHARACTER +
                        "            return null, e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        return %s, null;" + NEW_LINE_CHARACTER +
                        "    }";
        String clientStreamTemplate =
                "    action %s (string serviceName) (grpc:ClientConnection, error) {" + NEW_LINE_CHARACTER +
                        "        var res, err1 = ep.execute(\"\", \"%s\", serviceName);" + NEW_LINE_CHARACTER +
                        "        if (err1 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err1.message};" + NEW_LINE_CHARACTER +
                        "            return null, e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        var response, err2 = (grpc:ClientConnection)res;" + NEW_LINE_CHARACTER +
                        "        if (err2 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err2.message};" + NEW_LINE_CHARACTER +
                        "            return null,e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        return response,null;" + NEW_LINE_CHARACTER +
                        "    }";
        String serverStreamTemplate =
                "    action %s (%s req, string serviceName) (error) {" + NEW_LINE_CHARACTER +
                        "        %s" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        var res, err1 = ep.execute(%s, \"%s\", serviceName);" + NEW_LINE_CHARACTER +
                        "        if (err1 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {message:err1.message};" + NEW_LINE_CHARACTER +
                        "            return e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "        return null;" + NEW_LINE_CHARACTER +
                        "    }";
        if (methodType.equals(MethodDescriptor.MethodType.SERVER_STREAMING)) {
            return String.format(serverStreamTemplate, methodName, reqMessageName, "", "req", methodID);
        } else if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING) ||
                methodType.equals(MethodDescriptor.MethodType.BIDI_STREAMING)) {
            return String.format(clientStreamTemplate, methodName, methodID);
        } else {
            return String.format(unaryTemplate, methodName, reqMessageName, resMessageName, "", "req",
                    methodID, resMessageName, "response");
        }
    }
}
