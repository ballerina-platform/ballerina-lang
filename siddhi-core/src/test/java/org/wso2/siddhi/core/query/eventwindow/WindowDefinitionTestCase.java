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

package org.wso2.siddhi.core.query.eventwindow;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import static org.junit.Assert.assertTrue;

public class WindowDefinitionTestCase {
    private static final Logger log = Logger.getLogger(WindowDefinitionTestCase.class);

    @Test
    public void testEventWindow1() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1); ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testEventWindow2() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output all events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testEventWindow3() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output expired events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testEventWindow4() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output current events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        assertTrue(true);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testEventWindow5() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanCreationException.class)
    public void testEventWindow6() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string, val int) sum(val); ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testEventWindow7() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) output; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testEventWindow8() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String query = "define stream InStream (meta_tenantId int, contextId string, eventId string, eventType string, authenticationSuccess bool, username string, localUsername string, userStoreDomain string, tenantDomain string, remoteIp string, region string, inboundAuthType string, serviceProvider string, rememberMeEnabled bool, forceAuthEnabled bool, passiveAuthEnabled bool, rolesCommaSeparated string, authenticationStep string, identityProvider string, authStepSuccess bool, stepAuthenticator string, isFirstLogin bool, identityProviderType string, _timestamp long);\n" +
                "define window countWindow (meta_tenantId int, batchEndTime long, timestamp long) externalTimeBatch(batchEndTime, 1 sec, 0, 10 sec, true);\n" +
                "from InStream\n" +
                "select meta_tenantId, eventId\n" +
                "insert into countStream;\n" +
                "from countStream\n" +
                "select meta_tenantId, eventId\n" +
                "insert into countWindow;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(query);
        executionPlanRuntime.shutdown();
    }
}
