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
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.BType;

import java.util.Map;

/**
 * An {@code Action} is a operation (function) that can be executed against a connector.
 * <p/>
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
 * @since 0.8.0
 */
public class BallerinaAction implements Action, SymbolScope, Node {
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
    private Worker[] workers;
    private ParameterDef[] returnParams;
    private BlockStmt actionBody;
    private int stackFrameSize;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    public BallerinaAction(NodeLocation location,
                           String name,
                           String pkgPath,
                           Boolean isPublic,
                           SymbolName symbolName,
                           Annotation[] annotations,
                           ParameterDef[] parameterDefs,
                           ParameterDef[] returnParams,
                           Worker[] workers,
                           BlockStmt actionBody,
                           SymbolScope enclosingScope,
                           Map<SymbolName, BLangSymbol> symbolMap) {

        this.location = location;
        this.name = name;
        this.pkgPath = pkgPath;
        this.isPublic = isPublic;
        this.symbolName = symbolName;
        this.annotations = annotations;
        this.parameterDefs = parameterDefs;
        this.returnParams = returnParams;
        this.workers = workers;
        this.actionBody = actionBody;

        this.enclosingScope = enclosingScope;
        this.symbolMap = symbolMap;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    public ParameterDef[] getParameterDefs() {
        return parameterDefs;
    }

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    @Override
    public ParameterDef[] getReturnParameters() {
        return returnParams;
    }

    @Override
    public int getStackFrameSize() {
        return stackFrameSize;
    }

    @Override
    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return actionBody;
    }

    public VariableDef[] getVariableDefs() {
        return variableDefs;
    }

    public ConnectorDcl[] getConnectorDcls() {
        return connectorDcls;
    }

    @Override
    public BType[] getReturnParamTypes() {
        return null;
    }

    @Override
    public BType[] getArgumentTypes() {
        return null;
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
    public String getName() {
        return name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
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
        return ScopeName.LOCAL;
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
