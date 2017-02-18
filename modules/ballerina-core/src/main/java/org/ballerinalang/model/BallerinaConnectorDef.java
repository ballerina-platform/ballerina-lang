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

package org.ballerinalang.model;

import org.ballerinalang.model.builder.CallableUnitGroupBuilder;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
public class BallerinaConnectorDef extends BType implements Connector, CompilationUnit {
    private NodeLocation location;

    // BLangSymbol related attributes
    protected boolean isPublic;
    protected boolean isNative;
    private Annotation[] annotations;
    private ParameterDef[] parameterDefs;
    private BallerinaAction[] actions;
    private VariableDefStmt[] variableDefStmts;
    private int sizeOfConnectorMem;

    private BallerinaFunction initFunction;

    // Scope related variables
    private Map<SymbolName, BLangSymbol> symbolMap;

    private BallerinaConnectorDef(SymbolScope enclosingScope) {
        super(enclosingScope);
        this.symbolMap = new HashMap<>();
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
     * Get all the Actions can be performed in the Connector.
     *
     * @return arrays of all Actions
     */
    public BallerinaAction[] getActions() {
        return actions;
    }

    public VariableDefStmt[] getVariableDefStmts() {
        return variableDefStmts;
    }

    public BallerinaFunction getInitFunction() {
        return initFunction;
    }

    public void setInitFunction(BallerinaFunction initFunction) {
        this.initFunction = initFunction;
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
        return typeName;
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
        return isNative;
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
        return symbolScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    public BLangSymbol resolveMembers(SymbolName name) {
        return symbolMap.get(name);
    }

    /**
     * {@code BallerinaConnectorDefBuilder} is responsible for building a {@cdoe BallerinaConnectorDef} node.
     *
     * @since 0.8.0
     */
    public static class BallerinaConnectorDefBuilder extends CallableUnitGroupBuilder {
        private BallerinaConnectorDef connectorDef;

        public BallerinaConnectorDefBuilder(SymbolScope enclosingScope) {
            connectorDef = new BallerinaConnectorDef(enclosingScope);
            currentScope = connectorDef;
        }

        public BallerinaConnectorDef buildConnector() {
            this.connectorDef.location = this.location;
            this.connectorDef.typeName = this.name;
            this.connectorDef.pkgPath = this.pkgPath;
            this.connectorDef.symbolName = new SymbolName(name, pkgPath);

            this.connectorDef.annotations = this.annotationList.toArray(new Annotation[this.annotationList.size()]);
            this.connectorDef.parameterDefs = this.parameterDefList.toArray(
                    new ParameterDef[this.parameterDefList.size()]);
            this.connectorDef.actions = this.actionList.toArray(new BallerinaAction[this.actionList.size()]);
            this.connectorDef.variableDefStmts = this.variableDefStmtList.toArray(
                    new VariableDefStmt[variableDefStmtList.size()]);
            this.connectorDef.isNative = this.isNative;
            return connectorDef;
        }
    }
}
