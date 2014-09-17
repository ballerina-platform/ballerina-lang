/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.stream.runtime.StreamRuntime;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryParser {

    /**
     * Parse a query and return corresponding QueryRuntime
     *
     * @param query         query to be parsed
     * @param siddhiContext associated siddhi context
     * @param definitionMap map containing user given stream definitions
     * @return
     */
    public static QueryRuntime parse(Query query, SiddhiContext siddhiContext, Map<String, AbstractDefinition> definitionMap) {
        MetaStateEvent metaStateEvent = new MetaStateEvent(query.getInputStream().getStreamIds().size()); //MetaStateEvent for the query
        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        StreamRuntime streamRuntime;
        QuerySelector selector;
        OutputRateLimiter outputRateLimiter;
        QueryRuntime queryRuntime;
        Element element = null;
        try {
            streamRuntime = InputStreamParser.parse(query.getInputStream(), siddhiContext, definitionMap, metaStateEvent, executors);
            selector = SelectorParser.parse(query.getSelector(), query.getOutputStream(), siddhiContext, metaStateEvent, executors);
            outputRateLimiter = OutputParser.constructOutputRateLimiter(query.getOutputStream().getId(), query.getOutputRate());

            QueryParserHelper.updateVariablePosition(metaStateEvent, executors);
            QueryParserHelper.addEventConverters(streamRuntime, metaStateEvent);

            queryRuntime = new QueryRuntime(query, siddhiContext, streamRuntime, selector, outputRateLimiter, metaStateEvent);
            validateOutputStream(queryRuntime.getOutputStreamDefinition(), definitionMap);

            element = AnnotationHelper.getAnnotationElement("info", "name", query.getAnnotations());
        } catch (Exception e) {
            if (element != null) {
                throw new QueryCreationException(e.getMessage() + " when creating query " + element.getValue(), e);
            } else {
                throw new QueryCreationException(e.getMessage(), e);
            }
        }

        return queryRuntime;

    }

    private static void validateOutputStream(StreamDefinition outputStreamDefinition, Map<String, AbstractDefinition> definitionMap) {
        if (definitionMap.containsKey(outputStreamDefinition.getId()) && !(definitionMap.get(outputStreamDefinition.getId()).equalsIgnoreAnnotations(outputStreamDefinition))) {
            throw new DifferentDefinitionAlreadyExistException("Different stream definition same as output stream definition is already " +
                    "exist under stream name " + outputStreamDefinition.getId());
        }
    }


}
