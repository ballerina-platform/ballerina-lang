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
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

public class InsertIntoTableCallback implements OutputCallback {
    private EventTable eventTable;

    public InsertIntoTableCallback(EventTable eventTable, StreamDefinition outputStreamDefinition) {
        this.eventTable = eventTable;
        validateInsertIntoTable(eventTable.getTableDefinition().getAttributeList(), outputStreamDefinition.getAttributeList());
    }

    private void validateInsertIntoTable(List<Attribute> tableAttributeList, List<Attribute> outStreamAttributeList) {
        if (tableAttributeList.size() != outStreamAttributeList.size()) {
            throw new QueryCreationException("Event table " + eventTable.getTableDefinition().getTableId() + " expects " + tableAttributeList.size() + " attributes but found " + outStreamAttributeList.size() + " attributes");
        } else {
            for (int i = 0; i < tableAttributeList.size(); i++) {
                Attribute tableAttribute = tableAttributeList.get(i);
                Attribute streamAttribute = outStreamAttributeList.get(i);
                if (tableAttribute.getType() != streamAttribute.getType()) {
                    throw new QueryCreationException("Event table " + eventTable.getTableDefinition().getTableId() + " expects " + tableAttribute.getType() + " as attribute:" + (i + 1) + " but found type " + streamAttribute.getType());
                }
            }
        }
    }

    @Override
    public void send(StreamEvent streamEvent) {
        eventTable.add(streamEvent);
    }
}
