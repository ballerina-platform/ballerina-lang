/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.execution.query.input.stream;

import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

/**
 * Basic single input stream using filters, windows and functions
 */
public class BasicSingleInputStream extends SingleInputStream {

    protected BasicSingleInputStream(String streamId) {
        this(streamId, false);
    }

    protected BasicSingleInputStream(String streamId, boolean isInnerStream) {
        super(streamId, isInnerStream);
    }

    public BasicSingleInputStream(String streamReferenceId, String streamId) {
        this(streamReferenceId, streamId, false);
    }

    public BasicSingleInputStream(String streamReferenceId, String streamId, boolean isInnerStream) {
        super(streamReferenceId, streamId, isInnerStream);
    }

    public String getStreamId() {
        return streamId;
    }

    public String getStreamReferenceId() {
        return streamReferenceId;
    }

    public BasicSingleInputStream as(String streamReferenceId) {
        this.streamReferenceId = streamReferenceId;
        return this;
    }

    public List<StreamHandler> getStreamHandlers() {
        return streamHandlers;
    }

    public void addStreamHandlers(List<StreamHandler> streamHandlers) {
        this.streamHandlers = streamHandlers;
    }

    public BasicSingleInputStream filter(Expression filterExpression) {
        streamHandlers.add(new Filter(filterExpression));
        return this;
    }

    public BasicSingleInputStream filter(Filter filter) {
        streamHandlers.add(filter);
        return this;
    }

    public SingleInputStream window(String name, Expression... parameters) {
        return new SingleInputStream(this, new Window(name, parameters));
    }

    public SingleInputStream window(String namespace, String function, Expression... parameters) {
        return new SingleInputStream(this, new Window(namespace, function, parameters));
    }

    public SingleInputStream window(Window window) {
        return new SingleInputStream(this, window);
    }

    public BasicSingleInputStream function(String name, Expression... parameters) {
        streamHandlers.add(new StreamFunction(name, parameters));
        return this;
    }

    public BasicSingleInputStream function(String extensionNamespace, String functionName,
                                           Expression... parameters) {
        streamHandlers.add(new StreamFunction(extensionNamespace, functionName, parameters));
        return this;
    }

    public BasicSingleInputStream function(StreamFunction streamFunction) {
        streamHandlers.add(streamFunction);
        return this;
    }
}
