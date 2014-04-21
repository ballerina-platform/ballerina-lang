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
package org.wso2.siddhi.query.api.query.input;

import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.SourceNotExistException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.input.handler.Transformer;
import org.wso2.siddhi.query.api.query.input.handler.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class BasicStream implements FilteredStream {

    protected String streamId;
    protected AbstractDefinition definition;
    protected String streamReferenceId;
    protected Filter filter = null;
    protected Transformer transformer = null;
    protected boolean isCounterStream = false;
    protected QueryEventSource queryEventSource = null;


    protected BasicStream(String streamId, String streamReferenceId,
                          Filter filter) {
        this.streamId = streamId;
        this.streamReferenceId = streamReferenceId;
        this.filter = filter;
    }

    protected BasicStream(String streamId) {
        this(streamId, streamId);
    }

    public BasicStream(String streamReferenceId, String streamId) {
        this.streamId = streamId;
        this.streamReferenceId = streamReferenceId;
    }

    public AbstractDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(AbstractDefinition definition) {
        this.definition = definition;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getStreamReferenceId() {
        return streamReferenceId;
    }

    public BasicStream setStreamReferenceId(String streamReferenceId) {
        this.streamReferenceId = streamReferenceId;
        return this;
    }

    @Override
    public List<String> getStreamIds() {
        List<String> list = new ArrayList<String>();
        list.add(streamId);
        return list;
    }

    public void setCounterStream(boolean counterStream) {
        isCounterStream = counterStream;
    }

    @Override
    public List<QueryEventSource> constructQueryEventSourceList(
            ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
            List<QueryEventSource> queryEventSources) {
        definition = streamTableDefinitionMap.get(streamId);
        if (definition == null) {
            throw new SourceNotExistException("Definition not exist! No stream/table defined with stream ID: " + streamId);
        }
        if (definition instanceof TableDefinition) {
            if (filter != null || transformer != null) {
                throw new SourceNotExistException(streamId + " is not a Stream but a Table, and it cant have filter or transformer");
            }

        }
        queryEventSource = new QueryEventSource(streamId, streamReferenceId,
                                                definition,
                                                filter, transformer, null);
        queryEventSource.setCounterStream(isCounterStream);
        queryEventSources.add(queryEventSource);
        return queryEventSources;
    }


    public Filter getFilter() {
        return filter;
    }

    @Override
    public QueryEventSource getQueryEventSource() {
        return queryEventSource;
    }

    public FilteredStream filter(Condition filterCondition) {
        filter = new Filter(filterCondition);
        return this;
    }

    public FilteredStream setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public WindowStream window(String name, Expression... parameters) {
        return new WindowStream(this, new Window(name, parameters));
    }

    @Override
    public WindowStream window(String namespace, String function, Expression... parameters) {
        return new WindowStream(this, new Window(namespace, function, parameters));
    }

    public WindowStream window(Window window) {
        return new WindowStream(this, window);
    }

    @Override
    public TransformedStream transform(String name, Expression... parameters) {
        transformer = new Transformer(name, parameters);
        return this;
    }

    @Override
    public TransformedStream transform(String extensionName, String functionName,
                                       Expression... parameters) {
        transformer = new Transformer(extensionName, functionName, parameters);
        return this;
    }

    public TransformedStream setTransformer(Transformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public Transformer getTransformer() {
        return transformer;
    }
}