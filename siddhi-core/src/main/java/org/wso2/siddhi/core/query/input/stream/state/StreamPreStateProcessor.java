/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org)
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

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.processor.Processor;

/**
 * Created on 12/17/14.
 */
public class StreamPreStateProcessor implements PreStateProcessor {

    protected int stateId;
    protected boolean isStartState;
    protected volatile boolean stateChanged = false;

    protected Processor nextProcessor;

    protected ComplexEventChunk<StateEvent> currentStateEventChunk = new ComplexEventChunk<StateEvent>();
    protected ComplexEventChunk<StateEvent> pendingStateEventChunk = new ComplexEventChunk<StateEvent>();
    protected ComplexEventChunk<StateEvent> newAndEveryStateEventChunk = new ComplexEventChunk<StateEvent>();

    protected StateEventPool stateEventPool;
    //  private StreamEventPool streamEventPool;
    protected StreamEventCloner streamEventCloner;
    protected StateEventCloner stateEventCloner;
    protected StreamEventPool streamEventPool;


    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        System.out.println(stateId + " " + complexEventChunk);

        complexEventChunk.reset();
        StreamEvent streamEvent = (StreamEvent) complexEventChunk.next(); //Sure only one will be sent
        while (pendingStateEventChunk.hasNext()) {
            StateEvent stateEvent = pendingStateEventChunk.next();
            StateEvent clonedStateEvent = stateEventCloner.copyStateEvent(stateEvent);
            clonedStateEvent.setEvent(stateId, streamEventCloner.copyStreamEvent(streamEvent));
            System.out.println("PSP:" + clonedStateEvent);
            currentStateEventChunk.add(clonedStateEvent);
            currentStateEventChunk.reset();
            stateChanged = false;
            nextProcessor.process(currentStateEventChunk);

            if (stateChanged) {
                pendingStateEventChunk.remove();
                stateEventPool.returnEvents(stateEvent);
            }
            currentStateEventChunk.reset();
        }
        pendingStateEventChunk.reset();

    }

    /**
     * Get next processor element in the processor chain. Processed event should be sent to next processor
     *
     * @return
     */
    @Override
    public Processor getNextProcessor() {
        return nextProcessor;
    }

    /**
     * Set next processor element in processor chain
     *
     * @param processor Processor to be set as next element of processor chain
     */
    @Override
    public void setNextProcessor(Processor processor) {
        this.nextProcessor = processor;
    }

    /**
     * Set as the last element of the processor chain
     *
     * @param processor Last processor in the chain
     */
    @Override
    public void setToLast(Processor processor) {
        if (nextProcessor == null) {
            this.nextProcessor = processor;
        } else {
            this.nextProcessor.setToLast(processor);
        }
    }

    public void init() {
        if (isStartState) {
            StateEvent stateEvent = stateEventPool.borrowEvent();
            newAndEveryStateEventChunk.add(stateEvent);
        }
    }

    /**
     * Clone a copy of processor
     *
     * @return
     */
    @Override
    public Processor cloneProcessor() {
        return null;
    }

    @Override
    public void addState(StateEvent stateEvent) {
        System.out.println("PSP: add " + stateId + " " + stateEvent);
        newAndEveryStateEventChunk.add(stateEvent);
    }

    @Override
    public void addEveryState(StateEvent stateEvent) {
        System.out.println("PSP: addEvery " + stateId + " " + stateEvent);
        newAndEveryStateEventChunk.add(stateEventCloner.copyStateEvent(stateEvent));
    }

    public void stateChanged() {
        stateChanged = true;
    }

    public void setStartState(boolean isStartState) {
        this.isStartState = isStartState;
    }

    public void setStateEventPool(StateEventPool stateEventPool) {
        this.stateEventPool = stateEventPool;
    }

    public void setStreamEventPool(StreamEventPool streamEventPool) {
        this.streamEventPool = streamEventPool;
    }

    public void setStreamEventCloner(StreamEventCloner streamEventCloner) {
        this.streamEventCloner = streamEventCloner;
    }

    public void setStateEventCloner(StateEventCloner stateEventCloner) {
        this.stateEventCloner = stateEventCloner;
    }

    @Override
    public void updateState() {
        System.out.println("PSP: update " + stateId + " " + newAndEveryStateEventChunk);
        newAndEveryStateEventChunk.reset();
        while (newAndEveryStateEventChunk.hasNext()) {
            StateEvent stateEvent = newAndEveryStateEventChunk.next();
            newAndEveryStateEventChunk.remove();
            pendingStateEventChunk.add(stateEvent);
        }
        newAndEveryStateEventChunk.clear();
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    @Override
    public int getStateId() {
        return stateId;
    }
}
