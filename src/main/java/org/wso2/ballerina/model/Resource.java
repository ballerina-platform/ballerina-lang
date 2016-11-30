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

public class Resource {

    private List<Connection> connections;
    private List<Variable> variables;
    private List<Worker> workers;
    private List<Statement> statements;

    /**
     * Get all Connections declared within the default Worker scope of the Resource
     *
     * @return list of all the Connections belongs to the default Worker of the Resource
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the default Worker of the Resource
     *
     * @param connections list of connections to be assigned to the default Worker of the Resource
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a Connection to the default Worker of the Resource
     *
     * @param connection Connection to be added to the default Worker of the Resource
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /**
     * Get all the variables declared in the default Worker scope of the Resource
     *
     * @return list of all default Worker scoped variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the default Worker of the Resource
     *
     * @param variables list of variables
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable to the default Worker of the Resource
     *
     * @param variable variable to be added default Worker
     */
    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    /**
     * Get all the Workers associated with a Resource
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workers;
    }

    /**
     * Assign Workers to the Resource
     *
     * @param workers list of all the Workers
     */
    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    /**
     * Add a Worker to the Resource
     *
     * @param worker Worker to be added to the Resource
     */
    public void addWorker(Worker worker) {
        workers.add(worker);
    }

    /**
     * Get all the Statements associated with the default Worker
     *
     * @return list of Statements associated with the default Worker
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Set Statements to be associated with the default Worker
     *
     * @param statements list of Statements
     */
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Add a Statement to the default Worker in the Resource
     *
     * @param statement a Statement to be added to the default Worker
     */
    public void addStatement(Statement statement) {
        statements.add(statement);
    }

}
