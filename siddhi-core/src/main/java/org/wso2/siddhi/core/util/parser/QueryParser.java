/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulatorFactory;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryParser {

    /**
     * Parse a query and return corresponding QueryRuntime
     *
     * @param query                query to be parsed
     * @param executionPlanContext associated Execution Plan context
     * @param streamDefinitionMap  map containing user given stream definitions
     * @param tableDefinitionMap
     * @return queryRuntime
     */
    public static QueryRuntime parse(Query query, ExecutionPlanContext executionPlanContext,
                                     Map<String, AbstractDefinition> streamDefinitionMap,
                                     Map<String, AbstractDefinition> tableDefinitionMap,
                                     Map<String, EventTable> eventTableMap) {
        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        QueryRuntime queryRuntime;
        Element element = null;
        try {
            element = AnnotationHelper.getAnnotationElement("info", "name", query.getAnnotations());
            StreamRuntime streamRuntime = InputStreamParser.parse(query.getInputStream(),
                    executionPlanContext, streamDefinitionMap, tableDefinitionMap, eventTableMap, executors);

            QuerySelector selector = SelectorParser.parse(query.getSelector(), query.getOutputStream(),
                    executionPlanContext, streamRuntime.getMetaComplexEvent(), eventTableMap, executors);
            OutputRateLimiter outputRateLimiter = OutputParser.constructOutputRateLimiter(query.getOutputStream().getId(), query.getOutputRate());

            OutputCallback outputCallback = OutputParser.constructOutputCallback(query.getOutputStream(),
                    streamRuntime.getMetaComplexEvent().getOutputStreamDefinition(), eventTableMap, executionPlanContext);

            QueryParserHelper.reduceMetaComplexEvent(streamRuntime.getMetaComplexEvent());
            QueryParserHelper.updateVariablePosition(streamRuntime.getMetaComplexEvent(), executors);
            QueryParserHelper.initStreamRuntime(streamRuntime, streamRuntime.getMetaComplexEvent());

            selector.setEventPopulator(StateEventPopulatorFactory.constructEventPopulator(streamRuntime.getMetaComplexEvent()));

            queryRuntime = new QueryRuntime(query, executionPlanContext, streamRuntime, selector, outputRateLimiter, outputCallback, streamRuntime.getMetaComplexEvent());

        } catch (DuplicateDefinitionException e) {
            if (element != null) {
                throw new DuplicateDefinitionException(e.getMessage() + " when creating query " + element.getValue(), e);
            } else {
                throw new DuplicateDefinitionException(e.getMessage(), e);
            }
        } catch (RuntimeException e) {
            if (element != null) {
                throw new ExecutionPlanCreationException(e.getMessage() + " when creating query " + element.getValue(), e);
            } else {
                throw new ExecutionPlanCreationException(e.getMessage(), e);
            }
        }

        return queryRuntime;

    }


}
