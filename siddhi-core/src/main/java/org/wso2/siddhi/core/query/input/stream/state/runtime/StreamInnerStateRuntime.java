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
package org.wso2.siddhi.core.query.input.stream.state.runtime;

import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.PostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.PreStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.StreamPostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12/19/14.
 */
public class StreamInnerStateRuntime implements InnerStateRuntime {

    protected List<SingleStreamRuntime> singleStreamRuntimeList = new ArrayList<SingleStreamRuntime>();
    protected PreStateProcessor firstProcessor;
    protected PostStateProcessor lastProcessor;
    protected StateInputStream.Type stateType;

    public StreamInnerStateRuntime(StateInputStream.Type stateType) {
        this.stateType = stateType;
    }

    @Override
    public PreStateProcessor getFirstProcessor() {
        return firstProcessor;
    }

    public void setFirstProcessor(PreStateProcessor firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    @Override
    public PostStateProcessor getLastProcessor() {
        return lastProcessor;
    }

    public void setLastProcessor(PostStateProcessor lastProcessor) {
        this.lastProcessor = lastProcessor;
    }

    @Override
    public List<SingleStreamRuntime> getSingleStreamRuntimeList() {
        return singleStreamRuntimeList;
    }

    public void addStreamRuntime(SingleStreamRuntime singleStreamRuntime) {
        this.singleStreamRuntimeList.add(singleStreamRuntime);
    }

    @Override
    public void setQuerySelector(Processor commonProcessor) {
        lastProcessor.setNextProcessor(commonProcessor);
    }

    @Override
    public void setStartState() {
        firstProcessor.setStartState(true);
    }

    public StateInputStream.Type getStateType() {
        return stateType;
    }

    @Override
    public void init() {
        singleStreamRuntimeList.get(0).getProcessStreamReceiver().setNext(firstProcessor);
        singleStreamRuntimeList.get(0).getProcessStreamReceiver().addStatefulProcessor(firstProcessor);
        if (stateType == StateInputStream.Type.PATTERN) {
            firstProcessor.init();
        }
    }

    @Override
    public void reset() {
        firstProcessor.resetState();
    }

    @Override
    public void update() {
        firstProcessor.updateState();
    }

    @Override
    public InnerStateRuntime clone(String key) {
        StreamInnerStateRuntime streamInnerStateRuntime = new StreamInnerStateRuntime(stateType);
        for (SingleStreamRuntime singleStreamRuntime : singleStreamRuntimeList) {
            streamInnerStateRuntime.singleStreamRuntimeList.add((SingleStreamRuntime) singleStreamRuntime.clone(key));
        }

        Processor processor = streamInnerStateRuntime.singleStreamRuntimeList.get(0).getProcessorChain();
        streamInnerStateRuntime.firstProcessor = (StreamPreStateProcessor) processor;

        while (processor != null) {
            if (processor instanceof StreamPostStateProcessor) {
                streamInnerStateRuntime.lastProcessor = (StreamPostStateProcessor) processor;
                break;
            } else {
                processor = processor.getNextProcessor();
            }
        }

        ((StreamPostStateProcessor) streamInnerStateRuntime.lastProcessor).setThisStatePreProcessor(
                (StreamPreStateProcessor) streamInnerStateRuntime.firstProcessor);
        ((StreamPreStateProcessor) streamInnerStateRuntime.firstProcessor).setThisStatePostProcessor(
                (StreamPostStateProcessor) streamInnerStateRuntime.lastProcessor);
        ((StreamPreStateProcessor) streamInnerStateRuntime.firstProcessor).setThisLastProcessor(
                (StreamPostStateProcessor) streamInnerStateRuntime.lastProcessor);
        return streamInnerStateRuntime;
    }
}
