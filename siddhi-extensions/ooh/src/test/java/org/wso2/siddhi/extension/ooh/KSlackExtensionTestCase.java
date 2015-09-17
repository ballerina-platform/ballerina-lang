/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.extension.ooh;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

/**
 * This is the test case for KSlackExtension.
 * Created by miyurud on 8/10/15.
 */
public class KSlackExtensionTestCase {
    static final Logger log = Logger.getLogger(KSlackExtensionTestCase.class);

    @Before
    public void init() {
    }

    @Test
    public void OrderTest() throws InterruptedException {
        log.info("KSlackExtensionTestCase TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (eventtt long, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream#ooh:kslack(0) select eventtt, price, volume " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(org.wso2.siddhi.core.event.Event[] events) {
                for (org.wso2.siddhi.core.event.Event evt : events) {
                    Object[] dt = evt.getData();
                    log.info(dt[0]);
                }
            }
        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        //The following implements the out-of-order disorder handling scenario described in the
        //http://dl.acm.org/citation.cfm?doid=2675743.2771828
        inputHandler.send(new Object[]{1l, 700f, 100l});
        inputHandler.send(new Object[]{4l, 60.5f, 200l});
        inputHandler.send(new Object[]{3l, 60.5f, 200l});
        inputHandler.send(new Object[]{5l, 700f, 100l});
        inputHandler.send(new Object[]{6l, 60.5f, 200l});
        inputHandler.send(new Object[]{9l, 60.5f, 200l});
        inputHandler.send(new Object[]{7l, 700f, 100l});
        inputHandler.send(new Object[]{8l, 60.5f, 200l});
        inputHandler.send(new Object[]{10l, 60.5f, 200l});
        inputHandler.send(new Object[]{13l, 60.5f, 200l});

        Thread.sleep(1000);
        executionPlanRuntime.shutdown();
    }
}