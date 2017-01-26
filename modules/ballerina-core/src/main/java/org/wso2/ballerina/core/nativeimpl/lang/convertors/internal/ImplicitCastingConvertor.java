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
package org.wso2.ballerina.core.nativeimpl.lang.convertors.internal;

import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;

/**
 * Type converter for implicit casting operations
 */
public class ImplicitCastingConvertor {

    public static BValue convertWithValue(Expression lExpr, Expression rExpr, BValue rValue) {
        BValue resultValue = null;
        if (rExpr.getType() == BTypes.INT_TYPE) {
            if (lExpr.getType() == BTypes.LONG_TYPE && (rValue instanceof BInteger)) {
                resultValue = new BLong(((BInteger) rValue).longValue());
            } else if (lExpr.getType() == BTypes.FLOAT_TYPE && (rValue instanceof BInteger)) {
                resultValue = new BFloat(((BInteger) rValue).floatValue());
            } else if (lExpr.getType() == BTypes.DOUBLE_TYPE && (rValue instanceof BInteger)) {
                resultValue = new BDouble(((BInteger) rValue).doubleValue());
            }
        } else if (rExpr.getType() == BTypes.LONG_TYPE) {
            if (lExpr.getType() == BTypes.FLOAT_TYPE && (rValue instanceof BLong)) {
                resultValue = new BFloat(((BLong) rValue).floatValue());
            } else if (lExpr.getType() == BTypes.DOUBLE_TYPE && (rValue instanceof BLong)) {
                resultValue = new BDouble(((BLong) rValue).doubleValue());
            }
        } else if (rExpr.getType() == BTypes.FLOAT_TYPE) {
            if (lExpr.getType() == BTypes.DOUBLE_TYPE && (rValue instanceof BFloat)) {
                resultValue = new BDouble(((BFloat) rValue).doubleValue());
            }
        }
        return resultValue;
    }

    public static BValueType convertWithType(Expression lExpr, Expression rExpr, BValueType rValue, BValueType lValue) {
        BValueType resultValue = null;
        if (rExpr.getType() == BTypes.INT_TYPE) {
            if (lExpr.getType() == BTypes.LONG_TYPE) {
                resultValue = new BLong(rValue.longValue());
            } else if (lExpr.getType() == BTypes.FLOAT_TYPE) {
                resultValue = new BFloat(rValue.floatValue());
            } else if (lExpr.getType() == BTypes.DOUBLE_TYPE) {
                resultValue = new BDouble(rValue.doubleValue());
            }
        } else if (rExpr.getType() == BTypes.LONG_TYPE) {
            if (lExpr.getType() == BTypes.FLOAT_TYPE) {
                resultValue = new BFloat(rValue.floatValue());
            } else if (lExpr.getType() == BTypes.DOUBLE_TYPE) {
                resultValue = new BDouble(rValue.doubleValue());
            }
        } else if (rExpr.getType() == BTypes.FLOAT_TYPE) {
            if (lExpr.getType() == BTypes.DOUBLE_TYPE) {
                resultValue = new BDouble(rValue.doubleValue());
            }
        } else if (lExpr.getType() == BTypes.INT_TYPE) {
            if (rExpr.getType() == BTypes.LONG_TYPE) {
                resultValue = new BLong(lValue.longValue());
            } else if (rExpr.getType() == BTypes.FLOAT_TYPE) {
                resultValue = new BFloat(lValue.floatValue());
            } else if (rExpr.getType() == BTypes.DOUBLE_TYPE) {
                resultValue = new BDouble(lValue.doubleValue());
            }
        } else if (lExpr.getType() == BTypes.LONG_TYPE) {
            if (rExpr.getType() == BTypes.FLOAT_TYPE) {
                resultValue = new BFloat(lValue.floatValue());
            } else if (rExpr.getType() == BTypes.DOUBLE_TYPE) {
                resultValue = new BDouble(lValue.doubleValue());
            }
        } else if (lExpr.getType() == BTypes.FLOAT_TYPE) {
            if (rExpr.getType() == BTypes.DOUBLE_TYPE) {
                resultValue = new BDouble(lValue.doubleValue());
            }
        }
        return resultValue;
    }
}
