/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.expression;


import org.wso2.siddhi.query.api.expression.condition.And;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.condition.In;
import org.wso2.siddhi.query.api.expression.condition.IsNull;
import org.wso2.siddhi.query.api.expression.condition.Not;
import org.wso2.siddhi.query.api.expression.condition.Or;
import org.wso2.siddhi.query.api.expression.constant.BoolConstant;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.FloatConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;
import org.wso2.siddhi.query.api.expression.math.Add;
import org.wso2.siddhi.query.api.expression.math.Divide;
import org.wso2.siddhi.query.api.expression.math.Mod;
import org.wso2.siddhi.query.api.expression.math.Multiply;
import org.wso2.siddhi.query.api.expression.math.Subtract;

import java.io.Serializable;

/**
 * Siddhi expression
 */
public abstract class Expression implements Serializable {

    private static final long serialVersionUID = 1L;

    public static StringConstant value(String value) {
        return new StringConstant(value);
    }

    public static IntConstant value(int value) {
        return new IntConstant(value);
    }

    public static LongConstant value(long value) {
        return new LongConstant(value);
    }

    public static DoubleConstant value(double value) {
        return new DoubleConstant(value);
    }

    public static FloatConstant value(float value) {
        return new FloatConstant(value);
    }

    public static BoolConstant value(boolean value) {
        return new BoolConstant(value);
    }

    public static Variable variable(String attributeName) {
        return new Variable(attributeName);
    }

    public static Add add(Expression leftValue, Expression rightValue) {
        return new Add(leftValue, rightValue);
    }

    public static Subtract subtract(Expression leftValue, Expression rightValue) {
        return new Subtract(leftValue, rightValue);
    }

    public static Multiply multiply(Expression leftValue, Expression rightValue) {
        return new Multiply(leftValue, rightValue);
    }

    public static Divide divide(Expression leftValue, Expression rightValue) {
        return new Divide(leftValue, rightValue);
    }

    public static Mod mod(Expression leftValue, Expression rightValue) {
        return new Mod(leftValue, rightValue);
    }

    public static Expression function(String extensionNamespace, String extensionFunctionName,
                                      Expression... expressions) {
        return new AttributeFunction(extensionNamespace, extensionFunctionName, expressions);
    }

    public static Expression function(String functionName, Expression... expressions) {
        return new AttributeFunction("", functionName, expressions);
    }

    public static Expression compare(Expression leftExpression, Compare.Operator operator,
                                     Expression rightExpression) {
        return new Compare(leftExpression, operator, rightExpression);
    }

    public static Expression in(Expression leftExpression, String streamId) {
        return new In(leftExpression, streamId);
    }

    public static Expression and(Expression leftExpression, Expression rightExpression) {
        return new And(leftExpression, rightExpression);
    }

    public static Expression or(Expression leftExpression, Expression rightExpression) {
        return new Or(leftExpression, rightExpression);
    }

    public static Expression not(Expression expression) {
        return new Not(expression);
    }

    public static Expression isNull(Expression expression) {
        return new IsNull(expression);
    }

    public static Expression isNullStream(String streamId) {
        return new IsNull(streamId, null, false);
    }

    public static Expression isNullStream(String streamId, int streamIndex) {
        return new IsNull(streamId, streamIndex, false);
    }

    public static Expression isNullInnerStream(String streamId) {
        return new IsNull(streamId, null, true);
    }

    public static Expression isNullInnerStream(String streamId, int streamIndex) {
        return new IsNull(streamId, streamIndex, true);
    }

    /**
     * Time constant factory class
     */
    public abstract static class Time {

        public static TimeConstant milliSec(long i) {
            return new TimeConstant((long) i);
        }

        public static TimeConstant milliSec(int i) {
            return milliSec((long) i);
        }

        public static TimeConstant sec(long i) {
            return new TimeConstant(((long) i) * 1000);
        }

        public static TimeConstant sec(int i) {
            return sec((long) i);
        }

        public static TimeConstant minute(long i) {
            return new TimeConstant(((long) i) * 60 * 1000);
        }

        public static TimeConstant minute(int i) {
            return minute((long) i);
        }

        public static TimeConstant hour(long i) {
            return new TimeConstant(((long) i) * 60 * 60 * 1000);
        }

        public static TimeConstant hour(int i) {
            return hour((long) i);
        }

        public static TimeConstant day(long i) {
            return new TimeConstant(((long) i) * 24 * 60 * 60 * 1000);
        }

        public static TimeConstant day(int i) {
            return day((long) i);
        }

        public static TimeConstant week(long i) {
            return new TimeConstant(((long) i) * 7 * 24 * 60 * 60 * 1000);
        }

        public static TimeConstant week(int i) {
            return week((long) i);
        }

        public static TimeConstant month(long i) {
            return new TimeConstant(((long) i) * 30 * 24 * 60 * 60 * 1000);
        }

        public static TimeConstant month(int i) {
            return month((long) i);
        }

        public static TimeConstant year(long i) {
            return new TimeConstant(((long) i) * 365 * 24 * 60 * 60 * 1000);
        }

        public static TimeConstant year(int i) {
            return year((long) i);
        }

    }
}
