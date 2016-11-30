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

import org.wso2.ballerina.model.statements.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * A worker is a thread of execution that the integration developer programs as a lifeline.
 * <p>
 *
 * Workers are defined as follows:
 *
 *  worker WorkerName (message m) {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      Statement;+
 *      [reply MessageName;]
 *  }
 */
public class Worker {

    private List<Connection> connections;
    private List<Variable> variables;
    private List<Statement> statements;


    /**
     * Get all Connections declared within the Worker
     *
     * @return list of all the Connections belongs to the Worker
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the Worker
     *
     * @param connections list of connections to be assigned to the Worker
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a Connection to the Worker
     *
     * @param connection Connection to be added to the Worker
     */
    public void addConnection(Connection connection) {
        if (connections == null) {
            connections = new ArrayList<Connection>();
        }
        connections.add(connection);
    }

    /**
     * Get all the variables declared in the Worker
     *
     * @return list of all Worker scoped variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Worker
     *
     * @param variables list of variables
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable to the Worker
     *
     * @param variable variable to be added the Worker
     */
    public void addVariable(Variable variable) {
        if (variables == null) {
            variables = new ArrayList<Variable>();
        }
        variables.add(variable);
    }

    /**
     * Get all the Statements associated with the Worker
     *
     * @return list of Statements associated with the Worker
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Set Statements to be associated with the Worker
     *
     * @param statements list of Statements
     */
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Add a Statement to the Worker
     *
     * @param statement a Statement to be added to the Worker
     */
    public void addStatement(Statement statement) {
        if (statements == null) {
            statements = new ArrayList<Statement>();
        }
        statements.add(statement);
    }

}
