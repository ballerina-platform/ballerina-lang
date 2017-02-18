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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Operator;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValueType;

import java.util.function.BiFunction;

/**
 * {@code AddExpression} represents a binary add expression.
 *
 * @since 0.8.0
 */
public class AddExpression extends BinaryArithmeticExpression {

    public static final BiFunction<BValueType, BValueType, BValueType> ADD_INT_FUNC =
            (lVal, rVal) -> new BInteger(lVal.intValue() + rVal.intValue());

    public static final BiFunction<BValueType, BValueType, BValueType> ADD_LONG_FUNC =
            (lVal, rVal) -> new BLong(lVal.longValue() + rVal.longValue());

    public static final BiFunction<BValueType, BValueType, BValueType> ADD_FLOAT_FUNC =
            (lVal, rVal) -> new BFloat(lVal.floatValue() + rVal.floatValue());

    public static final BiFunction<BValueType, BValueType, BValueType> ADD_DOUBLE_FUNC =
            (lVal, rVal) -> new BDouble(lVal.doubleValue() + rVal.doubleValue());

    public static final BiFunction<BValueType, BValueType, BValueType> ADD_STRING_FUNC =
            (lVal, rVal) -> new BString(lVal.stringValue() + rVal.stringValue());

    public AddExpression(NodeLocation location, Expression lExpr, Expression rExpr) {
        super(location, lExpr, Operator.ADD, rExpr);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
