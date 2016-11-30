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
 * A Connector represents a participant in the integration and is used to interact with an external system.
 * Ballerina includes a set of standard Connectors.
 * <p>
 * A Connector is defined as follows:
 *
 * [ConnectorAnnotations]
 * connector ConnectorName ([ConnectorParamAnnotations]TypeName VariableName[(, TypeName VariableName)*]) {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      ActionDefinition;+
 * }
 */
@SuppressWarnings("unused")
public class Connector {

    private List<Annotation> annotations;
    private List<Argument> arguments;
    private List<Connection> connections;
    private List<Variable> variables;
    private List<Action> actions;

    /**
     * Get all the Annotations associated with a Connector
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
     * Add an Annotation to the Connector
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new ArrayList<Annotation>();
        }
        annotations.add(annotation);
    }

    /**
     * Get list of Arguments associated with the Connector definition
     *
     * @return list of Arguments
     */
    public List<Argument> getArguments() {
        return arguments;
    }

    /**
     * Set Arguments list to the Connector
     *
     * @param arguments list of Arguments
     */
    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    /**
     * Add an Argument to the Connector
     *
     * @param argument Argument to be added to the Connector definition
     */
    public void addArgument(Argument argument) {
        if (arguments == null) {
            arguments = new ArrayList<Argument>();
        }
        arguments.add(argument);
    }

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
        if (connections == null) {
            connections = new ArrayList<Connection>();
        }
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
        if (variables == null) {
            variables = new ArrayList<Variable>();
        }
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
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        actions.add(action);
    }

}
