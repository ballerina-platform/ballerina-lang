/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.jms;

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Includes common functionality for jms test cases.
 */
public class BaseTest {

    private EmbeddedActiveMQ embeddedBroker;

    @BeforeGroups(value = "jms-test", alwaysRun = true)
    public void start() throws Exception {
        Path path = Paths.get("src", "test", "resources");

        // Start broker
        embeddedBroker = new EmbeddedActiveMQ();
        String brokerXML = path.resolve("configfiles").resolve("broker.xml").toUri().toString();
        embeddedBroker.setConfigResourcePath(brokerXML);
        embeddedBroker.start();
    }

    @AfterGroups(value = "jms-test", alwaysRun = true)
    public void stop() {
    }

}
