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
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.transport.http.netty.common.disruptor.event.CarbonDisruptorEvent;

import java.util.concurrent.locks.Lock;

/**
 * Event Consumer of the Disruptor.
 */
public class CarbonDisruptorEventHandler extends DisruptorEventHandler {

    private CarbonMessageProcessor carbonMessageProcessor;

    public CarbonDisruptorEventHandler(CarbonMessageProcessor  engine) {
        carbonMessageProcessor = engine;
    }

    @Override
    public void onEvent(CarbonDisruptorEvent carbonDisruptorEvent, long l, boolean b) throws Exception {
        CarbonMessage carbonMessage = (CarbonMessage) carbonDisruptorEvent.getEvent();
        Lock lock = carbonMessage.getLock();
        if (carbonMessage.getDirection() == CarbonMessage.REQUEST) {
            // Mechanism to process each event from only one event handler
            if (lock.tryLock()) {
                CarbonCallback carbonCallback = carbonMessage.getCarbonCallback();
                carbonMessageProcessor.receive(carbonMessage, carbonCallback);
                // lock.unlock() does not used because if there are multiple event handlers and same event
                // should not processed by multiple event handlers .If  unlock happens too early for a event before
                // other Event handler object reads that event then there will be a probability of executing
                // same event by multiple event handlers.
            }
        } else if (carbonMessage.getDirection() == CarbonMessage.RESPONSE) {

            if (lock.tryLock()) {

                CarbonCallback carbonCallback = carbonMessage.getCarbonCallback();
                carbonCallback.done(carbonMessage);

            }


        }
    }
}

