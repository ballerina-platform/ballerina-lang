/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.List;

/**
 * Created on 12/19/14.
 */
public class NextInnerStateRuntime extends StreamInnerStateRuntime {

    private final InnerStateRuntime currentInnerStateRuntime;
    private final InnerStateRuntime nextInnerStateRuntime;

    public NextInnerStateRuntime(InnerStateRuntime currentInnerStateRuntime, InnerStateRuntime nextInnerStateRuntime, StateInputStream.Type stateType) {
        super(stateType);
        this.currentInnerStateRuntime = currentInnerStateRuntime;
        this.nextInnerStateRuntime = nextInnerStateRuntime;
    }

    @Override
    public void setQuerySelector(Processor commonProcessor) {
        nextInnerStateRuntime.setQuerySelector(commonProcessor);
    }

    @Override
    public void setStartState() {
        currentInnerStateRuntime.setStartState();
    }

    @Override
    public void init() {
        currentInnerStateRuntime.init();
        nextInnerStateRuntime.init();
    }

    @Override
    public void reset() {
        nextInnerStateRuntime.reset();
        currentInnerStateRuntime.reset();
    }

    @Override
    public void update() {
        currentInnerStateRuntime.update();
        nextInnerStateRuntime.update();
    }

    @Override
    public InnerStateRuntime clone(String key) {
        InnerStateRuntime cloned_currentInnerStateRuntime = currentInnerStateRuntime.clone(key);
        InnerStateRuntime cloned_nextInnerStateRuntime = nextInnerStateRuntime.clone(key);

        NextInnerStateRuntime nextInnerStateRuntime = new NextInnerStateRuntime(cloned_currentInnerStateRuntime, cloned_nextInnerStateRuntime, stateType);
        nextInnerStateRuntime.singleStreamRuntimeList.addAll(cloned_currentInnerStateRuntime.getSingleStreamRuntimeList());
        nextInnerStateRuntime.singleStreamRuntimeList.addAll(cloned_nextInnerStateRuntime.getSingleStreamRuntimeList());
        nextInnerStateRuntime.firstProcessor = cloned_currentInnerStateRuntime.getFirstProcessor();
        nextInnerStateRuntime.lastProcessor = cloned_nextInnerStateRuntime.getLastProcessor();

        cloned_currentInnerStateRuntime.getLastProcessor().setNextStatePreProcessor(cloned_nextInnerStateRuntime.getFirstProcessor());

        List<SingleStreamRuntime> runtimeList = nextInnerStateRuntime.getSingleStreamRuntimeList();
        for (int i = 0; i < runtimeList.size(); i++) {
            String streamId = runtimeList.get(i).getProcessStreamReceiver().getStreamId();
            for (int j = i; j < runtimeList.size(); j++) {
                if (streamId.equals(runtimeList.get(j).getProcessStreamReceiver().getStreamId())) {
                    runtimeList.get(j).setProcessStreamReceiver(runtimeList.get(i).getProcessStreamReceiver());
                }
            }
        }

        return nextInnerStateRuntime;
    }
}
