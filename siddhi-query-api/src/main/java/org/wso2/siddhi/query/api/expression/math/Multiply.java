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
package org.wso2.siddhi.query.api.expression.math;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Multiply {@link Expression}
 */
public class Multiply extends Expression {

    private static final long serialVersionUID = 1L;
    private Expression leftValue;
    private Expression rightValue;

    public Multiply(Expression leftValue, Expression rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public Expression getLeftValue() {
        return leftValue;
    }

    public Expression getRightValue() {
        return rightValue;
    }

    @Override
    public String toString() {
        return "Multiply{" +
                "leftValue=" + leftValue +
                ", rightValue=" + rightValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Multiply multiply = (Multiply) o;

        if (leftValue != null ? !leftValue.equals(multiply.leftValue) : multiply.leftValue != null) {
            return false;
        }
        if (rightValue != null ? !rightValue.equals(multiply.rightValue) : multiply.rightValue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = leftValue != null ? leftValue.hashCode() : 0;
        result = 31 * result + (rightValue != null ? rightValue.hashCode() : 0);
        return result;
    }

}
