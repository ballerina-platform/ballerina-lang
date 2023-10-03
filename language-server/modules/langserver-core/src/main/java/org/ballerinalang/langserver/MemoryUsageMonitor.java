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
package org.ballerinalang.langserver;

import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Monitors the memory usage and notifies the user if the memory usage is high.
 *
 * @since 2201.9.0
 */
public class MemoryUsageMonitor {

    private final MemoryMXBean memoryMXBean;
    public MemoryUsageMonitor(MemoryMXBean memoryMXBean) {
        this.memoryMXBean = memoryMXBean;
    }

    public MemoryUsageMonitor() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    public static final LanguageServerContext.Key<MemoryUsageMonitor> MEMORY_USAGE_MONITOR_KEY =
            new LanguageServerContext.Key<>();
    public static MemoryUsageMonitor getInstance(LanguageServerContext context) {
        MemoryUsageMonitor memoryUsageMonitor = context.get(MEMORY_USAGE_MONITOR_KEY);
        if (memoryUsageMonitor == null) {
            memoryUsageMonitor = new MemoryUsageMonitor();
            context.put(MEMORY_USAGE_MONITOR_KEY, memoryUsageMonitor);
        }
        return memoryUsageMonitor;
    }

    public void start(LanguageClient client) {
        Thread usageMonitor = new Thread(() -> {
            while (true) {
                try {
                    MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                    if (heapMemoryUsage == null) {
                        return;
                    }
                    long usedMemory = heapMemoryUsage.getUsed();
                    long maxMemory = heapMemoryUsage.getMax();

                    if (usedMemory >= maxMemory * 0.9) {
                        client.showMessage(new MessageParams(MessageType.Error,
                                "Memory usage is high. Some features may become unresponsive. " +
                                        "Please reload the window or increase the memory allocated for Ballerina"));
                    }
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });
        usageMonitor.setDaemon(true);
        usageMonitor.start();
    }
}
