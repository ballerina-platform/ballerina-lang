/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.runtime.worker;

import org.ballerinalang.model.types.BType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This is the main class which used as the data channel for worker to worker data communication.
 */
public class WorkerDataChannel {

    private String source;
    private String target;
    private BlockingQueue<Object[]> channel;
    private BType[] types;
    private static final Logger log = LoggerFactory.getLogger(WorkerDataChannel.class);

    public WorkerDataChannel(String source, String target) {
        this.source = source;
        this.target = target;
        this.channel =  new LinkedBlockingQueue<>();
    }

    public void putData(Object[] data) {
        try {
            if (data != null) {
                channel.put(data);
            }
        } catch (InterruptedException e) {
            // Handle the error properly
            log.error("Error occurred when inserting data to the channel");
        }
    }

    public Object[] takeData() {
        Object[] data = null;
        try {
            data = channel.poll(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Handle the error properly
            log.error("Error occurred when taking data from the channel");
        }
        return data;
    }

    public String getChannelName() {
        return source + "->" + target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public BType[] getTypes() {
        return types;
    }

    public void setTypes(BType[] types) {
        this.types = types;
    }
}
