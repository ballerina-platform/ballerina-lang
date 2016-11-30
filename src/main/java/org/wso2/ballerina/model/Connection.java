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

    String connectorName;
    List<String> argValues;

    /**
     *
     * @param connectorName Name of the Connector
     */
    public Connection(String connectorName) {
        this.connectorName = connectorName;
    }

    /**
     * Get the name of the Connector
     *
     * @return name of thr Connector
     */
    public String getConnectorName() {
        return connectorName;
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
