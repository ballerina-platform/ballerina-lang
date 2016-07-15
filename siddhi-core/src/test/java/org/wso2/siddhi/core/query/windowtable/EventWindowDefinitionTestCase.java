/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.query.windowtable;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import static org.junit.Assert.assertTrue;

public class EventWindowDefinitionTestCase {
    private static final Logger log = Logger.getLogger(EventWindowDefinitionTestCase.class);

    @Test
    public void testEventWindow1() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1); ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testEventWindow2() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output all events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testEventWindow3() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output expired events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testEventWindow4() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output current events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testEventWindow5() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanCreationException.class)
    public void testEventWindow6() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string, val int) sum(val); ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testEventWindow7() throws InterruptedException {
        log.info("EventWindowDefinitionTestCase Test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) output; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        executionPlanRuntime.shutdown();
    }
}
