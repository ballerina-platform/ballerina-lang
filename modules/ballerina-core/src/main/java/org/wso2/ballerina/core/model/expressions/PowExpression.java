/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.function.BiFunction;

import static org.wso2.ballerina.core.model.Operator.POW;

/**
 * {@code PowExpression} represents a binary pow expression.
 *
 * @since 0.8.0
 */
public class PowExpression extends BinaryArithmeticExpression {

    public static final BiFunction<BValueType, BValueType, BValueType> POW_INT_FUNC =
            (lVal, rVal) -> new BInteger((int) Math.pow(lVal.intValue(), rVal.intValue()));

    public static final BiFunction<BValueType, BValueType, BValueType> POW_LONG_FUNC =
            (lVal, rVal) -> new BLong((long) Math.pow(lVal.longValue(), rVal.longValue()));

    public static final BiFunction<BValueType, BValueType, BValueType> POW_FLOAT_FUNC =
            (lVal, rVal) -> new BFloat((float) Math.pow(lVal.floatValue(), rVal.floatValue()));

    public static final BiFunction<BValueType, BValueType, BValueType> POW_DOUBLE_FUNC =
            (lVal, rVal) -> new BDouble(Math.pow(lVal.doubleValue(), rVal.doubleValue()));

    public PowExpression(NodeLocation location, Expression lExpr, Expression rExpr) {
        super(location, lExpr, POW, rExpr);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
