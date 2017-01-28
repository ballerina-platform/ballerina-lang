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
 * @since 0.8.0
 */
public class BallerinaConnector implements Connector, CompilationUnit {

    private SymbolName name;
    private Annotation[] annotations;
    private Parameter[] parameters;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private BallerinaAction[] actions;
    private Position connectorLocation;

    private int sizeOfConnectorMem;

    public BallerinaConnector(SymbolName serviceName,
                              Position position,
                              Annotation[] annotations,
                              Parameter[] parameters,
                              ConnectorDcl[] connectorDcls,
                              VariableDcl[] variableDcls,
                              BallerinaAction[] actions) {
        this.name = serviceName;
        this.connectorLocation = position;
        this.parameters = parameters;
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
     * Get the name of the connector.
     *
     * @return name of the connector
     */
    public String getName() {
        return name.getName();
    }

    /**
     * Get the package qualified name.
     *
     * @return package qualified name
     */
    public String getPackageQualifiedName() {
        return name.getPkgName() + ":" + name.getName();
    }

    /**
     * Get {@code SymbolName} for Ballerina connector.
     *
     * @return Symbol name of Ballerina connector
     */
    public SymbolName getConnectorName() {
        return name;
    }

    /**
     * Get all the Annotations associated with a Connector.
     *
     * @return list of Annotations
     */
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * Get all Connections declared within the Connector scope.
     *
     * @return list of all the Connections belongs to a Service
     */
    public ConnectorDcl[] getConnectorDcls() {
        return connectorDcls;
    }

    /**
     * Get all the variables declared in the scope of Connector.
     *
     * @return list of all Connector scoped variables
     */
    public VariableDcl[] getVariableDcls() {
        return variableDcls;
    }

    /**
     * Get all the Actions can be performed in the Connector.
     *
     * @return array of all Actions
     */
    public BallerinaAction[] getActions() {
        return actions;
    }

    public void setSizeOfConnectorMem(int sizeOfConnectorMem) {
        this.sizeOfConnectorMem = sizeOfConnectorMem;
    }

    public int getSizeOfConnectorMem() {
        return sizeOfConnectorMem;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getLocation() {
        return connectorLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocation(Position location) {
        this.connectorLocation = location;
    }
}
