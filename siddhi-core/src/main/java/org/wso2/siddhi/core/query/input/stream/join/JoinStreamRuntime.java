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
package org.wso2.siddhi.core.query.input.stream.join;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JoinStreamRuntime implements StreamRuntime {

    List<SingleStreamRuntime> singleStreamRuntimeList = new ArrayList<SingleStreamRuntime>();
    private ExecutionPlanContext executionPlanContext;
    private MetaStateEvent metaStateEvent;

    public JoinStreamRuntime(ExecutionPlanContext executionPlanContext, MetaStateEvent metaStateEvent) {

        this.executionPlanContext = executionPlanContext;
        this.metaStateEvent = metaStateEvent;
    }


    public void addRuntime(SingleStreamRuntime singleStreamRuntime) {
        singleStreamRuntimeList.add(singleStreamRuntime);
    }

    public List<SingleStreamRuntime> getSingleStreamRuntimes() {
        return singleStreamRuntimeList;
    }

    @Override
    public StreamRuntime clone(String key) {

        JoinStreamRuntime joinStreamRuntime = new JoinStreamRuntime(executionPlanContext, metaStateEvent);
        for (SingleStreamRuntime singleStreamRuntime : singleStreamRuntimeList) {
            joinStreamRuntime.addRuntime((SingleStreamRuntime) singleStreamRuntime.clone(key));
        }
        SingleStreamRuntime leftSingleStreamRuntime = joinStreamRuntime.getSingleStreamRuntimes().get(0);
        SingleStreamRuntime rightSingleStreamRuntime = joinStreamRuntime.getSingleStreamRuntimes().get(1);

        Processor leftWindowProcessor = leftSingleStreamRuntime.getProcessorChain();
        Processor leftPostJoinProcessor = leftWindowProcessor.getNextProcessor();

        while (!(leftPostJoinProcessor instanceof JoinProcessor)) {
            leftWindowProcessor = leftWindowProcessor.getNextProcessor();
            leftPostJoinProcessor = leftWindowProcessor.getNextProcessor();
        }

        Processor rightWindowProcessor = rightSingleStreamRuntime.getProcessorChain();
        Processor rightPostJoinProcessor = rightWindowProcessor.getNextProcessor();

        while (!(rightPostJoinProcessor instanceof JoinProcessor)) {
            rightWindowProcessor = rightWindowProcessor.getNextProcessor();
            rightPostJoinProcessor = rightWindowProcessor.getNextProcessor();
        }

        ((JoinProcessor) rightPostJoinProcessor).setFindableProcessor((FindableProcessor) leftWindowProcessor);
        ((JoinProcessor) leftPostJoinProcessor).setFindableProcessor((FindableProcessor) rightWindowProcessor);

        return joinStreamRuntime;
    }

    @Override
    public void setCommonProcessor(Processor commonProcessor) {
        for (SingleStreamRuntime singleStreamRuntime : singleStreamRuntimeList) {
            singleStreamRuntime.setCommonProcessor(commonProcessor);
        }
    }

    @Override
    public MetaComplexEvent getMetaComplexEvent() {
        return metaStateEvent;
    }
}
