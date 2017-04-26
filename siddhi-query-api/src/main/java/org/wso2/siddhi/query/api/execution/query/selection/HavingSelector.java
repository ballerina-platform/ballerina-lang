/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query.selection;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.List;

public class HavingSelector extends Selector {

    private Expression havingExpression;

    public HavingSelector select(String rename, Expression expression) {
        return (HavingSelector) super.select(rename, expression);
    }

    public HavingSelector select(Variable variable) {
        return (HavingSelector) super.select(variable);

    }

    public HavingSelector having(Expression havingExpression) {
        this.havingExpression = havingExpression;
        return this;
    }

    public HavingSelector groupBy(Variable variable) {
        return (HavingSelector) super.groupBy(variable);

    }

    public HavingSelector addGroupByList(List<Variable> list) {
        return (HavingSelector) super.addGroupByList(list);

    }

    public Expression getHavingExpression() {
        return havingExpression;
    }

    public HavingSelector addSelectionList(List<OutputAttribute> projectionList) {
        return (HavingSelector) super.addSelectionList(projectionList);

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
        if (this == o) return true;
        if (!(o instanceof HavingSelector)) return false;

        HavingSelector selector = (HavingSelector) o;

        if (groupByList != null ? !groupByList.equals(selector.groupByList) : selector.groupByList != null)
            return false;
        if (havingExpression != null ? !havingExpression.equals(selector.havingExpression) : selector.havingExpression != null)
            return false;
        if (selectionList != null ? !selectionList.equals(selector.selectionList) : selector.selectionList != null)
            return false;

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
