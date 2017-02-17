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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValueType;

import java.util.function.BiFunction;

import static org.ballerinalang.model.Operator.LESS_EQUAL;

/**
 * {@code LessEqualExpression} represents a less than or equal (<=) expression in Ballerina.
 *
 * @since 0.8.0
 */
public class LessEqualExpression extends BinaryCompareExpression {

    public static final BiFunction<BValueType, BValueType, BValueType> LESS_EQUAL_INT_FUNC =
            (lVal, rVal) -> new BBoolean(lVal.intValue() <= rVal.intValue());

    public static final BiFunction<BValueType, BValueType, BValueType> LESS_EQUAL_LONG_FUNC =
            (lVal, rVal) -> new BBoolean(lVal.longValue() <= rVal.longValue());

    public static final BiFunction<BValueType, BValueType, BValueType> LESS_EQUAL_FLOAT_FUNC =
            (lVal, rVal) -> new BBoolean(lVal.floatValue() <= rVal.floatValue());

    public static final BiFunction<BValueType, BValueType, BValueType> LESS_EQUAL_DOUBLE_FUNC =
            (lVal, rVal) -> new BBoolean(lVal.doubleValue() <= rVal.doubleValue());

    public LessEqualExpression(NodeLocation location, Expression lExpr, Expression rExpr) {
        super(location, lExpr, LESS_EQUAL, rExpr);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
