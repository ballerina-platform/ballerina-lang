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
package io.ballerinalang.compiler.internal.parser;

/**
 * Operator precedence levels.
 * 
 * @since 1.2.0
 */
public enum OperatorPrecedence {

    MEMBER_ACCESS(0),   //  x.k, x.@a, f(x), x.f(y), x[y]
    UNARY(1),           //  (+x), (-x), (~x), (!x), (<T>x), (typeof x), (check x), (checkpanic x)
    MULTIPLICATIVE(2),  //  (x * y), (x / y), (x % y)
    ADDITIVE(3),        //  (x + y), (x - y)
    SHIFT(4),           //  (x << y), (x >> y), (x >>> y)
    RANGE(5),           //  (x ... y), (x ..< y)
    BINARY_COMPARE(6),  //  (x < y), (x > y), (x <= y), (x >= y), (x is y)
    EQUALITY(7),        //  (x == y), (x != y), (x == y), (x === y), (x !== y)
    BITWISE_AND(8),     //  (x & y)
    BITWISE_XOR(9),     //  (x ^ y)
    BITWISE_OR(10),     //  (x | y)
    LOGICAL_AND(11),    //  (x && y)
    LOGICAL_OR(12),     //  (x || y)

    ACTION(17),          //  Actions cannot reside inside expressions, hence they have the lowest precedence.
                        //  (x -> y()), (start x), ...
    ;

    private int level = 0;

    private OperatorPrecedence(int level) {
        this.level = level;
    }

    public boolean isHigherThan(OperatorPrecedence opPrecedence) {
        return this.level < opPrecedence.level;
    }
}
