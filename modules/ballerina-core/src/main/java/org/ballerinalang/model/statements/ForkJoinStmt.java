/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.statements;

import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.CallableUnit;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.builder.CallableUnitBuilder;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code ForkJoinStmt} represents a fork/join statement.
 *
 * @since 0.8.0
 */
public class ForkJoinStmt extends AbstractStatement implements SymbolScope, CompilationUnit, CallableUnit {
    private int stackFrameSize;
    private Worker[] workers;
    private Join join;
    private Timeout timeout;
    private VariableRefExpr messageReference;
    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;
    private int tempStackFrameSize;

    private ForkJoinStmt(NodeLocation nodeLocation, SymbolScope enclosingScope) {
        super(nodeLocation);
        this.enclosingScope = enclosingScope;
        symbolMap = new HashMap<>();
    }

    /**
     * An inner class represents a join block of a fork-join statement
     */
    public static class Join implements SymbolScope {

        private String joinType;
        private int joinCount;
        private String[] joinWorkers;
        private ParameterDef joinResult;
        private Statement joinBlock;
        private NodeLocation nodeLocation;
        // Scope related variables
        private SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap;

        public Join (NodeLocation nodeLocation, SymbolScope enclosingScope) {
            this.enclosingScope = enclosingScope;
            this.nodeLocation = nodeLocation;
            this.symbolMap = new HashMap<>();
        }


        public NodeLocation getNodeLocation() {
            return nodeLocation;
        }

        public int getJoinCount() {
            return joinCount;
        }

        public String[] getJoinWorkers() {
            return joinWorkers;
        }

        public ParameterDef getJoinResult() {
            return joinResult;
        }

        public String getJoinType() {
            return joinType;
        }

        public Statement getJoinBlock() {
            return joinBlock;
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
    }

    /**
     * An inner class represents a timeout block of a fork-join statement
     */
    public static class Timeout implements SymbolScope {

        private Expression timeoutExpression;
        private ParameterDef timeoutResult;
        private Statement timeoutBlock;
        private NodeLocation nodeLocation;

        // Scope related variables
        private SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap;

        private Timeout(NodeLocation nodeLocation, SymbolScope symbolScope) {
            this.enclosingScope = symbolScope;
            this.nodeLocation = nodeLocation;
            symbolMap = new HashMap<>();
        }


        public NodeLocation getNodeLocation() {
            return nodeLocation;
        }

        public ParameterDef getTimeoutResult() {
            return timeoutResult;
        }



        public Expression getTimeoutExpression() {
            return timeoutExpression;
        }

        public Statement getTimeoutBlock() {
            return timeoutBlock;
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
    }

    public Join getJoin() {
        return join;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public VariableRefExpr getMessageReference() {
        return messageReference;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    /**
     * Returns the name of the callable unit.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return null;
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
     * Returns the symbol name of the callable unit.
     *
     * @return the symbol name
     */
    @Override
    public SymbolName getSymbolName() {
        return null;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return null;
    }

    /**
     * Replaces the symbol name of this callable unit with the specified symbol name.
     *
     * @param symbolName name of the symbol.
     */
    @Override
    public void setSymbolName(SymbolName symbolName) {

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
        return new ParameterDef[0];
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
        return new ParameterDef[0];
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
        return tempStackFrameSize;
    }

    @Override
    public void setTempStackFrameSize(int frameSize) {
        this.tempStackFrameSize = frameSize;
    }

    /**
     * Returns the body of the callable unit as a {@code BlockStmt}.
     *
     * @return body of the callable unit
     */
    @Override
    public BlockStmt getCallableUnitBody() {
        return null;
    }

    /**
     * Get Types of the return parameters.
     *
     * @return Types of the return parameters
     */
    @Override
    public BType[] getReturnParamTypes() {
        return new BType[0];
    }

    /**
     * Sets a {@code BType} arrays containing the types of return parameters of this callable unit.
     *
     * @param returnParamTypes arrays of the return parameters
     */
    @Override
    public void setReturnParamTypes(BType[] returnParamTypes) {

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


    /**
     * Builds a {@code IfElseStmt} statement.
     *
     * @since 0.8.0
     */
    public static class ForkJoinStmtBuilder extends CallableUnitBuilder {
        private NodeLocation location;
        private Worker[] workers;
        private Join join;
        private String joinType;
        private int joinCount;
        private List<String> joinWorkers = new ArrayList<>();
        private ParameterDef joinResult;
        private Statement joinBlock;
        private Timeout timeout;
        private Expression timeoutExpression;
        private ParameterDef timeoutResult;
        private Statement timeoutBlock;
        private VariableRefExpr messageReference;
        private ForkJoinStmt forkJoinStmt;

        public ForkJoinStmtBuilder(SymbolScope enclosingScope) {
            forkJoinStmt = new ForkJoinStmt(location, enclosingScope);
            join = new Join(location, forkJoinStmt);
            timeout = new Timeout(location, forkJoinStmt);
            currentScope = forkJoinStmt;
        }

        public NodeLocation getLocation() {
            return location;
        }

        public void setJoinType(String joinType) {
            this.joinType = joinType;
        }

        public void setJoinCount(int joinCount) {
            this.joinCount = joinCount;
        }

        public void addJoinWorker(String joinWorker) {
            this.joinWorkers.add(joinWorker);
        }

        public void setJoinResult(ParameterDef joinResult) {
            this.joinResult = joinResult;
        }

        public void setJoinBlock(Statement joinBlock) {
            this.joinBlock = joinBlock;
        }

        public void setTimeoutExpression(Expression timeoutExpression) {
            this.timeoutExpression = timeoutExpression;
        }

        public void setTimeoutResult(ParameterDef timeoutResult) {
            this.timeoutResult = timeoutResult;
        }

        public void setTimeoutBlock(Statement timeoutBlock) {
            this.timeoutBlock = timeoutBlock;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setMessageReference(VariableRefExpr messageReference) {
            this.messageReference = messageReference;
        }

        public void setWorkers(Worker[] workers) {
            this.workers = workers;
        }

        public Join getJoin() {
            return join;
        }

        public Timeout getTimeout() {
            return timeout;
        }

        public ForkJoinStmt build() {
            forkJoinStmt.workers = this.workers;
            this.join.joinBlock = this.joinBlock;
            this.join.joinCount = this.joinCount;
            this.join.joinResult = this.joinResult;
            this.join.joinType = this.joinType;
            this.join.joinWorkers = joinWorkers.toArray(new String[joinWorkers.size()]);
            forkJoinStmt.join = this.join;
            this.timeout.timeoutBlock = this.timeoutBlock;
            this.timeout.timeoutExpression = this.timeoutExpression;
            this.timeout.timeoutResult = this.timeoutResult;
            forkJoinStmt.timeout = this.timeout;
            forkJoinStmt.messageReference = this.messageReference;
            forkJoinStmt.location = this.location;
            return forkJoinStmt;
        }
    }
}
