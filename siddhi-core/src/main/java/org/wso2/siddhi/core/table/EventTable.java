/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.table;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.Iterator;

public interface EventTable {


    public TableDefinition getTableDefinition();

    public void add(StreamEvent streamEvent);

    public void delete(StreamEvent streamEvent, ConditionExecutor conditionExecutor);

    public void update(StreamEvent streamEvent, ConditionExecutor conditionExecutor, int[] attributeUpdateMappingPosition);

    public boolean contains(AtomicEvent atomicEvent, ConditionExecutor conditionExecutor);

    public QueryEventSource getQueryEventSource();

    public Iterator<StreamEvent> iterator();

    public Iterator<StreamEvent> iterator(String SQLPredicate);
}