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
package org.wso2.siddhi.core.query.output.callback;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

public class UpdateTableCallback implements OutputCallback {
    private EventTable eventTable;
    private ConditionExecutor conditionExecutor;
    private StreamDefinition outputStreamDefinition;
    private int[] mappingPosition;

    public UpdateTableCallback(EventTable eventTable, ConditionExecutor conditionExecutor, StreamDefinition outputStreamDefinition) {
        this.eventTable = eventTable;
        this.conditionExecutor = conditionExecutor;
        this.outputStreamDefinition = outputStreamDefinition;
        validateUpdateTable(eventTable.getTableDefinition(), outputStreamDefinition.getAttributeList());

    }

    private void validateUpdateTable(AbstractDefinition tableDefinition, List<Attribute> outStreamAttributeList) {
        mappingPosition = new int[outStreamAttributeList.size()];
        for (int i = 0; i < outStreamAttributeList.size(); i++) {
            Attribute streamAttribute = outStreamAttributeList.get(i);
            mappingPosition[i] = tableDefinition.getAttributePosition(streamAttribute.getName());
        }
    }

    @Override
    public void send(StreamEvent streamEvent) {
        eventTable.update(streamEvent, conditionExecutor, mappingPosition);
    }

}
