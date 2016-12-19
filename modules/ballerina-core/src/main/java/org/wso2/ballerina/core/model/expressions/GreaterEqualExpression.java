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
import org.wso2.ballerina.core.model.values.BooleanValue;

import java.util.function.BiFunction;

import static org.wso2.ballerina.core.model.Operator.GREATER_EQUAL;

/**
 * {@code GreaterEqualExpression} represents a greater than or equal (>=) expression in Ballerina
 *
 * @since 1.0.0
 */
public class GreaterEqualExpression extends BinaryCompareExpression {

    public static final BiFunction<BValueRef, BValueRef, BValueRef> GREATER_EQUAL_INT_FUNC =
            (lVal, rVal) -> {
                BValue resultVal = new BooleanValue(lVal.getInt() >= rVal.getInt());
                return new BValueRef(resultVal);
            };

    public static final BiFunction<BValueRef, BValueRef, BValueRef> GREATER_EQUAL_LONG_FUNC =
            (lVal, rVal) -> {
                BValue resultVal = new BooleanValue(lVal.getLong() >= rVal.getLong());
                return new BValueRef(resultVal);
            };

    public static final BiFunction<BValueRef, BValueRef, BValueRef> GREATER_EQUAL_FLOAT_FUNC =
            (lVal, rVal) -> {
                BValue resultVal = new BooleanValue(lVal.getFloat() >= rVal.getFloat());
                return new BValueRef(resultVal);
            };

    public static final BiFunction<BValueRef, BValueRef, BValueRef> GREATER_EQUAL_DOUBLE_FUNC =
            (lVal, rVal) -> {
                BValue resultVal = new BooleanValue(lVal.getDouble() >= rVal.getDouble());
                return new BValueRef(resultVal);
            };

    public GreaterEqualExpression(Expression lExpr, Expression rExpr) {
        super(lExpr, GREATER_EQUAL, rExpr);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
