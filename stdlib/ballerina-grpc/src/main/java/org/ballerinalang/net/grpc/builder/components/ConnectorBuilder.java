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

import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;

/**
 * Class that responsible of generating grpc connector at .bal stub
 */
public class ConnectorBuilder {
    private String actionList;
    private String packageName;
    private String connectorName;
    
    public ConnectorBuilder(String actionList, String packageName, String connectorName) {
        this.actionList = actionList;
        this.packageName = packageName;
        this.connectorName = connectorName;
    }
    
    public String build() {
        String str = "package %s;" + NEW_LINE_CHARACTER +
                "import ballerina.net.grpc;" + NEW_LINE_CHARACTER +
                NEW_LINE_CHARACTER +
                "public connector %s(string host, int port){" + NEW_LINE_CHARACTER +
                " endpoint<grpc:GRPCConnector> ep {" + NEW_LINE_CHARACTER +
                "        create grpc:GRPCConnector(host, port);" + NEW_LINE_CHARACTER +
                "    }" + NEW_LINE_CHARACTER +
                NEW_LINE_CHARACTER +
                "    action connect () (grpc:ClientConnection, error) {" + NEW_LINE_CHARACTER +
                "        var resp, convErr = ep.connect(descriptorKey,\"{{stubType}}\",descriptorMap);"
                + NEW_LINE_CHARACTER +
                "        var conn, er = <grpc:ClientConnection>resp;" + NEW_LINE_CHARACTER +
                "        var connErr, er = <grpc:ConnectorError>convErr;" + NEW_LINE_CHARACTER +
                "        if (connErr != null) {" + NEW_LINE_CHARACTER +
                "            println(\"Error: \" + connErr.msg);" + NEW_LINE_CHARACTER +
                "            error err = {msg:connErr.msg};" + NEW_LINE_CHARACTER +
                "            return null,err;" + NEW_LINE_CHARACTER +
                "        }" + NEW_LINE_CHARACTER +
                "        return conn, null;" + NEW_LINE_CHARACTER +
                "    }" + NEW_LINE_CHARACTER +
                "%s" +
                NEW_LINE_CHARACTER +
                "}" + NEW_LINE_CHARACTER;
        return String.format(str, packageName, connectorName, actionList);
    }
}
