/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.memoryusagemonitor;

import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.MemoryUsageMonitor;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Tests {@link MemoryUsageMonitor}.
 *
 * @since 2201.9.0
 */
public class MemoryUsageMonitorTest extends AbstractLSTest {
    private ExtendedLanguageClient mockClient;
    private MemoryMXBean mockMemoryMXBean;
    private BallerinaLanguageServer languageServer;
    private Endpoint serviceEndpoint;

    @BeforeClass
    @Override
    public void init() throws Exception {
        MemoryUsageMonitor memoryUsageMonitor = new MemoryUsageMonitor(createMockMemoryMXBean());
        this.languageServer = new BallerinaLanguageServer();
        languageServer.getServerContext().put(MemoryUsageMonitor.MEMORY_USAGE_MONITOR_KEY, memoryUsageMonitor);
        mockClient = Mockito.mock(ExtendedLanguageClient.class);
    }

    @Test
    public void test() throws WorkspaceDocumentException, IOException, InterruptedException {
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer()
                .withLanguageServer(languageServer)
                .withClient(mockClient)
                .withInitOption(InitializationOptions.KEY_ENABLE_MEMORY_USAGE_MONITOR, true);
        this.serviceEndpoint = builder.build();

        Thread.sleep(2000);

        Mockito.verify(mockClient).showMessage(new MessageParams(MessageType.Error,
                "Memory usage is high. Some features may become unresponsive. " +
                        "Please reload the window or increase the memory allocated for Ballerina")
        );
    }

    private MemoryMXBean createMockMemoryMXBean() {
        mockMemoryMXBean = Mockito.mock(MemoryMXBean.class, Mockito.withSettings().stubOnly());
        MemoryUsage mockHeapMemoryUsage = new MemoryUsage(1_000_000_000, 900_000_000, 900_000_000, 1_000_000_000);
        Mockito.when(mockMemoryMXBean.getHeapMemoryUsage()).thenReturn(mockHeapMemoryUsage);
        return mockMemoryMXBean;
    }

    @AfterClass
    @Override
    public void cleanMocks() {
        super.cleanMocks();
        if (this.mockClient != null) {
            Mockito.reset(this.mockClient);
            this.mockClient = null;
        }
        if (this.mockMemoryMXBean != null) {
            Mockito.reset(this.mockMemoryMXBean);
            this.mockMemoryMXBean = null;
        }
    }

    @AfterClass
    @Override
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
        this.languageServer = null;
        this.serviceEndpoint = null;
    }
}
