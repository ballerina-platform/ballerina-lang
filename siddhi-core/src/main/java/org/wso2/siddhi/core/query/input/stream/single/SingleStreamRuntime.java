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

package org.wso2.siddhi.core.query.input.stream.single;

import org.wso2.siddhi.core.query.input.QueryStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.window.TimeWindowProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.ArrayList;
import java.util.List;

public class SingleStreamRuntime implements StreamRuntime {

    private Processor processorChain;
    private QueryStreamReceiver queryStreamReceiver;

    public SingleStreamRuntime(QueryStreamReceiver queryStreamReceiver, Processor processorChain) {
        this.queryStreamReceiver = queryStreamReceiver;
        this.processorChain = processorChain;
        queryStreamReceiver.setProcessorChain(processorChain);
    }

    public Processor getProcessorChain() {
        return processorChain;
    }

    public void setProcessorChain(Processor processorChain) {
        this.processorChain = processorChain;
    }

    public QueryStreamReceiver getQueryStreamReceiver() {
        return queryStreamReceiver;
    }

    @Override
    public List<SingleStreamRuntime> getSingleStreamRuntimes() {
        List<SingleStreamRuntime> list = new ArrayList<SingleStreamRuntime>(1);
        list.add(this);
        return list;

    }

    @Override
    public StreamRuntime clone(String key) {
        QueryStreamReceiver clonedQueryStreamReceiver = this.queryStreamReceiver.clone(key);
        SingleThreadEntryValveProcessor singleThreadEntryValveProcessor = null;
        TimeWindowProcessor windowProcessor;
        Processor clonedProcessorChain = null;
        if (processorChain != null) {
            if (!(processorChain instanceof QuerySelector || processorChain instanceof OutputRateLimiter)) {
                clonedProcessorChain = processorChain.cloneProcessor();
                if(clonedProcessorChain instanceof SingleThreadEntryValveProcessor){
                    singleThreadEntryValveProcessor = (SingleThreadEntryValveProcessor) clonedProcessorChain;
                }
            }
            Processor processor = processorChain.getNextProcessor();
            while (processor != null) {
                if (!(processor instanceof QuerySelector || processor instanceof OutputRateLimiter)) {
                    Processor clonedProcessor = processor.cloneProcessor();
                    clonedProcessorChain.setToLast(clonedProcessor);
                    if(clonedProcessor instanceof SingleThreadEntryValveProcessor){
                        singleThreadEntryValveProcessor = (SingleThreadEntryValveProcessor) clonedProcessor;
                    } else if(clonedProcessor instanceof TimeWindowProcessor){
                        windowProcessor = (TimeWindowProcessor) clonedProcessor;
                        windowProcessor.cloneScheduler((TimeWindowProcessor) processor,singleThreadEntryValveProcessor);
                    }
                }
                processor = processor.getNextProcessor();
            }
        }
        return new SingleStreamRuntime(clonedQueryStreamReceiver, clonedProcessorChain);
    }

    @Override
    public void setCommonProcessor(Processor commonProcessor) {
        if (processorChain == null) {
            queryStreamReceiver.setNext(commonProcessor);
        } else {
            queryStreamReceiver.setNext(processorChain);
            processorChain.setToLast(commonProcessor);
        }
    }
}
