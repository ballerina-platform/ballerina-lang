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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Operator;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;

import java.util.function.BiFunction;

/**
 * {@code UnaryExpression} represents a unary expression.
 *
 * @since 0.8.0
 */
public class UnaryExpression extends AbstractExpression {

    protected Operator op;
    protected Expression rExpr;
    //ToDO this has to be improved property since Unary does not need BiFunction
    private BiFunction<BValue, BValue, BValue> evalFuncNewNew;

    public static final BiFunction<BValue, BValue, BValue> NOT_BOOLEAN_FUNC =
            (lVal, rVal) -> new BBoolean(!((BValueType) rVal).booleanValue());

    public static final BiFunction<BValue, BValue, BValue> NEGATIVE_INT_FUNC =
            (lVal, rVal) -> new BInteger(-(((BValueType) rVal).intValue()));

    public static final BiFunction<BValue, BValue, BValue> POSITIVE_INT_FUNC =
            (lVal, rVal) -> rVal;

    public static final BiFunction<BValue, BValue, BValue> NEGATIVE_FLOAT_FUNC =
            (lVal, rVal) -> new BFloat(-(((BValueType) rVal).floatValue()));

    public static final BiFunction<BValue, BValue, BValue> POSITIVE_FLOAT_FUNC =
            (lVal, rVal) -> rVal;

            
    public BiFunction<BValue, BValue, BValue> getEvalFunc() {
        return evalFuncNewNew;
    }

    public void setEvalFunc(BiFunction<BValue, BValue, BValue> evalFuncNewNew) {
        this.evalFuncNewNew = evalFuncNewNew;
    }
    
    public UnaryExpression(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Operator op,
                           Expression rExpr) {
        super(location, whiteSpaceDescriptor);
        this.op = op;
        this.rExpr = rExpr;
    }

    public Expression getRExpr() {
        return rExpr;
    }

    public Operator getOperator() {
        return op;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }
}
