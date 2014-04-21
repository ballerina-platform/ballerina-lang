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
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public abstract class Condition {

    public static Condition compare(Expression leftExpression, Operator operator,
                                    Expression rightExpression) {
        return new Compare(leftExpression, operator, rightExpression);
    }

    public static Condition in(Condition leftCondition, String streamId) {
        return new InCondition(leftCondition, streamId);
    }

    public static Condition and(Condition leftCondition, Condition rightCondition) {
        return new AndCondition(leftCondition, rightCondition);
    }

    public static Condition or(Condition leftCondition, Condition rightCondition) {
        return new OrCondition(leftCondition, rightCondition);
    }

    public static Condition not(Condition condition) {
        return new NotCondition(condition);
    }

    public static Condition bool(Expression expression) {
        return new BooleanCondition(expression);
    }

    public static Condition extension(String extensionNamespace, String extensionFunctionName, Expression... expressions) {
        return new ConditionExtension(extensionNamespace, extensionFunctionName, expressions);
    }

    public static Condition function(String extensionFunctionName, Expression... expressions) {
        return new FunctionCondition(extensionFunctionName, expressions);
    }

    public enum Operator {
        LESS_THAN, GREATER_THAN, LESS_THAN_EQUAL, GREATER_THAN_EQUAL, EQUAL, NOT_EQUAL, CONTAINS, INSTANCE_OF
    }

    protected abstract void validate(List<QueryEventSource> queryEventSourceList,
                                     ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, String streamReferenceId, boolean processInStreamDefinition);

    protected Set<String> getDependencySet() {
        return new HashSet<String>();
    }
}
