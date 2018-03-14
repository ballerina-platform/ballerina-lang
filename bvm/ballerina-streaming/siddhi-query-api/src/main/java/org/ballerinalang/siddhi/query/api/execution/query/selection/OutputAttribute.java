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
package org.ballerinalang.siddhi.query.api.execution.query.selection;

import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;

/**
 * Query output stream attributes.
 */
public class OutputAttribute implements SiddhiElement {

    private static final long serialVersionUID = 1L;
    private String rename;
    private Expression expression;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public OutputAttribute(String rename, Expression expression) {
        this.rename = rename;
        this.expression = expression;
    }

    public OutputAttribute(Variable variable) {
        this.rename = variable.getAttributeName();
        this.expression = variable;
    }

    public String getRename() {
        return rename;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "OutputAttribute{" +
                "rename='" + rename + '\'' +
                ", expression=" + expression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OutputAttribute)) {
            return false;
        }

        OutputAttribute that = (OutputAttribute) o;

        if (expression != null ? !expression.equals(that.expression) : that.expression != null) {
            return false;
        }
        if (rename != null ? !rename.equals(that.rename) : that.rename != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = rename != null ? rename.hashCode() : 0;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }
}
