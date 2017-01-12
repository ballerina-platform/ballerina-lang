/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.ExecutableMultiReturnExpr;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.List;

/**
 * {@code FunctionInvocationExpr} represents function invocation expression
 *
 * @since 1.0.0
 */
public class FunctionInvocationExpr extends AbstractExpression implements ExecutableMultiReturnExpr {

    private SymbolName functionName;
    private List<Expression> expressionList;
    private Expression[] exprs;
    private Function calleeFunction;
    private Position functionInvokedLocation;

    public FunctionInvocationExpr(SymbolName functionName, Expression[] exprs) {
        this.functionName = functionName;
        this.exprs = exprs;
    }

    public SymbolName getFunctionName() {
        return functionName;
    }

    public void setFunctionName(SymbolName symbolName) {
        this.functionName = symbolName;
    }

    public Expression[] getExprs() {
        return exprs;
    }

    public Function getFunction() {
        return calleeFunction;
    }

    public void setFunction(Function function) {
        this.calleeFunction = function;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue[] executeMultiReturn(NodeExecutor executor) {
        return executor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        BValue[] values = executor.visit(this);

        if (calleeFunction.getReturnTypes().length == 0) {
            return null;
        }

        return values[0];
    }
}
