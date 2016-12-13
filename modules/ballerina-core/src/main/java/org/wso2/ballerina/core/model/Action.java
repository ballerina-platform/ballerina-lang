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

import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.types.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code Action} is a operation (function) that can be executed against a connector.
 * <p>
 * The structure of an action definition is as follows:
 * [ActionAnnotations]
 * action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
 * [throws exception] {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * WorkerDeclaration;*
 * Statement;+
 * }
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Action {

    private List<Annotation> annotationList;
    private List<Parameter> arguments;
    private List<ConnectorDcl> connectorDclList;
    private List<VariableDcl> variables;
    private List<Worker> workerList;
    private List<Statement> statements;

    private Identifier name;
    private Annotation[] annotations;
    private Parameter[] parameters;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private Worker[] workers;
    private Type[] returnTypes;
    private BlockStmt functionBody;

    public Action(Identifier name,
                  Annotation[] annotations,
                  Parameter[] parameters,
                  Type[] returnTypes,
                  ConnectorDcl[] connectorDcls,
                  VariableDcl[] variableDcls,
                  Worker[] workers,
                  BlockStmt functionBody) {

        this.name = name;
        this.annotations = annotations;
        this.parameters = parameters;
        this.returnTypes = returnTypes;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;
        this.workers = workers;
        this.functionBody = functionBody;
    }

    /**
     * Get all the Annotations associated with an Action
     *
     * @return list of Annotations
     */
    public List<Annotation> getAnnotations() {
        return annotationList;
    }

    /**
     * Set list of all the Annotations
     *
     * @param annotations list of Annotations
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotationList = annotations;
    }

    /**
     * Add an {@code Annotation} to the Action
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        // Since model is generated sequentially no chance of synchronizing issues
        // TODO: based on the usage of this and setAnnotation methods we may move this logic
        if (annotationList == null) {
            annotationList = new ArrayList<Annotation>();
        }
        annotationList.add(annotation);
    }

    /**
     * Get list of Arguments associated with the Action definition
     *
     * @return list of Arguments
     */
    public List<Parameter> getArguments() {
        return arguments;
    }

    /**
     * Set {@code Argument} list to the Action
     *
     * @param arguments list of Arguments
     */
    public void setArguments(List<Parameter> arguments) {
        this.arguments = arguments;
    }

    /**
     * Add an {@code Argument} to the Action
     *
     * @param argument Argument to be added to the Action definition
     */
    public void addArgument(Parameter argument) {
        if (arguments == null) {
            arguments = new ArrayList<Parameter>();
        }
        arguments.add(argument);
    }

    /**
     * Get all Connections declared within the Action scope
     *
     * @return list of all the Connections belongs to a Action
     */
    public List<ConnectorDcl> getConnectorDcls() {
        return connectorDclList;
    }

    /**
     * Assign connections to the Action
     *
     * @param connectorDcls list of connections to be assigned to a Action
     */
    public void setConnectorDcls(List<ConnectorDcl> connectorDcls) {
        this.connectorDclList = connectorDcls;
    }

    /**
     * Add a {@code Connection} to the Action
     *
     * @param connectorDcl Connection to be added to the Action
     */
    public void addConnection(ConnectorDcl connectorDcl) {
        if (connectorDclList == null) {
            connectorDclList = new ArrayList<ConnectorDcl>();
        }
        connectorDclList.add(connectorDcl);
    }

    /**
     * Get all the variables declared in the scope of Action
     *
     * @return list of all Action scoped variables
     */
    public List<VariableDcl> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Action
     *
     * @param variables list of Variables
     */
    public void setVariables(List<VariableDcl> variables) {
        this.variables = variables;
    }

    /**
     * Add a {@code Variable} to the Action
     *
     * @param variable variable to be added to the Action
     */
    public void addVariable(VariableDcl variable) {
        if (variables == null) {
            variables = new ArrayList<VariableDcl>();
        }
        variables.add(variable);
    }

    /**
     * Get all the Workers associated with an Action
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workerList;
    }

    /**
     * Assign Workers to the Action
     *
     * @param workers list of all the Workers
     */
    public void setWorkers(List<Worker> workers) {
        this.workerList = workers;
    }

    /**
     * Add a {@code Worker} to the Action
     *
     * @param worker Worker to be added to the Action
     */
    public void addWorker(Worker worker) {
        if (workerList == null) {
            workerList = new ArrayList<Worker>();
        }
        workerList.add(worker);
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
     * Add a {@code Statement} to the Action
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
