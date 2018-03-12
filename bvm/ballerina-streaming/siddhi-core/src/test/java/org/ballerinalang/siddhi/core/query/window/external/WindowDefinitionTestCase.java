/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.query.window.external;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.query.api.exception.DuplicateDefinitionException;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class WindowDefinitionTestCase {
    private static final Logger log = LoggerFactory.getLogger(WindowDefinitionTestCase.class);

    @Test
    public void testEventWindow1() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1); ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        assertTrue(true);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testEventWindow2() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output all events; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        assertTrue(true);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testEventWindow3() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output expired events; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        assertTrue(true);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testEventWindow4() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output current events; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        assertTrue(true);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiParserException.class)
    public void testEventWindow5() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) length(1) output; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testEventWindow6() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string, val int) sum(val); ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiParserException.class)
    public void testEventWindow7() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define window CheckStockWindow(symbol string) output; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = DuplicateDefinitionException.class)
    public void testEventWindow8() throws InterruptedException {
        log.info("WindowDefinitionTestCase Test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String query = "define stream InStream (meta_tenantId int, contextId string, eventId string, eventType " +
                "string, authenticationSuccess bool, username string, localUsername string, userStoreDomain string, " +
                "tenantDomain string, remoteIp string, region string, inboundAuthType string, serviceProvider string," +
                " rememberMeEnabled bool, forceAuthEnabled bool, passiveAuthEnabled bool, rolesCommaSeparated string," +
                " authenticationStep string, identityProvider string, authStepSuccess bool, stepAuthenticator string," +
                " isFirstLogin bool, identityProviderType string, _timestamp long);\n" +
                "define window countWindow (meta_tenantId int, batchEndTime long, timestamp long) externalTimeBatch" +
                "(batchEndTime, 1 sec, 0, 10 sec, true);\n" +
                "from InStream\n" +
                "select meta_tenantId, eventId\n" +
                "insert into countStream;\n" +
                "from countStream\n" +
                "select meta_tenantId, eventId\n" +
                "insert into countWindow;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(query);
        siddhiAppRuntime.shutdown();
    }
}
