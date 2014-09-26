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
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.stream.runtime.SingleStreamRuntime;
import org.wso2.siddhi.core.stream.runtime.StreamRuntime;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;

import java.util.List;
import java.util.Map;

public class InputStreamParser {

    /**
     * Parse an InputStream returning corresponding StreamRuntime
     *
     * @param inputStream    input stream to be parsed
     * @param context        associated siddhi context
     * @param definitionMap  map containing user given stream definitions
     * @param metaStateEvent Meta event used to collect execution info of stream associated with query
     * @param executors      List to hold VariableExpressionExecutors to update after query parsing
     * @return
     */
    public static StreamRuntime parse(InputStream inputStream, SiddhiContext context, Map<String, AbstractDefinition> definitionMap,
                                      MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> executors) {
        if (inputStream instanceof BasicSingleInputStream || inputStream instanceof SingleInputStream) {
            MetaStreamEvent metaStreamEvent = generateMetaStreamEvent(inputStream, definitionMap);
            SingleStreamRuntime singleStreamRuntime = SingleInputStreamParser.parseInputStream((SingleInputStream) inputStream,
                    context, metaStreamEvent, executors);
            metaStateEvent.addEvent(metaStreamEvent);
            return singleStreamRuntime;
        } else {
            //TODO: join,pattern, etc
            throw new OperationNotSupportedException();
        }
    }

    /**
     * Method to generate MetaStreamEvent reagent to the given input stream. Empty definition will be created and
     * definition and reference is will be set accordingly in this method.
     *
     * @param inputStream
     * @param definitionMap
     * @return
     */
    private static MetaStreamEvent generateMetaStreamEvent(InputStream inputStream, Map<String, AbstractDefinition> definitionMap) {
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        if (definitionMap != null && definitionMap.containsKey(((SingleInputStream) inputStream).getStreamId())) {
            metaStreamEvent.setInputDefinition(definitionMap.get(((SingleInputStream) inputStream).getStreamId()));
        } else {
            throw new DefinitionNotExistException("Stream definition with stream ID" + ((SingleInputStream) inputStream).getStreamId() + " has not been defined");
        }
        if ((((SingleInputStream) inputStream).getStreamReferenceId() != null) && !(((SingleInputStream) inputStream).getStreamId()).equals(((SingleInputStream) inputStream).getStreamReferenceId())) { //if ref id is provided
            metaStreamEvent.setInputReferenceId(((SingleInputStream) inputStream).getStreamReferenceId());
        }
        return metaStreamEvent;
    }
}
