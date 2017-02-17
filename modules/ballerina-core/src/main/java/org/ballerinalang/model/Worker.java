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
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * A {@code worker} is a thread of execution that the integration developer programs as a lifeline.
 * <p>
 *
 * Workers are defined as follows:
 *
 *  worker WorkerName (message m) {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      Statement;+
 *      [reply MessageName;]
 *  }
 *
 *  @since 0.8.0
 */
@SuppressWarnings("unused")
public class Worker implements SymbolScope, CompilationUnit, CallableUnit {

    private Future<BMessage> resultFuture;
    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;

    private Annotation[] annotations;
    private ParameterDef[] parameterDefs;
    private BType[] parameterTypes;
    private Worker[] workers;
    private ParameterDef[] returnParams;
    private BType[] returnParamTypes;
    private BlockStmt workerBody;
    private int stackFrameSize;
    private int tempStackFrameSize;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;
    private boolean isFlowBuilderVisited;

    public Worker(String name) {
        this.name = name;
    }

    private Worker(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
    }

    public Worker() {
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the location of this node.
     * <p>
     * {@link NodeLocation} includes the source filename and the line number.
     *
     * @return location of this node
     */
    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }

    /**
     * Returns the name of the callable unit.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPackagePath() {
        return null;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    /**
     * Replaces the symbol name of this callable unit with the specified symbol name.
     *
     * @param symbolName name of the symbol.
     */
    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;

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
        return ScopeName.WORKER;
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

    /**
     * Returns an arrays of annotations attached this callable unit.
     *
     * @return an arrays of annotations
     */
    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    /**
     * Returns an arrays of parameters of this callable unit.
     *
     * @return an arrays of parameters
     */
    @Override
    public ParameterDef[] getParameterDefs() {
        return this.parameterDefs;
    }

    /**
     * Returns an arrays of variable declarations of this callable unit.
     *
     * @return an arrays of variable declarations
     */
    @Override
    public VariableDef[] getVariableDefs() {
        return new VariableDef[0];
    }

    /**
     * Returns an arrays of return parameters (values) of this callable unit.
     *
     * @return an arrays of return parameters
     */
    @Override
    public ParameterDef[] getReturnParameters() {
        return this.returnParams;
    }


    /**
     * Returns size of the stack frame which should be allocated for each invocations.
     *
     * @return size of the stack frame
     */
    @Override
    public int getStackFrameSize() {
        return this.stackFrameSize;
    }

    /**
     * Replaces the size of the current stack frame with the specified size.
     *
     * @param frameSize size of the stack frame
     */
    @Override
    public void setStackFrameSize(int frameSize) {
        this.stackFrameSize = frameSize;
    }

    @Override
    public int getTempStackFrameSize() {
        return this.tempStackFrameSize;
    }

    @Override
    public void setTempStackFrameSize(int frameSize) {
        if (this.tempStackFrameSize > 0 && stackFrameSize != this.tempStackFrameSize) {
            throw new FlowBuilderException("Attempt to Overwrite tempValue Frame size. current :" +
                    this.tempStackFrameSize + ", new :" + stackFrameSize);
        }
        this.tempStackFrameSize = stackFrameSize;
    }

    /**
     * Returns the body of the callable unit as a {@code BlockStmt}.
     *
     * @return body of the callable unit
     */
    @Override
    public BlockStmt getCallableUnitBody() {
        return this.workerBody;
    }

    /**
     * Get Types of the return parameters.
     *
     * @return Types of the return parameters
     */
    @Override
    public BType[] getReturnParamTypes() {
        return this.returnParamTypes;
    }

    /**
     * Sets a {@code BType} arrays containing the types of return parameters of this callable unit.
     *
     * @param returnParamTypes arrays of the return parameters
     */
    @Override
    public void setReturnParamTypes(BType[] returnParamTypes) {
        this.returnParamTypes = returnParamTypes;
    }

    /**
     * Get Types of the return input arguments.
     *
     * @return Types of the return input arguments
     */
    @Override
    public BType[] getArgumentTypes() {
        return new BType[0];
    }

    /**
     * Sets a {@code BType} arrays containing the types of input parameters of this callable unit.
     *
     * @param parameterTypes arrays of the input parameters
     */
    @Override
    public void setParameterTypes(BType[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Future<BMessage> getResultFuture() {
        return resultFuture;
    }

    public void setResultFuture(Future<BMessage> resultFuture) {
        this.resultFuture = resultFuture;
    }

    public boolean isFlowBuilderVisited() {
        return isFlowBuilderVisited;
    }

    public void setFlowBuilderVisited(boolean flowBuilderVisited) {
        isFlowBuilderVisited = flowBuilderVisited;
    }

    /**
     * {@code WorkerBuilder} is responsible for building a {@code Worker} node.
     *
     * @since 0.8.0
     */
    public static class WorkerBuilder extends CallableUnitBuilder {
        private Worker bWorker;

        public WorkerBuilder(SymbolScope enclosingScope) {
            bWorker = new Worker(enclosingScope);
            currentScope = bWorker;
        }

        public Worker buildWorker() {
            bWorker.location = this.location;
            bWorker.name = this.name;
            bWorker.pkgPath = this.pkgPath;
            bWorker.symbolName = new SymbolName(name, pkgPath);

            bWorker.parameterDefs = this.parameterDefList.toArray(new ParameterDef[this.parameterDefList.size()]);
            bWorker.returnParams = this.returnParamList.toArray(new ParameterDef[this.returnParamList.size()]);
            bWorker.workerBody = this.body;
            return bWorker;
        }

    }
}
