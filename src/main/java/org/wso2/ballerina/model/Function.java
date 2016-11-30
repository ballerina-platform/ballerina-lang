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

import java.util.List;

public class Function {

    private List<Connection> connections;
    private List<Variable> variables;
    private List<Worker> workers;
    private List<Statement> statements;

    /**
     * Get all Connections declared within the Function scope
     *
     * @return list of all the Connections belongs to a Function
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the Function
     *
     * @param connections list of connections to be assigned to a Function
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a Connection to the Function
     *
     * @param connection Connection to be added to the Function
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /**
     * Get all the variables declared in the scope of Function
     *
     * @return list of all Function scoped variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Function
     *
     * @param variables list of Function
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable to the Function
     *
     * @param variable variable to be added to the Function
     */
    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    /**
     * Get all the Workers associated with a Function
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workers;
    }

    /**
     * Assign Workers to the Function
     *
     * @param workers list of all the Workers
     */
    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    /**
     * Add a Worker to the Function
     *
     * @param worker Worker to be added to the Resource
     */
    public void addWorker(Worker worker) {
        workers.add(worker);
    }


    /**
     * Get all the Statements associated with the Function
     *
     * @return list of Statements associated with the Function
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Set Statements to be associated with the Function
     *
     * @param statements list of Statements
     */
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Add a Statement to the Function
     *
     * @param statement a Statement to be added to the Function
     */
    public void addStatement(Statement statement) {
        statements.add(statement);
    }


}
