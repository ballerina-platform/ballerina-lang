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

package org.ballerinalang.siddhi.core.query.selector.attribute.aggregator;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for distinct attribute aggregator.
 */
public class DistinctCountAttributeAggregatorTestCase {
    private static final Logger log = LoggerFactory.getLogger(DistinctCountAttributeAggregatorTestCase.class);
    private volatile int count;

    @BeforeMethod
    public void init() {
        count = 0;
    }

    @Test
    public void distinctCountTest() throws InterruptedException {
        log.info("Distinct Count TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (eventId string, userID string, pageID string); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from inputStream#window.time(5 sec) " +
                "select userID, pageID, distinctCount(pageID) as distinctPages " +
                "group by userID " +
                "having distinctPages > 3 " +
                "insert into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(org.ballerinalang.siddhi.core.event.Event[] events) {
                for (org.ballerinalang.siddhi.core.event.Event event : events) {
                    AssertJUnit.assertEquals("User ID", "USER_1", event.getData(0));
                    AssertJUnit.assertEquals("Page ID", "WEB_PAGE_4", event.getData(1));
                    AssertJUnit.assertEquals("Distinct Pages", 4L, event.getData(2));
                    count++;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"E001", "USER_1", "WEB_PAGE_1"});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E002", "USER_2", "WEB_PAGE_1"});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E003", "USER_1", "WEB_PAGE_2"}); // 1st Event in window
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E004", "USER_2", "WEB_PAGE_2"});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E005", "USER_1", "WEB_PAGE_3"}); // 2nd Event in window
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E006", "USER_1", "WEB_PAGE_1"}); // 3rd Event in window
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E007", "USER_1", "WEB_PAGE_4"}); // 4th Event in window
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E008", "USER_2", "WEB_PAGE_2"});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E009", "USER_1", "WEB_PAGE_1"});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"E010", "USER_2", "WEB_PAGE_1"});

        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals("Event count", 1, count);
    }
}
