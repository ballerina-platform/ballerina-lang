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
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.TypeConvertor;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.function.Function;

/**
 * Class to hold the data related to type casting expression
 */
public class TypeCastExpression extends AbstractExpression implements CallableUnitInvocationExpr<TypeConvertor> {

    private Expression sourceExpression;
    private BType targetType;
    private String packageName;
    private SymbolName typeConverterName;
    private TypeConvertor typeConvertor;
    protected Function<BValueType, BValueType> evalFunc;

    public TypeCastExpression(Expression sourceExpression, BType targetType) {
        this.sourceExpression = sourceExpression;
        this.targetType = targetType;
    }

    public TypeCastExpression(String packageName, SymbolName typeConverterName) {
        this.packageName = packageName;
        this.typeConverterName = typeConverterName;
    }

    public Function<BValueType, BValueType> getEvalFunc() {
        return evalFunc;
    }

    public void setEvalFunc(Function<BValueType, BValueType> evalFunc) {
        this.evalFunc = evalFunc;
    }

    public Expression getSourceExpression() {
        return sourceExpression;
    }

    public void setSourceExpression(Expression sourceExpression) {
        this.sourceExpression = sourceExpression;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public SymbolName getTypeConverterName() {
        return typeConverterName;
    }

    public void setTypeConverterName(SymbolName typeConverterName) {
        this.typeConverterName = typeConverterName;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    /**
     * Returns the symbol name of this callable unit invocation expression
     *
     * @return the symbol name
     */
    @Override
    public SymbolName getCallableUnitName() {
        return typeConverterName;
    }

    /**
     * Returns an array of arguments of this callable unit invocation expression
     *
     * @return the array of arguments
     */
    @Override
    public Expression[] getArgExprs() {
        Expression[] expressions = {this.sourceExpression};
        return expressions;
    }

    /**
     * Returns the {@code CallableUnit} linked with this callable unit invocation expression
     *
     * @return the linked {@code CallableUnit}
     */
    @Override
    public TypeConvertor getCallableUnit() {
        return this.typeConvertor;
    }

    /**
     * Sets the {@code CallableUnit}
     *
     * @param callableUnit type of the callable unit
     */
    @Override
    public void setCallableUnit(TypeConvertor callableUnit) {
        this.typeConvertor = callableUnit;

    }

    /**
     * Returns an array of argument types of this callable unit invocation expression
     *
     * @return an array of argument types
     */
    @Override
    public BType[] getTypes() {
        return new BType[0];
    }

    /**
     * Sets an array of argument types
     *
     * @param types array of argument types
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
}
