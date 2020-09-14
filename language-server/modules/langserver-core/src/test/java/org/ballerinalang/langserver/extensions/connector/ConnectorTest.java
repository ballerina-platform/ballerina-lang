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

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorResponse;
import org.ballerinalang.langserver.extensions.ballerina.connector.BallerinaConnectorServiceImpl;
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

    private Path connectorToml = FileUtils.RES_DIR.resolve("extensions")
            .resolve("connector")
            .resolve("connector.toml");

    @BeforeClass
    public void startLangServer() {
        System.setProperty(BallerinaConnectorServiceImpl.DEFAULT_CONNECTOR_FILE_KEY, connectorToml.toString());
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }
//
//    @Test(description = "Test getting all connectors.")
//    public void getConnectors() {
//        BallerinaConnectorsResponse connectorsResponse = LSExtensionTestUtil
//                .getConnectors(this.serviceEndpoint);
//        Assert.assertEquals(connectorsResponse.getConnectors().size(), 4);
//        Assert.assertEquals(connectorsResponse.getConnectors().get(0).getModule(), "nats");
//        Assert.assertEquals(connectorsResponse.getConnectors().get(0).getName(), "Producer");
//    }

    @Test(description = "Test getting HTTP connectors.")
    public void getHTTPConnector() {
        BallerinaConnectorResponse connectorsResponse = LSExtensionTestUtil
                .getConnector("ballerina", "http", "1.0.0", "Client",
                        "http:Client", true, this.serviceEndpoint);
        Assert.assertEquals(((JsonObject) ((JsonObject) connectorsResponse.getAst()).get("records")).entrySet().size(),
                16);
        Assert.assertEquals(((JsonObject) ((JsonObject) connectorsResponse.getAst()).get("name")).
                get("value").getAsString(), "Client");
    }

//    @Test(description = "Test getting twitter connectors.")
//    public void getTwitterConnector() {
//        BallerinaConnectorResponse connectorsResponse = LSExtensionTestUtil
//                .getConnector("wso2", "twitter", "0.9.26", "Client", this.serviceEndpoint);
//        Assert.assertEquals(((JsonObject) ((JsonObject) connectorsResponse.getAst()).get("name")).
//                get("value").getAsString(), "Client");
//    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
