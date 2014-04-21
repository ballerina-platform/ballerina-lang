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
package org.wso2.siddhi.core.query.processor.handler.pattern;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.snapshot.ThreadBarrier;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerElement;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.MarkedElement;
import org.wso2.siddhi.core.util.LogHelper;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueue;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class PatternHandlerProcessor
        implements HandlerProcessor, Runnable, MarkedElement,
                   SchedulerElement {

    static final Logger log = Logger.getLogger(PatternHandlerProcessor.class);

    //  private List<BasicStream> inputStreamList;
    private String streamId;
    private ThreadPoolExecutor threadPoolExecutor;
    private SchedulerSiddhiQueue<StreamEvent> inputQueue;
    private List<PatternInnerHandlerProcessor> patternInnerHandlerProcessorList;
    private int patternInnerHandlerProcessorListSize;
    private SiddhiContext siddhiContext;
    private final ThreadBarrier threadBarrier;
    private String elementId;
    private QueryPostProcessingElement next;

    public PatternHandlerProcessor(String streamId,
                                   List<PatternInnerHandlerProcessor> patternInnerHandlerProcessorList,
                                   SiddhiContext siddhiContext) {
        this.streamId = streamId;
        this.patternInnerHandlerProcessorList = patternInnerHandlerProcessorList;
        this.threadPoolExecutor = siddhiContext.getThreadPoolExecutor();
        this.patternInnerHandlerProcessorListSize = patternInnerHandlerProcessorList.size();
        this.siddhiContext = siddhiContext;
        this.threadBarrier = siddhiContext.getThreadBarrier();
//        this.inputQueue = SchedulerQueueFactory.createSchedulerQueue(elementId, this, siddhiContext,false);
        this.inputQueue = new SchedulerSiddhiQueue<StreamEvent>(this);

    }


    @Override
    public void run() {
        try {
            int eventCounter = 0;
            while (true) {
                threadBarrier.pass();
                StreamEvent streamEvent = inputQueue.poll();
                if (streamEvent == null) {
                    break;
                } else if (siddhiContext.getEventBatchSize() > 0 && eventCounter > siddhiContext.getEventBatchSize()) {
                    threadPoolExecutor.execute(this);
                    break;
                }
                eventCounter++;
                process(streamEvent);
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    private void process(StreamEvent streamEvent) {
        try {
            //in reverse order to execute the later states first to overcome to dependencies of count states
            for (int i = patternInnerHandlerProcessorListSize - 1; i >= 0; i--) {
                patternInnerHandlerProcessorList.get(i).moveNextEventsToCurrentEvents();
            }
            for (int i = patternInnerHandlerProcessorListSize - 1; i >= 0; i--) {
                patternInnerHandlerProcessorList.get(i).process(streamEvent);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(StreamEvent streamEvent) {
        LogHelper.logMethod(log, streamEvent);
        if (siddhiContext.isAsyncProcessing() || siddhiContext.isDistributedProcessingEnabled()) {
            if (log.isDebugEnabled()) {
                LogHelper.debugLogMessage(log, streamEvent, " added to inputQueue");
            }
            inputQueue.put(streamEvent);
        } else {
            if (log.isDebugEnabled()) {
                LogHelper.debugLogMessage(log, streamEvent, " sent to process");
            }
            process(streamEvent);
        }

    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public void schedule() {
        threadPoolExecutor.execute(this);
    }

    @Override
    public void scheduleNow() {
        threadPoolExecutor.execute(this);
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public List<PatternInnerHandlerProcessor> getPatternInnerHandlerProcessorList() {
        return patternInnerHandlerProcessorList;
    }
}
