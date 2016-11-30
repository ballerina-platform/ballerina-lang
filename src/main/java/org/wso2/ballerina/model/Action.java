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
 * Actions are operations (functions) that can be executed against a connector.
 * <p>
 * The structure of an action definition is as follows:
 *  [ActionAnnotations]
 *  action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
 *  [throws exception] {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      WorkerDeclaration;*
 *      Statement;+
 *  }
 *
 */
@SuppressWarnings("unused")
public class Action {

    private List<Annotation> annotations;
    private List<Argument> arguments;
    private List<Connection> connections;
    private List<Variable> variables;
    private List<Worker> workers;
    private List<Statement> statements;

    /**
     * Get all the Annotations associated with an Action
     *
     * @return list of Annotations
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Set list of all the Annotations
     *
     * @param annotations list of Annotations
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Add an Annotation to the Action
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        // Since model is generated sequentially no chance of synchronizing issues
        if (annotations == null) {  // TODO: based on the usage of this and setAnnotation methods we may move this logic
            annotations = new ArrayList<Annotation>();
        }
        annotations.add(annotation);
    }

    /**
     * Get list of Arguments associated with the Action definition
     *
     * @return list of Arguments
     */
    public List<Argument> getArguments() {
        return arguments;
    }

    /**
     * Set Arguments list to the Action
     *
     * @param arguments list of Arguments
     */
    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    /**
     * Add an Argument to the Action
     *
     * @param argument Argument to be added to the Action definition
     */
    public void addArgument(Argument argument) {
        if (arguments == null) {
            arguments = new ArrayList<Argument>();
        }
        arguments.add(argument);
    }

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
        if (connections == null) {
            connections = new ArrayList<Connection>();
        }
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
        if (variables == null) {
            variables = new ArrayList<Variable>();
        }
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
        if (workers == null) {
            workers = new ArrayList<Worker>();
        }
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
        if (statements == null) {
            statements = new ArrayList<Statement>();
        }
        statements.add(statement);
    }

}
