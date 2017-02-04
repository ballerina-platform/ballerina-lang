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

import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.Map;

import static org.wso2.ballerina.core.model.types.TypeConstants.CONNECTOR_TNAME;

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
public class BallerinaConnectorDef extends BType implements Connector, SymbolScope, CompilationUnit {
    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;

    private Annotation[] annotations;
    private ParameterDef[] parameterDefs;
    private ConnectorDcl[] connectorDcls;
    private VariableDef[] variableDefs;
    private BallerinaAction[] actions;
    private int sizeOfConnectorMem;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    public BallerinaConnectorDef(NodeLocation location,
                                 String name,
                                 String pkgPath,
                                 Boolean isPublic,
                                 SymbolName symbolName,
                                 Annotation[] annotations,
                                 ParameterDef[] parameterDefs,
                                 ConnectorDcl[] connectorDcls,
                                 VariableDef[] variableDefs,
                                 BallerinaAction[] actions,
                                 SymbolScope enclosingScope,
                                 Map<SymbolName, BLangSymbol> symbolMap) {

        super(CONNECTOR_TNAME, pkgPath, enclosingScope, BConnector.class);

        this.location = location;
        this.name = name;
        this.pkgPath = pkgPath;
        this.isPublic = isPublic;
        this.symbolName = symbolName;

        this.parameterDefs = parameterDefs;
        this.annotations = annotations;
        this.connectorDcls = connectorDcls;
        this.variableDefs = variableDefs;
        this.actions = actions;

        this.enclosingScope = enclosingScope;
        this.symbolMap = symbolMap;

        // Set the connector name for all the actions
        // TODO Figure out a way to handle this
//        for (Action action : actions) {
//            action.getSymbolName().setConnectorName(name.getName());
//        }
    }

    /**
     * Get all the Annotations associated with a Connector.
     *
     * @return list of Annotations
     */
    public Annotation[] getAnnotations() {
        return annotations;
    }

    public ParameterDef[] getParameterDefs() {
        return parameterDefs;
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
    public VariableDef[] getVariableDefs() {
        return variableDefs;
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


    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }


    // Methods in BLangSymbol interface

    @Override
    public <V extends BValue> V getDefaultValue() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return this;
    }


    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.CONNECTOR;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }
}
