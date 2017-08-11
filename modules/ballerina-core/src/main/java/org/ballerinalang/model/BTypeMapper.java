/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * {@code BTypeMapper} represents a  TypeMapper in ballerina.
 *
 * @since 0.8.0
 */
public class BTypeMapper implements TypeMapper, SymbolScope, CompilationUnit {
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;

    // BLangSymbol related attributes
    protected Identifier identifier;
    protected String pkgPath;
    protected boolean isNative;
    protected SymbolName symbolName;

    private AnnotationAttachment[] annotations;
    private ParameterDef[] parameterDefs;
    private BType[] parameterTypes;
    private ParameterDef[] returnParams;
    private BType[] returnParamTypes;
    private BlockStmt typeMapperBody;
    private int stackFrameSize;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    // Linker related variables
    private int tempStackFrameSize;
    private boolean isFlowBuilderVisited;


    private BTypeMapper(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
    }

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Get all the Annotations associated with a BallerinatypeMapper.
     *
     * @return list of Annotations
     */
    public AnnotationAttachment[] getAnnotations() {
        return annotations;
    }

    /**
     * Get list of Arguments associated with the typeMapper definition.
     *
     * @return list of Arguments
     */
    public ParameterDef[] getParameterDefs() {
        return parameterDefs;
    }

    /**
     * Get all the variableDcls declared in the scope of BallerinaTypeMapper.
     *
     * @return list of all BallerinaTypeMapper scoped variableDcls
     */
    public VariableDef[] getVariableDefs() {
        return null;
    }

    public ParameterDef[] getReturnParameters() {
        return returnParams;
    }

    @Override
    public String getTypeMapperName() {
        return null;
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }

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
        return this.typeMapperBody;
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

    /**
     * Get worker interaction statements related to a callable unit.
     *
     * @return Queue of worker interactions
     */
    @Override
    public Queue<Statement> getWorkerInteractionStatements() {
        return null;
    }

    /**
     * Get the workers defined within a callable unit.
     *
     * @return Array of workers
     */
    @Override
    public Worker[] getWorkers() {
        return new Worker[0];
    }

    @Override
    public void addWorkerDataChannel(WorkerDataChannel workerDataChannel) {

    }

    @Override
    public Map<String, WorkerDataChannel> getWorkerDataChannelMap() {
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

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }


    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        return this.identifier.getName();
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
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
     * {@code BTypeMapperBuilder} is responsible for building a {@code BTypeMapper} node.
     *
     * @since 0.8.0
     */
    public static class BTypeMapperBuilder extends CallableUnitBuilder {
        private BTypeMapper bTypeCon;

        public BTypeMapperBuilder(SymbolScope enclosingScope) {
            bTypeCon = new BTypeMapper(enclosingScope);
            currentScope = bTypeCon;
        }

        public BTypeMapper buildTypeMapper() {
            bTypeCon.location = this.location;
            bTypeCon.whiteSpaceDescriptor = this.whiteSpaceDescriptor;
            bTypeCon.identifier = this.identifier;
            bTypeCon.pkgPath = this.pkgPath;
            bTypeCon.isNative = this.isNative;

            bTypeCon.annotations = this.annotationList.toArray(new AnnotationAttachment[this.annotationList.size()]);
            bTypeCon.parameterDefs = this.parameterDefList.toArray(new ParameterDef[this.parameterDefList.size()]);
            bTypeCon.returnParams = this.returnParamList.toArray(new ParameterDef[this.returnParamList.size()]);
            bTypeCon.typeMapperBody = this.body;
            return bTypeCon;
        }
    }
}
