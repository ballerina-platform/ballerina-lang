/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A Connection represents the instantiation of a connector with a particular configuration.
 */
@SuppressWarnings("unused")
public class Connection {

    /* Name of the Connector which Connection is instantiated against */
    String connectorName;

    /* Name of the Connection instance */
    String connectionIdentifier;

    List<String> argValues;

    /**
     *
     * @param connectorName Name of the Connector which Connection is instantiated against
     * @param connectionIdentifier Identifier of the Connection instance
     */
    public Connection(String connectorName, String connectionIdentifier) {
        this.connectorName = connectorName;
        this.connectionIdentifier = connectionIdentifier;
    }

    /**
     * Get the name of the Connector which Connection is instantiated against
     *
     * @return name of the Connector
     */
    public String getConnectorName() {
        return connectorName;
    }

    /**
     * Get the identifier of the Connection instance
     *
     * @return identifier of the Connection instance
     */
    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    /**
     * Get values of the arguments
     *
     * @return list of argument values
     */
    public List<String> getArgValues() {
        return argValues;
    }

    /**
     * Assign argument values to the Connection
     *
     * @param argValues list of argument values
     */
    public void setArgValues(List<String> argValues) {
        this.argValues = argValues;
    }

    /**
     * Add an argument value to the Connection
     *
     * @param arg argument value
     */
    public void addArg(String arg) {
        if (argValues == null) {
            argValues = new ArrayList<String>();
        }
        argValues.add(arg);
    }

}
