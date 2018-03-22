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

package org.ballerinalang.siddhi.core.managment;

import org.ballerinalang.siddhi.core.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EventTestCase {
    private static final Logger log = LoggerFactory.getLogger(EventTestCase.class);
    private int count;
    private boolean eventArrived;
    private long firstValue;
    private long lastValue;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
        firstValue = 0;
        lastValue = 0;
    }

    @Test
    public void eventTest1() throws InterruptedException {
        log.info("event test 1 - equal");

        Event event1 = new Event(123, new Object[]{23, 234.5, 3f, 234L, "adfsad", true});
        Event event2 = new Event(123, new Object[]{23, 234.5, 3f, 234L, "adfsad", true});

        AssertJUnit.assertEquals(event1, event2);
        AssertJUnit.assertTrue(event1.equals(event2));

    }

    @Test
    public void eventTest2() throws InterruptedException {
        log.info("event test 2 - not equal");

        Event event1 = new Event(1223, new Object[]{23, 234.5, 3f, 234L, "adfsad", true});
        Event event2 = new Event(123, new Object[]{23, 234.5, 3f, 234L, "adfsad", true});

        AssertJUnit.assertTrue(!event1.equals(event2));

    }

    @Test
    public void eventTest3() throws InterruptedException {
        log.info("event test 3 - not equal");

        Event event1 = new Event(123, new Object[]{23, 2343.5, 3f, 234L, "adfsad", true});
        Event event2 = new Event(123, new Object[]{23, 234.5, 3f, 234L, "adfsad", true});

        AssertJUnit.assertTrue(!event1.equals(event2));


    }


}
