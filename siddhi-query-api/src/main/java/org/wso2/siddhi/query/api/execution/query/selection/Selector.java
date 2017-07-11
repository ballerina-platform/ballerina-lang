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
package org.wso2.siddhi.query.api.execution.query.selection;

import org.wso2.siddhi.query.api.exception.DuplicateAttributeException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Selector selecting query output stream attributes
 */
public class Selector implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<OutputAttribute> selectionList = new ArrayList<OutputAttribute>();
    private List<Variable> groupByList = new ArrayList<Variable>();
    private Expression havingExpression;

    public static Selector selector() {
        return new Selector();
    }

    public static BasicSelector basicSelector() {
        return new BasicSelector();
    }

    public Selector select(String rename, Expression expression) {
        OutputAttribute outputAttribute = new OutputAttribute(rename, expression);
        checkSelection(outputAttribute);
        selectionList.add(outputAttribute);
        return this;
    }

    public Selector select(Variable variable) {
        OutputAttribute outputAttribute = new OutputAttribute(variable);
        checkSelection(outputAttribute);
        selectionList.add(outputAttribute);
        return this;
    }

    private void checkSelection(OutputAttribute newAttribute) {
        for (OutputAttribute attribute : selectionList) {
            if (attribute.getRename().equals(newAttribute.getRename())) {
                throw new DuplicateAttributeException(attribute.getRename() + " is already defined as an output " +
                        "attribute ");
            }
        }
    }

    public Selector having(Expression havingExpression) {
        this.havingExpression = havingExpression;
        return this;
    }

    public Selector groupBy(Variable variable) {
        groupByList.add(variable);
        return this;
    }

    public Selector addGroupByList(List<Variable> list) {
        if (list != null) {
            groupByList.addAll(list);
        }
        return this;
    }

    public List<OutputAttribute> getSelectionList() {
        return selectionList;
    }

    public List<Variable> getGroupByList() {
        return groupByList;
    }

    public Expression getHavingExpression() {
        return havingExpression;
    }

    public Selector addSelectionList(List<OutputAttribute> projectionList) {
        for (OutputAttribute outputAttribute : projectionList) {
            checkSelection(outputAttribute);
            this.selectionList.add(outputAttribute);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Selector{" +
                "selectionList=" + selectionList +
                ", groupByList=" + groupByList +
                ", havingExpression=" + havingExpression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Selector)) {
            return false;
        }

        Selector selector = (Selector) o;

        if (groupByList != null ? !groupByList.equals(selector.groupByList) : selector.groupByList != null) {
            return false;
        }
        if (havingExpression != null ? !havingExpression.equals(selector.havingExpression) : selector
                .havingExpression != null) {
            return false;
        }
        if (selectionList != null ? !selectionList.equals(selector.selectionList) : selector.selectionList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionList != null ? selectionList.hashCode() : 0;
        result = 31 * result + (groupByList != null ? groupByList.hashCode() : 0);
        result = 31 * result + (havingExpression != null ? havingExpression.hashCode() : 0);
        return result;
    }
}
