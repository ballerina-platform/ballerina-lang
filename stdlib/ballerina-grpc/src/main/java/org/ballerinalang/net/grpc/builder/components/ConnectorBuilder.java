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
    private String connectorName;
    private String stubType;
    
    public ConnectorBuilder(String actionList, String connectorName, String stubType) {
        this.actionList = actionList;
        this.connectorName = connectorName;
        this.stubType = stubType;
    }
    
    public String build() {
        String str =
                "public connector %s(string host, int port){" + NEW_LINE_CHARACTER +
                " endpoint<grpc:GRPCConnector> ep {" + NEW_LINE_CHARACTER +
                "        create grpc:GRPCConnector(host, port, \"%s\", descriptorKey, descriptorMap);"
                + NEW_LINE_CHARACTER +
                "    }" + NEW_LINE_CHARACTER +
                NEW_LINE_CHARACTER +
                "%s" +
                NEW_LINE_CHARACTER +
                "}" + NEW_LINE_CHARACTER;
        return String.format(str, connectorName, stubType, actionList);
    }
}
