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
package org.wso2.siddhi.core.query.processor.handler;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.BundleEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.snapshot.ThreadBarrier;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerElement;
import org.wso2.siddhi.core.query.processor.PreSelectProcessingElement;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueue;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.concurrent.ThreadPoolExecutor;

public class SimpleHandlerProcessor implements HandlerProcessor, PreSelectProcessingElement, Runnable, SchedulerElement {

    static final Logger log = Logger.getLogger(TableHandlerProcessor.class);

    private QueryEventSource queryEventSource;
    private ThreadPoolExecutor threadPoolExecutor;
    private SchedulerSiddhiQueue<StreamEvent> inputQueue;
    private FilterProcessor filterProcessor;
    private TransformProcessor transformProcessor;
    private SiddhiContext context;
    private final ThreadBarrier threadBarrier;


    protected QueryPostProcessingElement next;

    public SimpleHandlerProcessor(QueryEventSource queryEventSource,
                                  FilterProcessor filterProcessor,
                                  TransformProcessor transformProcessor,
                                  SiddhiContext siddhiContext) {
        this.queryEventSource = queryEventSource;
        this.filterProcessor = filterProcessor;
        this.transformProcessor = transformProcessor;
        this.threadPoolExecutor = siddhiContext.getThreadPoolExecutor();
        this.context = siddhiContext;
        this.threadBarrier = siddhiContext.getThreadBarrier();
        this.inputQueue = new SchedulerSiddhiQueue<StreamEvent>(this);

    }

    @Override
    public void receive(StreamEvent streamEvent) {
//        System.out.println(event);
        if (context.isAsyncProcessing() || context.isDistributedProcessingEnabled()) {
            inputQueue.put(streamEvent);
        } else {
            if (streamEvent instanceof AtomicEvent) {
                processHandler((AtomicEvent) streamEvent);
            } else {
                processHandler((BundleEvent) streamEvent);
            }
        }
    }


    @Override
    public void run() {
        try {

            InListEvent listEvent = new InListEvent();
            while (true) {
                threadBarrier.pass();
                StreamEvent streamEvent = inputQueue.poll();
                if (streamEvent == null) {
                    if (listEvent.getActiveEvents() == 1) {
                        processHandler(listEvent.getEvent0());
                    } else if (listEvent.getActiveEvents() > 1) {
                        processHandler(listEvent);
                    }
                    break;
                } else if (context.getEventBatchSize() > 0 && listEvent.getActiveEvents() > context.getEventBatchSize()) {
                    if (listEvent.getActiveEvents() == 1) {
                        processHandler(listEvent.getEvent0());
                    } else if (listEvent.getActiveEvents() > 1) {
                        processHandler(listEvent);
                    }
                    threadPoolExecutor.execute(this);
                    break;
                }
                if (streamEvent instanceof ListEvent) {
                    for (Event event : ((ListEvent) streamEvent).getEvents()) {
                        listEvent.addEvent(event);
                    }
                } else {
                    listEvent.addEvent((Event) streamEvent);
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    protected void processHandler(BundleEvent bundleEvent) {
        bundleEvent = filterProcessor.process(bundleEvent);
        if (bundleEvent != null) {
            if (transformProcessor != null) {
                InStream inStreamEvent = transformProcessor.process(bundleEvent);
                if (inStreamEvent instanceof AtomicEvent) {
                    next.process((AtomicEvent) inStreamEvent);
                } else {  //BundleEvent
                    next.process((BundleEvent) inStreamEvent);
                }
            } else {
                next.process(bundleEvent);
            }
        }
    }

    protected void processHandler(AtomicEvent atomicEvent) {
        atomicEvent = filterProcessor.process(atomicEvent);
        if (atomicEvent != null) {
            if (transformProcessor != null) {
                InStream inStreamEvent = transformProcessor.process(atomicEvent);
                if (inStreamEvent instanceof AtomicEvent) {
                    next.process((AtomicEvent) inStreamEvent);
                } else {  //BundleEvent
                    next.process((BundleEvent) inStreamEvent);
                }
            } else {
                next.process(atomicEvent);
            }
        }
    }

    public String getStreamId() {
        return queryEventSource.getSourceId();
    }

    @Override
    public void schedule() {
        threadPoolExecutor.execute(this);
    }

    @Override
    public void scheduleNow() {
        threadPoolExecutor.execute(this);
    }

    public void setNext(QueryPostProcessingElement next) {
        this.next = next;
    }

    @Override
    public void setNext(QuerySelector querySelector) {
        this.next = querySelector;
    }
}
