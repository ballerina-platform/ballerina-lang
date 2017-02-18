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

import org.ballerinalang.model.builder.CallableUnitBuilder;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.Collections;
import java.util.HashMap;
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
    protected boolean isNative;

    private Annotation[] annotations;
    private ParameterDef[] parameterDefs;
    private BType[] parameterTypes;
    private Worker[] workers;
    private ParameterDef[] returnParams;
    private BType[] returnParamTypes;
    private BlockStmt actionBody;
    private BallerinaConnectorDef connectorDef;
    private int stackFrameSize;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    // Linker related variables
    private int tempStackFrameSize;
    private boolean isFlowBuilderVisited;

    private BallerinaAction(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
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
    public int getTempStackFrameSize() {
        return tempStackFrameSize;
    }

    @Override
    public void setTempStackFrameSize(int stackFrameSize) {
        if (this.tempStackFrameSize > 0 && stackFrameSize != this.tempStackFrameSize) {
            throw new FlowBuilderException("Attempt to Overwrite tempValue Frame size. current :" +
                    this.tempStackFrameSize + ", new :" + stackFrameSize);
        }
        this.tempStackFrameSize = stackFrameSize;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return actionBody;
    }

    public VariableDef[] getVariableDefs() {
        return null;
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public ConnectorDcl[] getConnectorDcls() {
        return null;
    }

    @Override
    public BType[] getReturnParamTypes() {
        return returnParamTypes;
    }

    @Override
    public void setReturnParamTypes(BType[] returnParamTypes) {
        this.returnParamTypes = returnParamTypes;
    }

    @Override
    public BType[] getArgumentTypes() {
        return parameterTypes;
    }

    @Override
    public void setParameterTypes(BType[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public BallerinaConnectorDef getConnectorDef() {
        return connectorDef;
    }

    public void setConnectorDef(BallerinaConnectorDef connectorDef) {
        this.connectorDef = connectorDef;
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

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    public boolean isFlowBuilderVisited() {
        return isFlowBuilderVisited;
    }

    public void setFlowBuilderVisited(boolean flowBuilderVisited) {
        isFlowBuilderVisited = flowBuilderVisited;
    }

    /**
     * {@code BallerinaActionBuilder} is responsible for building a {@cdoe BallerinaAction} node.
     *
     * @since 0.8.0
     */
    public static class BallerinaActionBuilder extends CallableUnitBuilder {
        private BallerinaAction bAction;

        public BallerinaActionBuilder(SymbolScope enclosingScope) {
            bAction = new BallerinaAction(enclosingScope);
            currentScope = bAction;
        }

        public BallerinaAction buildAction() {
            bAction.location = this.location;
            bAction.name = this.name;
            bAction.pkgPath = this.pkgPath;
            bAction.symbolName = new SymbolName(name, pkgPath);

            bAction.annotations = this.annotationList.toArray(new Annotation[this.annotationList.size()]);
            bAction.parameterDefs = this.parameterDefList.toArray(new ParameterDef[this.parameterDefList.size()]);
            bAction.returnParams = this.returnParamList.toArray(new ParameterDef[this.returnParamList.size()]);
            bAction.workers = this.workerList.toArray(new Worker[this.workerList.size()]);
            bAction.actionBody = this.body;
            bAction.isNative = this.isNative;
            return bAction;
        }
    }
}
