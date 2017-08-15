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
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * A {@code BallerinaFunction} is an operation that is executed by a {@code Worker}.
 * <p>
 * The structure of a BallerinaFunction is as follows:
 * <p>
 * [FunctionAnnotations]
 * [public] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
 * ((TypeName[(, TypeName)*])?) [throws exception] {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * WorkerDeclaration;*
 * Statement;+
 * }
 *
 * @since 0.8.0
 */
public class BallerinaFunction implements Function, SymbolScope, CompilationUnit {
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;

    // BLangSymbol related attributes
    protected Identifier identifier;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;
    protected boolean isNative;
    protected boolean isLambda = false;
    private boolean hasReturnsKeyword;

    private AnnotationAttachment[] annotations;
    private ParameterDef[] parameterDefs;
    private BType[] parameterTypes;
    private Worker[] workers;
    private Queue<Statement> workerInteractionStatements;
    private ParameterDef[] returnParameters;
    private BType[] returnParamTypes;
    private BlockStmt functionBody;
    private int stackFrameSize;

    private Map<String, WorkerInfo> workerInfoMap = new HashMap<>();

    // Key -  workerDataChannelName
    private Map<String, WorkerDataChannel> workerDataChannelMap = new HashMap<>();

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    private NativeUnitProxy nativeFunction;

    // Linker related variables
    private int tempStackFrameSize;
    private BType bType;

    private BallerinaFunction(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
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

    /**
     * Get all the Workers associated with a BallerinaFunction.
     *
     * @return list of Workers
     */
    @Override
    public Worker[] getWorkers() {
        return workers;
    }

    @Override
    public void addWorkerDataChannel(WorkerDataChannel workerDataChannel) {
        workerDataChannelMap.put(workerDataChannel.getChannelName(), workerDataChannel);
    }

    @Override
    public Map<String, WorkerDataChannel> getWorkerDataChannelMap() {
        return workerDataChannelMap;
    }

    @Override
    public Queue<Statement> getWorkerInteractionStatements() {
        return workerInteractionStatements;
    }

    /**
     * Get all Connections declared within the BallerinaFunction scope.
     *
     * @return list of all the Connections belongs to a BallerinaFunction
     */
    public ConnectorDcl[] getConnectorDcls() {
        return null;
    }

    public void setNativeFunction(NativeUnitProxy nativeFunction) {
        this.nativeFunction = nativeFunction;
    }

    public NativeUnitProxy getNativeFunction() {
        return nativeFunction;
    }

    // Methods in CallableUnit interface

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Get all the Annotations associated with a BallerinaFunction.
     *
     * @return list of Annotations
     */
    @Override
    public AnnotationAttachment[] getAnnotations() {
        return this.annotations;
    }

    /**
     * Get list of Arguments associated with the function definition.
     *
     * @return list of Arguments
     */
    public ParameterDef[] getParameterDefs() {
        return this.parameterDefs;
    }

    /**
     * Get all the variableDcls declared in the scope of BallerinaFunction.
     *
     * @return list of all BallerinaFunction scoped variableDcls
     */
    @Override
    public VariableDef[] getVariableDefs() {
        return null;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return this.functionBody;
    }

    @Override
    public ParameterDef[] getReturnParameters() {
        return this.returnParameters;
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
        return identifier.getName();
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

    @Override
    public BType getType() {
        if (bType == null) {
            BFunctionType functionType = new BFunctionType(this.getSymbolScope().getEnclosingScope(), parameterTypes,
                    returnParamTypes);
            bType = functionType;
        }
        return bType;
    }

    @Override
    public void setType(BType type) {
    }

    @Override
    public Kind getKind() {
        return null;
    }

    @Override
    public void setKind(Kind kind) {
    }

    @Override
    public int getVarIndex() {
        return 0;
    }

    @Override
    public void setVarIndex(int index) {
    }

    @Override
    public SimpleTypeName getTypeName() {
        return null;
    }

    public boolean isLambda() {
        return isLambda;
    }

    public void setLambda(boolean lambda) {
        isLambda = lambda;
    }

    public boolean hasReturnsKeyword() {
        return hasReturnsKeyword;
    }

    public void setHasReturnsKeyword(boolean hasReturnsKeyword) {
        this.hasReturnsKeyword = hasReturnsKeyword;
    }

    /**
     * {@code BallerinaFunctionBuilder} is responsible for building a {@code BallerinaFunction} node.
     *
     * @since 0.8.0
     */
    public static class BallerinaFunctionBuilder extends CallableUnitBuilder {
        private BallerinaFunction bFunc;

        public BallerinaFunctionBuilder(SymbolScope enclosingScope) {
            bFunc = new BallerinaFunction(enclosingScope);
            currentScope = bFunc;
        }

        public BallerinaFunction buildFunction() {
            bFunc.location = this.location;
            bFunc.whiteSpaceDescriptor = this.whiteSpaceDescriptor;
            bFunc.identifier = this.identifier;
            bFunc.pkgPath = this.pkgPath;
            bFunc.symbolName = new SymbolName(identifier.getName(), pkgPath);

            bFunc.annotations = this.annotationList.toArray(new AnnotationAttachment[this.annotationList.size()]);
            bFunc.parameterDefs = this.parameterDefList.toArray(new ParameterDef[this.parameterDefList.size()]);
            bFunc.returnParameters = this.returnParamList.toArray(new ParameterDef[this.returnParamList.size()]);
            // Set the parameters to the workers if there are any
            for (Worker worker : this.workerList) {
                worker.setParameterDefs(bFunc.getParameterDefs());
            }
            bFunc.workers = this.workerList.toArray(new Worker[this.workerList.size()]);
            bFunc.workerInteractionStatements = this.workerInteractionStatements;
            bFunc.functionBody = this.body;
            bFunc.isNative = this.isNative;
            return bFunc;
        }
    }
}
