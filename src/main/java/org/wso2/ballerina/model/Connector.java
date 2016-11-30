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

import java.util.List;

/**
 * A connector represents a participant in the integration and is used to interact with an external system.
 * Ballerina includes a set of standard connectors.
 * <p>
 * A connector is defined as follows:
 *
 * [ConnectorAnnotations]
 * connector ConnectorName ([ConnectorParamAnnotations]TypeName VariableName[(, TypeName VariableName)*]) {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      ActionDefinition;+
 * }
 */
public class Connector {

    private List<Connection> connections;
    private List<Variable> variables;
    private List<Action> actions;

    /**
     * Get all Connections declared within the Connector scope
     *
     * @return list of all the Connections belongs to a Service
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the Connector
     *
     * @param connections list of connections to be assigned to a Connector
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a Connection to the Connector
     *
     * @param connection Connection to be added to the Connector
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /**
     * Get all the variables declared in the scope of Connector
     *
     * @return list of all Connector scoped variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Connector
     *
     * @param variables list of variables
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable to the Connector
     *
     * @param variable variable to be added to the Connector
     */
    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    /**
     * Get all the Actions can be performed in the Connector
     *
     * @return list of all Actions
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Set list of Actions to the Connector
     *
     * @param actions list of Actions
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * Add an Action to the Connector
     *
     * @param action Action to be added to the Connector
     */
    public void addAction(Action action) {
        actions.add(action);
    }

}
