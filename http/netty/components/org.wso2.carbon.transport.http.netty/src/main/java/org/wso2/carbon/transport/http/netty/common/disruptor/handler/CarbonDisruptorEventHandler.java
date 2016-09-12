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

package org.wso2.carbon.transport.http.netty.common.disruptor.handler;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.transport.http.netty.common.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;

import java.util.concurrent.locks.Lock;

/**
 * Event Consumer of the Disruptor.
 */
public class CarbonDisruptorEventHandler extends DisruptorEventHandler {

    public CarbonDisruptorEventHandler() {
    }

    @Override
    public void onEvent(CarbonDisruptorEvent carbonDisruptorEvent, long l, boolean b) throws Exception {
        CarbonMessage carbonMessage = (CarbonMessage) carbonDisruptorEvent.getEvent();
        Lock lock = carbonMessage.getLock();
        if (carbonMessage.getProperty(Constants.DIRECTION) == null) {
            // Mechanism to process each event from only one event handler
            if (lock.tryLock()) {
                CarbonCallback carbonCallback = (CarbonCallback) carbonMessage.getProperty(Constants.CALL_BACK);
                NettyTransportContextHolder.getInstance().getMessageProcessor(
                        carbonMessage.getProperty(org.wso2.carbon.transport.http.netty.common.Constants.CHANNEL_ID)
                                     .toString()).receive(carbonMessage, carbonCallback);
                // lock.unlock() does not used because if there are multiple event handlers and same event
                // should not processed by multiple event handlers .If  unlock happens too early for a event before
                // other Event handler object reads that event then there will be a probability of executing
                // same event by multiple event handlers.
            }
        } else if (carbonMessage.getProperty(Constants.DIRECTION).equals(Constants.DIRECTION_RESPONSE)) {
            if (lock.tryLock()) {
                CarbonCallback carbonCallback = (CarbonCallback) carbonMessage.getProperty(Constants.CALL_BACK);
                carbonCallback.done(carbonMessage);
            }
        }
    }

    @Override
    public void onEvent(Object o) throws Exception {
        if (o instanceof CarbonDisruptorEvent) {
            CarbonMessage carbonMessage = (CarbonMessage) ((CarbonDisruptorEvent) o).getEvent();
            if (carbonMessage.getProperty(Constants.DIRECTION) == null) {

                CarbonCallback carbonCallback = (CarbonCallback) carbonMessage.getProperty(Constants.CALL_BACK);
                NettyTransportContextHolder.getInstance().getMessageProcessor(
                        carbonMessage.getProperty(org.wso2.carbon.transport.http.netty.common.Constants.CHANNEL_ID)
                                     .toString()).receive(carbonMessage, carbonCallback);


            } else if (carbonMessage.getProperty(Constants.DIRECTION).equals(Constants.DIRECTION_RESPONSE)) {

                CarbonCallback carbonCallback = (CarbonCallback) carbonMessage.getProperty(Constants.CALL_BACK);
                carbonCallback.done(carbonMessage);
            }
        }
    }
}

