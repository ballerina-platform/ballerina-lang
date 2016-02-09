/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.common.disruptor.config;

import com.lmax.disruptor.RingBuffer;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the disruptor configuration.
 */
public class DisruptorConfig {

    private int bufferSize;
    private int noDisruptors;
    private int noOfEventHandlersPerDisruptor;
    private String disruptorWaitStrategy;
    private boolean notShared;
    private List<RingBuffer> disruptorMap = new ArrayList<>();
    private AtomicInteger index = new AtomicInteger(0);

    public DisruptorConfig() {

        this.bufferSize = 512;

        this.noDisruptors = 5;

        this.noOfEventHandlersPerDisruptor = 1;

        this.disruptorWaitStrategy = Constants.PHASED_BACKOFF;
    }

    public DisruptorConfig(String bufferSize, String noDisruptors, String noOfEventHandlersPerDisruptor,
                           String disruptorWaitStrategy, boolean notShared) {
        this.bufferSize = Integer.parseInt(bufferSize);
        this.noDisruptors = Integer.parseInt(noDisruptors);
        this.noOfEventHandlersPerDisruptor = Integer.parseInt(noOfEventHandlersPerDisruptor);
        this.disruptorWaitStrategy = disruptorWaitStrategy;
        this.notShared = notShared;

    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getNoDisruptors() {
        return noDisruptors;
    }

    public int getNoOfEventHandlersPerDisruptor() {
        return noOfEventHandlersPerDisruptor;
    }

    public String getDisruptorWaitStrategy() {
        return disruptorWaitStrategy;
    }

    public boolean isShared() {
        return !notShared;
    }

    public RingBuffer getDisruptor() {
        int ind = index.getAndIncrement() % noDisruptors;
            return disruptorMap.get(ind);
    }

    public void addDisruptor(RingBuffer ringBuffer) {
        disruptorMap.add(ringBuffer);
    }

    public void notifyChannelInactive() {
        index.getAndDecrement();
    }

}
