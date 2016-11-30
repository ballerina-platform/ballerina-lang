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

public class Action {

    private List<Connection> connections;
    private List<Variable> variables;
    private List<Worker> workers;
    private List<Statement> statements;

    /**
     * Get all Connections declared within the Action scope
     *
     * @return list of all the Connections belongs to a Action
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the Action
     *
     * @param connections list of connections to be assigned to a Action
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a Connection to the Action
     *
     * @param connection Connection to be added to the Action
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /**
     * Get all the variables declared in the scope of Action
     *
     * @return list of all Action scoped variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Action
     *
     * @param variables list of Variables
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable to the Action
     *
     * @param variable variable to be added to the Action
     */
    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    /**
     * Get all the Workers associated with an Action
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workers;
    }

    /**
     * Assign Workers to the Action
     *
     * @param workers list of all the Workers
     */
    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    /**
     * Add a Worker to the Action
     *
     * @param worker Worker to be added to the Action
     */
    public void addWorker(Worker worker) {
        workers.add(worker);
    }

    /**
     * Get all the Statements associated with the Action
     *
     * @return list of Statements associated with the Action
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Set Statements to be associated with the Action
     *
     * @param statements list of Statements
     */
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Add a Statement to the Action
     *
     * @param statement a Statement to be added to the Action
     */
    public void addStatement(Statement statement) {
        statements.add(statement);
    }

}
