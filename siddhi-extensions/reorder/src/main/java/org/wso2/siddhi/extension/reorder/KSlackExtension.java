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

package org.wso2.siddhi.extension.reorder;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The following code conducts reordering of an out-of-order event stream.
 * This implements the K-Slack based disorder handling algorithm which was originally described in
 * https://www2.informatik.uni-erlangen.de/publication/download/IPDPS2013.pdf
 */
public class KSlackExtension extends StreamProcessor implements SchedulingProcessor {
    private long k = 0; //In the beginning the K is zero.
    private long greatestTimestamp = 0; //Used to track the greatest timestamp of tuples seen so far in the stream history.
    private TreeMap<Long, ArrayList<StreamEvent>> eventTreeMap;
    private TreeMap<Long, ArrayList<StreamEvent>> expiredEventTreeMap;
    private ExpressionExecutor timestampExecutor;
    private long MAX_K = Long.MAX_VALUE;
    private long TIMER_DURATION = -1l;
    private boolean expireFlag = false;
    private long lastSentTimeStamp = -1l;
    private Scheduler scheduler;
    private long lastScheduledTimestamp = -1;
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {
        //Do nothing
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        ComplexEventChunk<StreamEvent> complexEventChunk = new ComplexEventChunk<StreamEvent>(false);
        lock.lock();
        try {
            while (streamEventChunk.hasNext()) {
                StreamEvent event = streamEventChunk.next();

                if(event.getType() != ComplexEvent.Type.TIMER) {

                    streamEventChunk.remove(); //We might have the rest of the events linked to this event forming a chain.

                    long timestamp = (Long) timestampExecutor.execute(event);

                    if (expireFlag) {
                        if (timestamp < lastSentTimeStamp) {
                            continue;
                        }
                    }

                    ArrayList<StreamEvent> eventList = eventTreeMap.get(timestamp);
                    if (eventList == null) {
                        eventList = new ArrayList<StreamEvent>();
                    }
                    eventList.add(event);
                    eventTreeMap.put(timestamp, eventList);

                    if (timestamp > greatestTimestamp) {
                        greatestTimestamp = timestamp;
                        long minTimestamp = eventTreeMap.firstKey();
                        long timeDifference = greatestTimestamp - minTimestamp;

                        if (timeDifference > k) {
                            if (timeDifference < MAX_K) {
                                k = timeDifference;
                            } else {
                                k = MAX_K;
                            }
                        }

                        Iterator<Map.Entry<Long, ArrayList<StreamEvent>>> entryIterator = eventTreeMap.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<Long, ArrayList<StreamEvent>> entry = entryIterator.next();
                            ArrayList<StreamEvent> list = expiredEventTreeMap.get(entry.getKey());

                            if (list != null) {
                                list.addAll(entry.getValue());
                            } else {
                                expiredEventTreeMap.put(entry.getKey(), entry.getValue());
                            }
                        }
                        eventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();

                        entryIterator = expiredEventTreeMap.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<Long, ArrayList<StreamEvent>> entry = entryIterator.next();

                            if (entry.getKey() + k <= greatestTimestamp) {
                                entryIterator.remove();
                                ArrayList<StreamEvent> timeEventList = entry.getValue();
                                lastSentTimeStamp = entry.getKey();

                                for (StreamEvent aTimeEventList : timeEventList) {
                                    complexEventChunk.add(aTimeEventList);
                                }
                            }
                        }
                    }
                } else {
                    if(expiredEventTreeMap.size() > 0) {
                        TreeMap<Long, ArrayList<StreamEvent>> expiredEventTreeMapSnapShot = expiredEventTreeMap;
                        expiredEventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();
                        onTimerEvent(expiredEventTreeMapSnapShot, nextProcessor);
                        lastScheduledTimestamp = lastScheduledTimestamp + TIMER_DURATION;
                        scheduler.notifyAt(lastScheduledTimestamp);
                    }
                }



            }
        } catch (ArrayIndexOutOfBoundsException ec) {
            //This happens due to user specifying an invalid field index.
            throw new ExecutionPlanCreationException("The very first parameter must be an Integer with a valid " +
                    " field index (0 to (fieldsLength-1)).");
        }
        lock.unlock();
        nextProcessor.process(complexEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        if (attributeExpressionLength > 4) {
            throw new ExecutionPlanCreationException("Maximum four input parameters can be specified for KSlack. " +
                    " Timestamp field (long), k-slack buffer expiration time-out window (long), Max_K size (long), and boolean " +
                    " flag to indicate whether the late events should get discarded. But found " +
                    attributeExpressionLength + " attributes.");
        }

        //This is the most basic case. Here we do not use a timer. The basic K-slack algorithm is implemented.
        if(attributeExpressionExecutors.length == 1){
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                timestampExecutor = attributeExpressionExecutors[0];
                attributes.add(new Attribute("beta0", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the first argument of " +
                        "reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[0].getReturnType());
            }
            //In the following case we have the timer operating in background. But we do not impose a K-slack window length.
        }else if(attributeExpressionExecutors.length == 2){
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                timestampExecutor = attributeExpressionExecutors[0];
                attributes.add(new Attribute("beta0", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the first argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[0].getReturnType());
            }

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                TIMER_DURATION = (Long)attributeExpressionExecutors[1].execute(null);
                attributes.add(new Attribute("beta1", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the second argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[1].getReturnType());
            }
            //In the third case we have both the timer operating in the background and we have also specified a K-slack window length.
        }else if(attributeExpressionExecutors.length == 3){
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                timestampExecutor = attributeExpressionExecutors[0];
                attributes.add(new Attribute("beta0", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the first argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[0].getReturnType());
            }

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                TIMER_DURATION = (Long)attributeExpressionExecutors[1].execute(null);
                attributes.add(new Attribute("beta1", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the second argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[1].getReturnType());
            }

            if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.LONG) {
                MAX_K = (Long)attributeExpressionExecutors[2].execute(null);
                attributes.add(new Attribute("beta2", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the third argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[2].getReturnType());
            }
            //In the fourth case we have an additional boolean flag other than the above three parameters. If the flag
            // is set to true any out-of-order events which arrive after the expiration of K-slack are discarded.
        }else if(attributeExpressionExecutors.length == 4){
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                timestampExecutor = attributeExpressionExecutors[0];
                attributes.add(new Attribute("beta0", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the first argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[0].getReturnType());
            }

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                TIMER_DURATION = (Long)attributeExpressionExecutors[1].execute(null);
                attributes.add(new Attribute("beta1", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the second argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[1].getReturnType());
            }

            if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.LONG) {
                MAX_K = (Long)attributeExpressionExecutors[2].execute(null);
                attributes.add(new Attribute("beta2", Attribute.Type.LONG));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the third argument of " +
                        " reorder:kslack() function. Required LONG, but found " +
                        attributeExpressionExecutors[2].getReturnType());
            }

            if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.BOOL) {
                expireFlag = (Boolean)attributeExpressionExecutors[3].execute(null);
                attributes.add(new Attribute("beta3", Attribute.Type.BOOL));
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found for the fourth argument of " +
                        " reorder:kslack() function. Required BOOL, but found " +
                        attributeExpressionExecutors[3].getReturnType());
            }
        }

        if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
            timestampExecutor = attributeExpressionExecutors[0];
        } else {
            throw new ExecutionPlanCreationException("Return type expected by KSlack is LONG but found " +
                    attributeExpressionExecutors[0].getReturnType());
        }

        eventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();
        expiredEventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();

        if(TIMER_DURATION != -1l && scheduler != null) {
            lastScheduledTimestamp = executionPlanContext.getTimestampGenerator().currentTime() + TIMER_DURATION;
            scheduler.notifyAt(lastScheduledTimestamp);
        }
        return attributes;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        if (lastScheduledTimestamp < 0) {
            lastScheduledTimestamp = executionPlanContext.getTimestampGenerator().currentTime() + TIMER_DURATION;
            scheduler.notifyAt(lastScheduledTimestamp);
        }
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    private void onTimerEvent(TreeMap<Long, ArrayList<StreamEvent>> treeMap, Processor nextProcessor) {
        Iterator<Map.Entry<Long, ArrayList<StreamEvent>>> entryIterator = treeMap.entrySet().iterator();
        ComplexEventChunk<StreamEvent> complexEventChunk = new ComplexEventChunk<StreamEvent>(false);

        while (entryIterator.hasNext()) {
            ArrayList<StreamEvent> timeEventList = entryIterator.next().getValue();

            for (StreamEvent aTimeEventList : timeEventList) {
                complexEventChunk.add(aTimeEventList);
            }
        }
        nextProcessor.process(complexEventChunk);
    }
}
