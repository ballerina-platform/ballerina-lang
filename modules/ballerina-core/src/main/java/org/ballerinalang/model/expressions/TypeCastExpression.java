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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.TypeMapper;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;

import java.util.function.Function;

/**
 * Class to hold the data related to type casting expression
 *
 * @since 0.8.0
 */
public class TypeCastExpression extends AbstractExpression implements CallableUnitInvocationExpr<TypeMapper> {
    private String name;
    private String pkgName;
    private String pkgPath;
    private SimpleTypeName typeName;
    private Expression rExpr;
    private BType targetType;
    private String packageName;
    private SymbolName typeMapperName;
    private TypeMapper typeMapper;
    protected Function<BValueType, BValueType> evalFuncNewNew;
    private int retuningBranchID;
    private boolean hasReturningBranch;

    public TypeCastExpression(NodeLocation location, Expression rExpr, BType targetType) {
        super(location);
        this.rExpr = rExpr;
        this.targetType = targetType;
    }

    public TypeCastExpression(NodeLocation location, SimpleTypeName typeName, Expression rExpr) {
        super(location);
        this.rExpr = rExpr;
        this.typeName = typeName;
    }

    public Function<BValueType, BValueType> getEvalFunc() {
        return evalFuncNewNew;
    }

    public void setEvalFunc(Function<BValueType, BValueType> evalFuncNewNew) {
        this.evalFuncNewNew = evalFuncNewNew;
    }

    public Expression getRExpr() {
        return rExpr;
    }

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    @Override
    public BType getType() {
        return targetType;
    }

    public BType getTargetType() {
        return targetType;
    }

    public void setTargetType(BType targetType) {
        this.targetType = targetType;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public SymbolName getTypeMapperName() {
        return typeMapperName;
    }

    public void setTypeMapperName(SymbolName typeMapperName) {
        this.typeMapperName = typeMapperName;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackageName() {
        return pkgName;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    /**
     * Returns an arrays of arguments of this callable unit invocation expression
     *
     * @return the arrays of arguments
     */
    @Override
    public Expression[] getArgExprs() {
        Expression[] expressions = {this.rExpr};
        return expressions;
    }

    /**
     * Returns the {@code CallableUnit} linked with this callable unit invocation expression
     *
     * @return the linked {@code CallableUnit}
     */
    @Override
    public TypeMapper getCallableUnit() {
        return this.typeMapper;
    }

    /**
     * Sets the {@code CallableUnit}
     *
     * @param callableUnit type of the callable unit
     */
    @Override
    public void setCallableUnit(TypeMapper callableUnit) {
        this.typeMapper = callableUnit;

    }

    /**
     * Returns an arrays of argument types of this callable unit invocation expression
     *
     * @return an arrays of argument types
     */
    @Override
    public BType[] getTypes() {
        return new BType[0];
    }

    /**
     * Sets an arrays of argument types
     *
     * @param types arrays of argument types
     */
    @Override
    public void setTypes(BType[] types) {

    }

    /**
     * Executes and Returns all the results of this expression
     *
     * @param executor instance of a {@code NodeExecutor}
     * @return results of this expression
     */
    @Override
    public BValue[] executeMultiReturn(NodeExecutor executor) {
        return new BValue[0];
    }

    @Override
    public int getGotoBranchID() {
        return retuningBranchID;
    }

    @Override
    public void setGotoBranchID(int retuningBranchID) {
        this.retuningBranchID = retuningBranchID;
    }

    @Override
    public boolean hasGotoBranchID() {
        return hasReturningBranch;
    }

    @Override
    public void setHasGotoBranchID(boolean hasReturningBranch) {
        this.hasReturningBranch = hasReturningBranch;
    }

}
