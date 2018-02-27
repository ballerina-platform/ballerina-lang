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
public class ActionBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(ActionBuilder.class);
    private String methodName;
    private String reqMessageName;
    private String resMessageName;
    private String methodID;
    private String reqStructFieldName;
    private String reqStructFieldType;
    private String resStructFieldName;
    private String resStructFieldType;
    
    public ActionBuilder(String methodName, String reqMessageName, String resMessageName, String methodID,
                         String reqStructFieldName, String reqStructFieldType,
                         String resStructFieldName, String resStructFieldType) {
        this.methodName = methodName;
        this.reqMessageName = reqMessageName;
        this.resMessageName = resMessageName;
        this.methodID = methodID;
        this.reqStructFieldName = reqStructFieldName;
        this.reqStructFieldType = reqStructFieldType;
        this.resStructFieldName = resStructFieldName;
        this.resStructFieldType = resStructFieldType;
    }
    
    
    public String build() {
        String templPrt1 = "", templPrt2 = "";
        String template =
                "    action %s (grpc:ClientConnection conn, any req) (any, error) {" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        %s" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        var res, err3 = ep.execute(conn, value, \"%s\");" + NEW_LINE_CHARACTER +
                        "        if (err3 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {msg:err3.msg};" + NEW_LINE_CHARACTER +
                        "            return null, e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        %s" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        return value, null;" + NEW_LINE_CHARACTER +
                        "    }";
        
        if (resStructFieldName != null) {
            String template5 =
                    "var response, err4 = (%s)res;" + NEW_LINE_CHARACTER +
                            "        if (err4 != null) {" + NEW_LINE_CHARACTER +
                            "            %s resObj = {};" + NEW_LINE_CHARACTER +
                            "            var %s, err5 = (%s)req;" + NEW_LINE_CHARACTER +
                            "            if (err5 != null) {" + NEW_LINE_CHARACTER +
                            "                error e = {msg:err5.msg};" + NEW_LINE_CHARACTER +
                            "                return null, e;" + NEW_LINE_CHARACTER +
                            "            }" + NEW_LINE_CHARACTER +
                            "            resObj.%s = %s;" + NEW_LINE_CHARACTER +
                            "            value = resObj;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = response;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            templPrt2 = String.format(template5, resMessageName, resMessageName, resStructFieldName,
                    resStructFieldType, resStructFieldName, resStructFieldName);
        }
        if (reqStructFieldName != null) {
            String template2 =
                    "var request, err = (%s)req;" + NEW_LINE_CHARACTER +
                            "        any value;" + NEW_LINE_CHARACTER +
                            "        if (err != null) {" + NEW_LINE_CHARACTER +
                            "            %s reqObj = {};" + NEW_LINE_CHARACTER +
                            "            var %s, err2 = (%s)req;" + NEW_LINE_CHARACTER +
                            "            if (err2 != null) {" + NEW_LINE_CHARACTER +
                            "                error e = {msg:err2.msg};" + NEW_LINE_CHARACTER +
                            "                return null, e;" + NEW_LINE_CHARACTER +
                            "            }" + NEW_LINE_CHARACTER +
                            "            reqObj.%s = %s;" + NEW_LINE_CHARACTER +
                            "            value = reqObj;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = request;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            templPrt1 = String.format(template2, reqMessageName, reqMessageName, reqStructFieldName,
                    reqStructFieldType, reqStructFieldName, reqStructFieldName);
        }
        if (reqStructFieldName == null) {
            String template3 =
                    "var request, err = (%s)req;" + NEW_LINE_CHARACTER +
                            "        any value;" + NEW_LINE_CHARACTER +
                            "        if (err != null) {" + NEW_LINE_CHARACTER +
                            "            error e = {msg:err.msg};" + NEW_LINE_CHARACTER +
                            "            return null, e;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = request;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            templPrt1 = String.format(template3, reqMessageName);
        }
        if (resStructFieldName == null) {
            String template4 =
                    "var response, err4 = (%s)res;" + NEW_LINE_CHARACTER +
                            "        if (err4 != null) {" + NEW_LINE_CHARACTER +
                            "            error e = {msg:err4.msg};" + NEW_LINE_CHARACTER +
                            "            return null, e;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = response;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            templPrt2 = String.format(template4, resMessageName);
        }
        return String.format(template, methodName, templPrt1, methodID, templPrt2);
    }
}
