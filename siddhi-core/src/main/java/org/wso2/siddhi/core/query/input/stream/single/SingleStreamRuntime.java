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
package org.wso2.siddhi.core.query.input.stream.single;

import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.ArrayList;
import java.util.List;

/**
 * StreamRuntime to represent Single streams. Ex: Streams to filter queries.
 */
public class SingleStreamRuntime implements StreamRuntime {

    private Processor processorChain;
    private MetaComplexEvent metaComplexEvent;
    private ProcessStreamReceiver processStreamReceiver;

    public SingleStreamRuntime(ProcessStreamReceiver processStreamReceiver, Processor processorChain,
                               MetaComplexEvent metaComplexEvent) {
        this.processStreamReceiver = processStreamReceiver;
        this.processorChain = processorChain;
        this.metaComplexEvent = metaComplexEvent;
    }

    public Processor getProcessorChain() {
        return processorChain;
    }

    public void setProcessorChain(Processor processorChain) {
        this.processorChain = processorChain;
    }

    public ProcessStreamReceiver getProcessStreamReceiver() {
        return processStreamReceiver;
    }

    public void setProcessStreamReceiver(ProcessStreamReceiver processStreamReceiver) {
        this.processStreamReceiver = processStreamReceiver;
    }

    @Override
    public List<SingleStreamRuntime> getSingleStreamRuntimes() {
        List<SingleStreamRuntime> list = new ArrayList<SingleStreamRuntime>(1);
        list.add(this);
        return list;

    }

    @Override
    public StreamRuntime clone(String key) {
        ProcessStreamReceiver clonedProcessStreamReceiver = this.processStreamReceiver.clone(key);
        EntryValveProcessor entryValveProcessor = null;
        SchedulingProcessor schedulingProcessor;
        Processor clonedProcessorChain = null;
        if (processorChain != null) {
            if (!(processorChain instanceof QuerySelector || processorChain instanceof OutputRateLimiter)) {
                clonedProcessorChain = processorChain.cloneProcessor(key);
                if (clonedProcessorChain instanceof EntryValveProcessor) {
                    entryValveProcessor = (EntryValveProcessor) clonedProcessorChain;
                }
            }
            Processor processor = processorChain.getNextProcessor();
            while (processor != null) {
                if (!(processor instanceof QuerySelector || processor instanceof OutputRateLimiter)) {
                    Processor clonedProcessor = processor.cloneProcessor(key);
                    clonedProcessorChain.setToLast(clonedProcessor);
                    if (clonedProcessor instanceof EntryValveProcessor) {
                        entryValveProcessor = (EntryValveProcessor) clonedProcessor;
                    } else if (clonedProcessor instanceof SchedulingProcessor) {
                        schedulingProcessor = (SchedulingProcessor) clonedProcessor;
                        schedulingProcessor.setScheduler(((SchedulingProcessor) processor).getScheduler().clone(
                                key, entryValveProcessor));
                    }
                }
                processor = processor.getNextProcessor();
            }
        }
        return new SingleStreamRuntime(clonedProcessStreamReceiver, clonedProcessorChain, metaComplexEvent);
    }

    @Override
    public void setCommonProcessor(Processor commonProcessor) {
        if (processorChain == null) {
            processStreamReceiver.setNext(commonProcessor);
        } else {
            processStreamReceiver.setNext(processorChain);
            processorChain.setToLast(commonProcessor);
        }
    }

    public MetaComplexEvent getMetaComplexEvent() {
        return metaComplexEvent;
    }
}
