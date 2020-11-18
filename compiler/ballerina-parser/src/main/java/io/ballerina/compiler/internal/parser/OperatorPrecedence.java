/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.internal.parser;

/**
 * Operator precedence levels.
 * 
 * @since 1.2.0
 */
public enum OperatorPrecedence {

    MEMBER_ACCESS(0),       //  x.k, x.@a, f(x), x.f(y), x[y], x?.k, x.<y>, x/<y>, x/**/<y>, x/*xml-step-extend
    UNARY(1),               //  (+x), (-x), (~x), (!x), (<T>x), (typeof x),
    EXPRESSION_ACTION(1),   //  Expression that can also be an action. eg: (check x), (checkpanic x). Same as unary.
    MULTIPLICATIVE(2),      //  (x * y), (x / y), (x % y)
    ADDITIVE(3),            //  (x + y), (x - y)
    SHIFT(4),               //  (x << y), (x >> y), (x >>> y)
    RANGE(5),               //  (x ... y), (x ..< y)
    BINARY_COMPARE(6),      //  (x < y), (x > y), (x <= y), (x >= y), (x is y)
    EQUALITY(7),            //  (x == y), (x != y), (x == y), (x === y), (x !== y)
    BITWISE_AND(8),         //  (x & y)
    BITWISE_XOR(9),         //  (x ^ y)
    BITWISE_OR(10),         //  (x | y)
    LOGICAL_AND(11),        //  (x && y)
    LOGICAL_OR(12),         //  (x || y)
    ELVIS_CONDITIONAL(13),  //  x ?: y
    CONDITIONAL(14),        //  x ? y : z

    ANON_FUNC_OR_LET(16),   //  (x) => y
    QUERY(17),              //  from x, select x, where x

    //  Actions cannot reside inside expressions, hence they have the lowest precedence.
    REMOTE_CALL_ACTION(18), //  (x -> y()), 
    ACTION(19),             //  (start x), ...
    DEFAULT(20),             //  (start x), ...
    ;

    private int level = 0;

    private OperatorPrecedence(int level) {
        this.level = level;
    }

    public boolean isHigherThanOrEqual(OperatorPrecedence opPrecedence, boolean allowActions) {
        if (allowActions) {
            if (this == EXPRESSION_ACTION && opPrecedence == REMOTE_CALL_ACTION) {
                return false;
            }
        }
        return this.level <= opPrecedence.level;
    }
}
