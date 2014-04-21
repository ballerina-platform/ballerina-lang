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
package org.wso2.siddhi.core.query.creator;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.statemachine.pattern.PatternState;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.QueryPartComposite;
import org.wso2.siddhi.core.util.parser.StateParser;
import org.wso2.siddhi.core.util.parser.StreamParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.pattern.PatternStream;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PatternQueryCreator extends QueryCreator {

    private final List<PatternState> patternStateList;

    public PatternQueryCreator(String queryId, Query query, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, ConcurrentMap<String, EventTable> eventTableMap,OutputRateManager outputRateManager, SiddhiContext siddhiContext) {
        super(queryId, query, streamTableDefinitionMap, eventTableMap,outputRateManager, siddhiContext);
        patternStateList = StateParser.convertToPatternStateList(StateParser.identifyStates(((PatternStream) query.getInputStream()).getPatternElement()));
        init();
    }

    @Override
    protected void updateQueryEventSourceList(List<QueryEventSource> queryEventSourceList) {
        //update outputStreamDefinition as no transformer for pattern
        for (PatternState patternState : patternStateList) {
            patternState.getTransformedStream().getQueryEventSource().setOutDefinition(patternState.getTransformedStream().getQueryEventSource().getInDefinition());
        }
    }

    @Override
    public QueryPartComposite constructQuery() {
        QueryPartComposite queryPartComposite = StreamParser.parsePatternStream(query.getInputStream(), patternStateList, queryEventSourceList, streamTableDefinitionMap, eventTableMap, siddhiContext);
        QuerySelector querySelector = constructQuerySelector(outputRateManager);
        queryPartComposite.setQuerySelector(querySelector);
        StateParser.populateMemoryOptimizationParameters(patternStateList, query.getSelector(), queryPartComposite.getHandlerProcessorList());
        return queryPartComposite;
    }
}
