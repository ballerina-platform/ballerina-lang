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
package org.wso2.siddhi.query.api.query;

import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.input.handler.Transformer;
import org.wso2.siddhi.query.api.query.input.handler.Window;

public class QueryEventSource {

    private String sourceId;
    private String referenceSourceId;
    private AbstractDefinition inDefinition;
    private AbstractDefinition outDefinition;
    private boolean isCounterStream;
    private Filter filter;
    private Transformer transformer;
    private Window window;

    public QueryEventSource(String sourceId, String referenceSourceId,
                            AbstractDefinition inDefinition, Filter filter,
                            Transformer transformer, Window window) {
        this.sourceId = sourceId;
        this.referenceSourceId = referenceSourceId;
        this.inDefinition = inDefinition;
        this.filter = filter;
        this.transformer = transformer;
        this.window = window;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getReferenceSourceId() {
        return referenceSourceId;
    }

    public void setReferenceSourceId(String referenceSourceId) {
        this.referenceSourceId = referenceSourceId;
    }

    public AbstractDefinition getInDefinition() {
        return inDefinition;
    }

    public void setInDefinition(StreamDefinition inStreamDefinition) {
        this.inDefinition = inStreamDefinition;
    }

    public AbstractDefinition getOutDefinition() {
        return outDefinition;
    }

    public void setOutDefinition(AbstractDefinition outDefinition) {
        this.outDefinition = outDefinition;
    }

    public boolean isCounterStream() {
        return isCounterStream;
    }

    public void setCounterStream(boolean counterStream) {
        isCounterStream = counterStream;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
    }
}
