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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.query.api.execution.query.output.stream;

import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Updating UpdateSet Attribute for UpdateStream.
 */
public class UpdateSet implements SiddhiElement {

    private static final long serialVersionUID = 1L;
    private List<SetAttribute> setAttributeList = new ArrayList<>();
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public UpdateSet set(Variable tableVariable, Expression assignmentExpression) {
        setAttributeList.add(new SetAttribute(tableVariable, assignmentExpression));
        return this;
    }

    public List<SetAttribute> getSetAttributeList() {
        return setAttributeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UpdateSet updateSet = (UpdateSet) o;

        return setAttributeList != null ? setAttributeList.equals(updateSet.setAttributeList) :
                updateSet.setAttributeList == null;
    }

    @Override
    public int hashCode() {
        return setAttributeList != null ? setAttributeList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UpdateSet{" +
                "setAttributeList=" + setAttributeList +
                '}';
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

    /**
     * Attribute assignment for updates.
     */
    public static class SetAttribute implements SiddhiElement {

        private static final long serialVersionUID = 1L;
        private Variable tableVariable;
        private Expression assignmentExpression;
        private int[] queryContextStartIndex;
        private int[] queryContextEndIndex;

        public SetAttribute(Variable tableVariable, Expression assignmentExpression) {
            this.tableVariable = tableVariable;
            this.assignmentExpression = assignmentExpression;
        }

        public Variable getTableVariable() {
            return tableVariable;
        }

        public Expression getAssignmentExpression() {
            return assignmentExpression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SetAttribute that = (SetAttribute) o;

            if (tableVariable != null ? !tableVariable.equals(that.tableVariable) : that.tableVariable != null) {
                return false;
            }
            return assignmentExpression != null ? assignmentExpression.equals(that.assignmentExpression) :
                    that.assignmentExpression == null;
        }

        @Override
        public int hashCode() {
            int result = tableVariable != null ? tableVariable.hashCode() : 0;
            result = 31 * result + (assignmentExpression != null ? assignmentExpression.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SetAttribute{" +
                    "tableVariable=" + tableVariable +
                    ", assignmentExpression=" + assignmentExpression +
                    '}';
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
}
