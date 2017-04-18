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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is the main class which used as the data channel for worker to worker data communication.
 */
public class WorkerDataChannel {

    private String source;
    private String target;
    private BlockingQueue<Object[]> channel = new LinkedBlockingQueue<>();

    public WorkerDataChannel(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public void putData(Object[] data) {
        try {
            channel.put(data);
        } catch (InterruptedException e) {
            // Handle the error properly
        }
    }

    public Object[] takeData() {
        Object[] data = null;
        try {
            data = channel.take();
        } catch (InterruptedException e) {
            // Handle the error properly
        }
        return data;
    }

    public String getChannelName() {
        return source + "->" + target;
    }
}
