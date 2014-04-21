/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.query.api.condition;

import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.ExpressionValidator;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class BooleanCondition extends Condition {

    private Expression expression;

    public BooleanCondition(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    protected void validate(List<QueryEventSource> queryEventSourceList, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, String streamReferenceId,
                            boolean processInStreamDefinition) {
        ExpressionValidator.validate(expression, queryEventSourceList, streamReferenceId, processInStreamDefinition);
    }

    @Override
    public String toString() {
        return "BooleanCondition{" +
               "expression=" + expression +
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

        BooleanCondition that = (BooleanCondition) o;

        if (expression != null ? !expression.equals(that.expression) : that.expression != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }

    public Set<String> getDependencySet() {
        return ExpressionValidator.getDependencySet(expression);
    }
}
