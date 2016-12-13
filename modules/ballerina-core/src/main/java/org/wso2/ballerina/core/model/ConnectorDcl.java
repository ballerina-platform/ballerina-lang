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

package org.wso2.ballerina.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code Connection} represents the instantiation of a connector with a particular configuration.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ConnectorDcl {

    /* Name of the Connector which Connection is instantiated against */
    String connectorName;

    /* Name of the Connection instance */
    Identifier connectionIdentifier;

    List<String> argValues;

    /**
     *
     * @param connectorName Name of the Connector which Connection is instantiated against
     * @param connectionIdentifier Identifier of the Connection instance
     */
    public ConnectorDcl(String connectorName, Identifier connectionIdentifier) {
        this.connectorName = connectorName;
        this.connectionIdentifier = connectionIdentifier;
    }

    /**
     * Get the name of the {@code Connector} which Connection is instantiated against
     *
     * @return name of the Connector
     */
    public String getConnectorName() {
        return connectorName;
    }

    /**
     * Get the {@code Identifier} of the Connection instance
     *
     * @return identifier of the Connection instance
     */
    public Identifier getConnectionIdentifier() {
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
     * Add an {@code Argument} value to the Connection
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
