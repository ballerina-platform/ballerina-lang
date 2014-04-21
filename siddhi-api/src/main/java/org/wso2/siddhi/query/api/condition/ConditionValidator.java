/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ConditionValidator {

    public static void validate(Condition condition, List<QueryEventSource> queryEventSources,
                                ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, String streamReferenceId, boolean processInStreamDefinition) {
        condition.validate(queryEventSources,streamTableDefinitionMap, streamReferenceId, processInStreamDefinition);
    }

    public static Set<String> getDependencySet(Condition condition) {
        return condition.getDependencySet();
    }

}
