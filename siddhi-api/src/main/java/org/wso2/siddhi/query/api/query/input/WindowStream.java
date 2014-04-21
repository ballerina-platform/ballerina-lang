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

import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.SourceNotExistException;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.input.handler.Transformer;
import org.wso2.siddhi.query.api.query.input.handler.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class WindowStream implements SingleStream {

    protected String streamId;
    protected StreamDefinition streamDefinition;
    protected String streamReferenceId;
    protected Filter filter = null;
    protected Window window = null;
    protected Transformer transformer = null;
    protected QueryEventSource queryEventSource = null;

    public WindowStream(BasicStream basicStream, Window window) {
        this.streamId = basicStream.getStreamId();
        this.streamDefinition = (StreamDefinition) basicStream.getDefinition();
        this.streamReferenceId = basicStream.getStreamReferenceId();
        this.filter = basicStream.getFilter();
        this.transformer = basicStream.getTransformer();
        this.window = window;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getStreamReferenceId() {
        return streamReferenceId;
    }

    public WindowStream setStreamReferenceId(String streamReferenceId) {
        this.streamReferenceId = streamReferenceId;
        return this;
    }

    @Override
    public List<String> getStreamIds() {
        List<String> list = new ArrayList<String>();
        list.add(streamId);
        return list;
    }


    @Override
    public List<QueryEventSource> constructQueryEventSourceList(
            ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
            List<QueryEventSource> queryEventSources) {
        AbstractDefinition definition = streamTableDefinitionMap.get(streamId);
        if (definition == null) {
            throw new SourceNotExistException("Stream definition not exist! No steam defined with stream ID: " + streamId);
        }
        if (definition instanceof TableDefinition) {
            throw new SourceNotExistException(streamId + " is not a Stream but a Table, and it cant have window");
        }
        streamDefinition = (StreamDefinition) definition;
        queryEventSource = new QueryEventSource(streamId, streamReferenceId,
                                                streamDefinition,
                                                filter, transformer, window);

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

    public Window getWindow() {
        return window;
    }

    public Transformer getTransformer() {
        return transformer;
    }
}
