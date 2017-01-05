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
 * A {@code Connector} represents a participant in the integration and is used to interact with an external system.
 * Ballerina includes a set of standard Connectors.
 * <p>
 * A Connector is defined as follows:
 * <p>
 * [ConnectorAnnotations]
 * connector ConnectorName ([ConnectorParamAnnotations]TypeName VariableName[(, TypeName VariableName)*]) {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * ActionDefinition;+
 * }
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class BallerinaConnector extends PositionAwareNode implements Connector , Node {

    private List<Annotation> annotationList;
    private List<Parameter> arguments;
    private List<ConnectorDcl> connectorDclList;
    private List<VariableDcl> variableDclList;

    private SymbolName name;
    private Annotation[] annotations;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private BallerinaAction[] actions;
    private Position connectorLocation;

    public BallerinaConnector(SymbolName serviceName,
                              Position position,
                              Annotation[] annotations,
                              ConnectorDcl[] connectorDcls,
                              VariableDcl[] variableDcls,
                              BallerinaAction[] actions) {
        this.name = serviceName;
        this.connectorLocation = position;
        this.annotations = annotations;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;
        this.actions = actions;

        // Set the connector name for all the actions
        for (Action action : actions) {
            action.getSymbolName().setConnectorName(name.getName());
        }
    }

    /**
     * Get the name of the connector
     *
     * @return name of the connector
     */
    public String getName() {
        return name.getName();
    }

    /**
     * Get the package qualified name
     *
     * @return package qualified name
     */
    public String getPackageQualifiedName() {
        return name.getPkgName() + ":" + name.getName();
    }

    /**
     * Get {@code SymbolName} for Ballerina connector
     *
     * @return Symbol name of Ballerina connector
     */
    public SymbolName getConnectorName() {
        return name;
    }

    /**
     * Get all the Annotations associated with a Connector
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
     * Add an {@code Annotation} to the Connector
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        if (annotationList == null) {
            annotationList = new ArrayList<Annotation>();
        }
        annotationList.add(annotation);
    }

    /**
     * Get list of Arguments associated with the Connector definition
     *
     * @return list of Arguments
     */
    public List<Parameter> getArguments() {
        return arguments;
    }

    /**
     * Set Arguments list to the Connector
     *
     * @param arguments list of Arguments
     */
    public void setArguments(List<Parameter> arguments) {
        this.arguments = arguments;
    }

    /**
     * Add an {@code Argument} to the Connector
     *
     * @param argument Argument to be added to the Connector definition
     */
    public void addArgument(Parameter argument) {
        if (arguments == null) {
            arguments = new ArrayList<Parameter>();
        }
        arguments.add(argument);
    }

    /**
     * Get all Connections declared within the Connector scope
     *
     * @return list of all the Connections belongs to a Service
     */
    public List<ConnectorDcl> getConnectorDcls() {
        return connectorDclList;
    }

    /**
     * Assign connections to the Connector
     *
     * @param connectorDcls list of connections to be assigned to a Connector
     */
    public void setConnectorDcls(List<ConnectorDcl> connectorDcls) {
        this.connectorDclList = connectorDcls;
    }

    /**
     * Add a {@code Connection} to the Connector
     *
     * @param connectorDcl Connection to be added to the Connector
     */
    public void addConnection(ConnectorDcl connectorDcl) {
        if (connectorDclList == null) {
            connectorDclList = new ArrayList<ConnectorDcl>();
        }
        connectorDclList.add(connectorDcl);
    }

    /**
     * Get all the variables declared in the scope of Connector
     *
     * @return list of all Connector scoped variables
     */
    public List<VariableDcl> getVariables() {
        return variableDclList;
    }

    /**
     * Assign variables to the Connector
     *
     * @param variables list of variables
     */
    public void setVariables(List<VariableDcl> variables) {
        this.variableDclList = variables;
    }

    /**
     * Add a {@code Variable} to the Connector
     *
     * @param variable variable to be added to the Connector
     */
    public void addVariable(VariableDcl variable) {
        if (variableDclList == null) {
            variableDclList = new ArrayList<VariableDcl>();
        }
        variableDclList.add(variable);
    }

    /**
     * Get all the Actions can be performed in the Connector
     *
     * @return array of all Actions
     */
    public BallerinaAction[] getActions() {
        return actions;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Position getConnectorLocation() {
        return connectorLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnectorLocation(Position location) {
        this.connectorLocation = location;
    }
}
