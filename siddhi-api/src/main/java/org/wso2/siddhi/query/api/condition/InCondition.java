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
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.SourceNotExistException;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class InCondition extends Condition {

    private Condition condition;
    private String sourceId;

    public InCondition(Condition condition, String sourceId) {
        this.condition = condition;
        this.sourceId = sourceId;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getSourceId() {
        return sourceId;
    }


    @Override
    protected void validate(List<QueryEventSource> queryEventSourceList, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, String streamReferenceId,
                            boolean processInStreamDefinition) {

        AbstractDefinition definition = streamTableDefinitionMap.get(sourceId);
        if (definition == null) {
            throw new SourceNotExistException("Event Table with name '" + sourceId + "' not defined in Siddhi");

        } else {
            if (!(definition instanceof TableDefinition)) {
                throw new SourceNotExistException(sourceId + " is not an Event Table");

            }
        }

        queryEventSourceList.add(new QueryEventSource(sourceId, sourceId, definition, null, null, null));
        condition.validate(queryEventSourceList, streamTableDefinitionMap, streamReferenceId, processInStreamDefinition);
        queryEventSourceList.remove(queryEventSourceList.size() - 1);
    }

    @Override
    public String toString() {
        return "InCondition{" +
               "condition='" + condition + '\'' +
               ", sourceId=" + sourceId +
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

        InCondition that = (InCondition) o;

        if (condition != null ? !condition.equals(that.condition) : that.condition != null) {
            return false;
        }
        if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = condition != null ? condition.hashCode() : 0;
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
        return result;
    }

    public Set<String> getDependencySet() {
        return condition.getDependencySet();
    }
}
