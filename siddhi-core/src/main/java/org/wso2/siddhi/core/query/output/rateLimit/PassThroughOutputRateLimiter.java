/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.output.rateLimit;


import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.processor.Processor;

public class PassThroughOutputRateLimiter extends OutputRateLimiter {
    private static final Logger log = Logger.getLogger(PassThroughOutputRateLimiter.class);
    private String id;

    public PassThroughOutputRateLimiter(String id) {
        this.id = id;
    }

    public PassThroughOutputRateLimiter clone(String key) {
        return new PassThroughOutputRateLimiter(id + key);
    }


    @Override
    public void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent) {
        if (log.isTraceEnabled()) {
            log.trace("event is sent through outputRateLimiter "+ id+ this);
        }
        sendToCallBacks(timeStamp, currentEvent, expiredEvent, currentEvent != null ? currentEvent : expiredEvent);
    }

    @Override
    public void process(StreamEvent event) {
        //this method will not be used since no processing is done by rateLimiters
    }

    @Override
    public Processor getNextProcessor() {
        return null;
    }

    @Override
    public void setNextProcessor(Processor processor) {
        //this method will not be used as there is no processors after an outputRateLimiter
    }

    @Override
    public void setToLast(Processor processor) {
        //this method will not be used as there is no processors after an outputRateLimiter
    }

    @Override
    public Processor cloneProcessor() {
        return null;
    }
}
