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
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.predicate.PredicateBuilder;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;
import org.wso2.siddhi.query.api.definition.TableDefinition;

public class InConditionExecutor implements ConditionExecutor {

    public ConditionExecutor conditionExecutor;
    private EventTable eventTable;

    public InConditionExecutor(EventTable eventTable,
                               ConditionExecutor conditionExecutor) {
        this.eventTable = eventTable;
        this.conditionExecutor = conditionExecutor;
    }

    public boolean execute(AtomicEvent event) {
        return eventTable.contains(event, conditionExecutor);
    }

    @Override
    public String constructFilterQuery(AtomicEvent newEvent, int level) {
        return "*";
    }

    @Override
    public PredicateTreeNode constructPredicate(AtomicEvent newEvent, TableDefinition tableDefinition, PredicateBuilder predicateBuilder) {
        return predicateBuilder.buildVariableExpression("*");
    }

}
