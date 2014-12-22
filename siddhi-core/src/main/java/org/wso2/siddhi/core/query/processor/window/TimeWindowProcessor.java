/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.query.processor.window;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.join.Finder;
import org.wso2.siddhi.core.query.input.stream.single.SingleThreadEntryValveProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.util.Scheduler;

public class TimeWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private ComplexEventChunk<StreamEvent> expiredEventChunk;
    private Scheduler scheduler;

    public void setTimeInMilliSeconds(long timeInMilliSeconds) {
        this.timeInMilliSeconds = timeInMilliSeconds;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public StreamEvent find(Finder finder) {    //todo optimize
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
        expiredEventChunk.reset();
        while (expiredEventChunk.hasNext()) {
            StreamEvent streamEvent = expiredEventChunk.next();
            if (finder.execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }

    @Override
    protected void init(ExpressionExecutor[] inputExecutors) {
        expiredEventChunk = new ComplexEventChunk<StreamEvent>();
        if (inputExecutors != null) {
            timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) inputExecutors[0]).getValue();
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {

        while (streamEventChunk.hasNext()) {

            StreamEvent streamEvent = streamEventChunk.next();
            long currentTime = System.currentTimeMillis();  //todo fix

            StreamEvent clonedEvent = null;
            if (streamEvent.getType() == StreamEvent.Type.CURRENT) {
                clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
                clonedEvent.setType(StreamEvent.Type.EXPIRED);
                clonedEvent.setTimestamp(currentTime + timeInMilliSeconds);
            }

            while (expiredEventChunk.hasNext()) {
                StreamEvent expiredEvent = expiredEventChunk.next();
                long timeDiff = expiredEvent.getTimestamp() - currentTime;
                if (timeDiff <= 0) {
                    expiredEventChunk.remove();
                    streamEventChunk.insertBeforeCurrent(expiredEvent);
                } else {
                    scheduler.notifyAt(expiredEvent.getTimestamp());
                    expiredEventChunk.reset();
                    break;
                }
            }
            if (streamEvent.getType() == StreamEvent.Type.CURRENT) {
                this.expiredEventChunk.add(clonedEvent);
            }
            expiredEventChunk.reset();
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    public TimeWindowProcessor cloneWindowProcessor() {
        TimeWindowProcessor windowProcessor = new TimeWindowProcessor();
        windowProcessor.setTimeInMilliSeconds(this.timeInMilliSeconds);
        windowProcessor.expiredEventChunk = new ComplexEventChunk<StreamEvent>();
        return windowProcessor;
    }

    public void cloneScheduler(TimeWindowProcessor timeWindowProcessor,SingleThreadEntryValveProcessor singleThreadEntryValveProcessor){
        this.scheduler = timeWindowProcessor.scheduler.cloneScheduler(singleThreadEntryValveProcessor);
    }
}
