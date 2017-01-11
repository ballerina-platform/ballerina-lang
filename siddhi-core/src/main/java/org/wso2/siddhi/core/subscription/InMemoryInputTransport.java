/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.subscription;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO: 12/23/16 Currently this class is only used for testing purposes.
public class InMemoryInputTransport extends InputTransport {

    private InputCallback inputCallback;
    private ScheduledExecutorService executorService;
    private DataGenerator dataGenerator = new DataGenerator();

    @Override
    public void init(Map<String, String> transportOptions, InputCallback inputCallback) {
        this.inputCallback = inputCallback;
        this.executorService = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder().setNameFormat
                ("Siddhi-inmemoryinputtransport-scheduler-thread-%d").build());
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        this.executorService.scheduleAtFixedRate(dataGenerator, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void disconnect() {
        this.executorService.shutdown();
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return false;
    }

    private class DataGenerator implements Runnable {

        @Override
        public void run() {
//            inputCallback.onEvent(new Object[]{"WSO2", 56.75f, 5});
//            inputCallback.onEvent("{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");
//            inputCallback.onEvent("WSO2,56.75,5");
//            inputCallback.onEvent("WSO2,56.75,5,Sri Lanka");
//            inputCallback.onEvent("symbol=WSO2, price=56.75, volume=5, country=Sri Lanka");
//            inputCallback.onEvent("<event><symbol>WSO2</symbol><price>56.75</price><volume>5</volume><country>Sri Lanka</country></event>");
            HashMap hashMap = new HashMap();
            hashMap.put("symbol", "WSO2");
            hashMap.put("price", 56.75);
            hashMap.put("volume", 5);
            hashMap.put("country", "Sri Lanka");
//            inputCallback.onEvent(hashMap);
            Event event = new Event();
            event.setData(new Object[]{"WSO2", 56.75f, 5});
//            inputCallback.onEvent(event);
        }
    }
}
