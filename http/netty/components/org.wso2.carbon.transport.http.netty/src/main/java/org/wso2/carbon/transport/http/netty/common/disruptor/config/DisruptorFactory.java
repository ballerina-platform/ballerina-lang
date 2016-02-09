/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.common.disruptor.config;


import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;
import org.wso2.carbon.transport.http.netty.common.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.transport.http.netty.common.disruptor.exception.GenericExceptionHandler;
import org.wso2.carbon.transport.http.netty.common.disruptor.handler.CarbonDisruptorEventHandler;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Util Class creates Disruptors for Inbound and Outbound Transports.
 */
public class DisruptorFactory {

    private static ConcurrentHashMap<DisruptorType, DisruptorConfig> disruptorConfigHashMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static void createDisruptors(DisruptorType type, DisruptorConfig disruptorConfig) {
        WaitStrategy inboundWaitStrategy = getWaitStrategy(disruptorConfig.getDisruptorWaitStrategy());
        for (int i = 0; i < disruptorConfig.getNoDisruptors(); i++) {
            ExecutorService executorService =
                    Executors.newFixedThreadPool(disruptorConfig.getNoOfEventHandlersPerDisruptor());
            Disruptor disruptor = new Disruptor<>(CarbonDisruptorEvent.EVENT_FACTORY, disruptorConfig.getBufferSize(),
                    executorService,
                    ProducerType.MULTI,
                    inboundWaitStrategy);
            ExceptionHandler exh = new GenericExceptionHandler();
            EventHandler[] eventHandlers = new EventHandler[disruptorConfig.getNoOfEventHandlersPerDisruptor()];
            for (int j = 0; j < disruptorConfig.getNoOfEventHandlersPerDisruptor(); j++) {
                EventHandler eventHandler = new CarbonDisruptorEventHandler();
                eventHandlers[j] = eventHandler;
            }
            disruptor.handleEventsWith(eventHandlers);
            for (EventHandler eventHandler : eventHandlers) {
                disruptor.handleExceptionsFor(eventHandler).with(exh);
            }
            disruptorConfig.addDisruptor(disruptor.start());
        }
        disruptorConfigHashMap.put(type, disruptorConfig);
    }


    private static WaitStrategy getWaitStrategy(String waitstrategy) {
        WaitStrategy waitStrategy;
        switch (waitstrategy) {
            case TransportConstants.BLOCKING_WAIT:
                waitStrategy = new BlockingWaitStrategy();
                break;
            case TransportConstants.BUSY_SPIN:
                waitStrategy = new BusySpinWaitStrategy();
                break;
            case TransportConstants.LITE_BLOCKING:
                waitStrategy = new LiteBlockingWaitStrategy();
                break;
            case TransportConstants.SLEEP_WAITING:
                waitStrategy = new SleepingWaitStrategy();
                break;
            case TransportConstants.TIME_BLOCKING:
                waitStrategy = new TimeoutBlockingWaitStrategy(1, TimeUnit.SECONDS);
                break;
            default:
                waitStrategy = PhasedBackoffWaitStrategy.withLiteLock(1, 4, TimeUnit.SECONDS);

        }
        return waitStrategy;
    }


    public static DisruptorConfig getDisruptorConfig(DisruptorType disruptorType) {
        return disruptorConfigHashMap.get(disruptorType);
    }

    /**
     * Describe types of disruptors.
     */
    public enum DisruptorType {
        INBOUND, OUTBOUND
    }

}
