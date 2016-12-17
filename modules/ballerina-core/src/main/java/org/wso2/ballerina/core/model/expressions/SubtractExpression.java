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

import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.DoubleValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.LongValue;

import java.util.function.BiFunction;

import static org.wso2.ballerina.core.model.Operator.SUB;

/**
 * {@code SubstractExpression} represents a binary substract expression
 *
 * @since 1.0.0
 */
public class SubtractExpression extends BinaryArithmeticExpression {

    public static final BiFunction<BValueRef, BValueRef, BValueRef> SUB_INT_FUNC =
            (lVal, rVal) -> {
                BValue resultVal = new IntValue(lVal.getInt() - rVal.getInt());
                return new BValueRef(resultVal);
            };

    public static final BiFunction<BValueRef, BValueRef, BValueRef> SUB_LONG_FUNC =
            (lVal, rVal) -> {
                BValue bValue = new LongValue(lVal.getLong() - rVal.getLong());
                return new BValueRef(bValue);
            };

    public static final BiFunction<BValueRef, BValueRef, BValueRef> SUB_FLOAT_FUNC =
            (lVal, rVal) -> {
                BValue bValue = new FloatValue(lVal.getFloat() - rVal.getFloat());
                return new BValueRef(bValue);
            };

    public static final BiFunction<BValueRef, BValueRef, BValueRef> SUB_DOUBLE_FUNC =
            (lVal, rVal) -> {
                BValue bValue = new DoubleValue(lVal.getDouble() - rVal.getDouble());
                return new BValueRef(bValue);
            };

    public SubtractExpression(Expression lExpr, Expression rExpr) {
        super(lExpr, SUB, rExpr);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
