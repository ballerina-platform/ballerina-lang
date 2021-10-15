/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.connector;

import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorListResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test for connector API.
 */
public class ConnectorTest {
    private Endpoint serviceEndpoint;
    private Path testConnectorFilePath = FileUtils.RES_DIR.resolve("extensions").resolve("connector")
            .resolve("TestConnector").resolve("main.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test getting all connectors.")
    public void getConnectors() {
        BallerinaConnectorListResponse connectorsResponse = LSExtensionTestUtil
                .getConnectors(testConnectorFilePath.toString(), "", this.serviceEndpoint);

        Assert.assertNotEquals(connectorsResponse.getCentralConnectors().size(), 0);
        Assert.assertNotEquals(connectorsResponse.getLocalConnectors().size(), 0);
    }

    // TODO: Need to add mock server.
    // @Test(description = "Test search twilio connectors.")
    // public void searchConnectors() {
    //     BallerinaConnectorListResponse connectorsResponse = LSExtensionTestUtil
    //             .getConnectors(testConnectorFilePath.toString(), "twilio", this.serviceEndpoint);

    //     Assert.assertNotEquals(connectorsResponse.getCentralConnectors().size(), 0);
    //     Assert.assertNotEquals(connectorsResponse.getLocalConnectors().size(), 0);
    // }

    // @Test(description = "Test fetch twilio connector metadata.")
    // public void getTwilioConnector() {
    //     BalConnector connector = LSExtensionTestUtil
    //             .getConnector("120", "ballerinax", "twilio", "2.0.0", "Client", this.serviceEndpoint);

    //     Assert.assertEquals(connector.id, "120");
    //     Assert.assertEquals(connector.moduleName, "twilio");
    // }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
