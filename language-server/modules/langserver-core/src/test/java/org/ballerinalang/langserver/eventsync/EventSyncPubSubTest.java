/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.eventsync;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Tests event sync publisher subscriber in Language Server.
 *
 * @since 2201.1.1
 */
public class EventSyncPubSubTest {

    private Endpoint serviceEndpoint;
    private final Path testRoot = FileUtils.RES_DIR.resolve("eventsync");

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "eventsync-data-provider")
    public void testCompletion(String config) throws IOException {
        String configJsonPath = getConfigJsonPath(config);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        await().atMost(2, TimeUnit.SECONDS).until(this::gotEvent);
        Assert.assertTrue(gotEvent(), "EventSyncPubSub Test Failed. Subscriber did not get notified");
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "eventsync-data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"event_sync_config.json"},
        };
    }

    public String getConfigJsonPath(String configFilePath) {
        return "eventsync" + File.separator + "config" + File.separator + configFilePath;
    }

    private boolean gotEvent() {
        return EventSyncTestSubscriber.gotEvent;
    }
}
