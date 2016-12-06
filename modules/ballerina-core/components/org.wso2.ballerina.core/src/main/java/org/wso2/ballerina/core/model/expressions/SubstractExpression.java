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

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.DoubleValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.LongValue;
import org.wso2.ballerina.core.utils.TriFunction;

/**
 * {@code SubstractExpression} represents a binary substract expression
 *
 * @since 1.0.0
 */
public class SubstractExpression extends BinaryArithmeticExpression {

    public static final TriFunction<Context, Expression, Expression, BValueRef> SUB_INT_FUNC =
            (ctx, lExpr, rExpr) -> {
                IntValue lValue = (IntValue) lExpr.evaluate(ctx).getBValue();
                IntValue rValue = (IntValue) rExpr.evaluate(ctx).getBValue();
                BValue bValue = new IntValue(lValue.getValue() - rValue.getValue());
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> SUB_LONG_FUNC =
            (ctx, lExpr, rExpr) -> {
                LongValue lValue = (LongValue) lExpr.evaluate(ctx).getBValue();
                LongValue rValue = (LongValue) rExpr.evaluate(ctx).getBValue();
                BValue bValue = new LongValue(lValue.getValue() - rValue.getValue());
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> SUB_FLOAT_FUNC =
            (ctx, lExpr, rExpr) -> {
                FloatValue lValue = (FloatValue) lExpr.evaluate(ctx).getBValue();
                FloatValue rValue = (FloatValue) rExpr.evaluate(ctx).getBValue();
                BValue bValue = new FloatValue(lValue.getValue() - rValue.getValue());
                return new BValueRef(bValue);
            };

    public static final TriFunction<Context, Expression, Expression, BValueRef> SUB_DOUBLE_FUNC =
            (ctx, lExpr, rExpr) -> {
                DoubleValue lValue = (DoubleValue) lExpr.evaluate(ctx).getBValue();
                DoubleValue rValue = (DoubleValue) rExpr.evaluate(ctx).getBValue();
                BValue bValue = new DoubleValue(lValue.getValue() - rValue.getValue());
                return new BValueRef(bValue);
            };

    public SubstractExpression(Expression lExpr, Operator op, Expression rExpr) {
        super(lExpr, op, rExpr);
    }
}
