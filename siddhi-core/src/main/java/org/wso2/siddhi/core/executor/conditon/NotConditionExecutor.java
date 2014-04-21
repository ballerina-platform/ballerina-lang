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
package org.wso2.siddhi.core.executor.conditon;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.table.predicate.PredicateBuilder;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;
import org.wso2.siddhi.query.api.definition.TableDefinition;

public class NotConditionExecutor implements ConditionExecutor {

    public ConditionExecutor conditionExecutor;

    public NotConditionExecutor(ConditionExecutor conditionExecutor) {
        this.conditionExecutor = conditionExecutor;
    }

    public boolean execute(AtomicEvent event) {
        return !conditionExecutor.execute(event);
    }

    @Override
    public String constructFilterQuery(AtomicEvent newEvent, int level) {
        String value = conditionExecutor.constructFilterQuery(newEvent, 1);
        if (value.equals("*")) {
            return "*";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(" !(").append(value).append(")");
            return sb.toString();
        }
    }


    @Override
    public PredicateTreeNode constructPredicate(AtomicEvent newEvent, TableDefinition tableDefinition, PredicateBuilder predicateBuilder) {
        PredicateTreeNode value = conditionExecutor.constructPredicate(newEvent, tableDefinition, predicateBuilder);
        if (value.toString().equals("*")) {
            return predicateBuilder.buildVariableExpression("*");
        } else {
            return predicateBuilder.buildNotCondition(value);
        }
    }

}
