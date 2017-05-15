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
package org.wso2.siddhi.query.api.expression.condition;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Or condition {@link Expression}
 */
public class Or extends Expression {

    private static final long serialVersionUID = 1L;

    private Expression leftExpression;
    private Expression rightExpression;

    public Or(Expression leftExpression, Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    @Override
    public String toString() {
        return "Or{" +
                "leftExpression=" + leftExpression +
                ", rightExpression=" + rightExpression +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Or that = (Or) o;

        if (leftExpression != null ? !leftExpression.equals(that.leftExpression) : that.leftExpression != null) {
            return false;
        }
        if (rightExpression != null ? !rightExpression.equals(that.rightExpression) : that.rightExpression != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = leftExpression != null ? leftExpression.hashCode() : 0;
        result = 31 * result + (rightExpression != null ? rightExpression.hashCode() : 0);
        return result;
    }

}
