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
package org.ballerinalang.model;

/**
 * {code Operator} represents operators in Ballerina.
 *
 * @since 0.8.0
 */
public enum Operator {

    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    AND("&&"),
    OR("||"),
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    GREATER_EQUAL(">="),
    LESS_THAN("<"),
    LESS_EQUAL("<="),
    NEW("new"),
    NOT("!");

    private final String opValue;

    Operator(String opValue) {
        this.opValue = opValue;
    }

    @Override
    public String toString() {
        return opValue;
    }
}
