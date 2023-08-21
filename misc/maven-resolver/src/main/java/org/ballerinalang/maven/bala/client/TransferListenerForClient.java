/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com).
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
package org.ballerinalang.maven.bala.client;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Progress bar implementation for Maven Resolver.
 */
public class TransferListenerForClient extends AbstractTransferListener {
    private static final String BALA_EXTENSION = "bala";
    private PrintStream out;
    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<>();
    private Map<String, ProgressBar> progressBars = new ConcurrentHashMap<>();
    private Map<String, Long> progresses = new ConcurrentHashMap<>();

    public TransferListenerForClient() {
        this(System.out);
    }

    public TransferListenerForClient(PrintStream out) {
        this.out = out;
    }

    @Override
    public void transferProgressed(TransferEvent event) {
        TransferResource resource = event.getResource();
        if (resource.getFile().getName().contains(BALA_EXTENSION)) {
            downloads.put(resource, event.getTransferredBytes());
            for (Map.Entry<TransferResource, Long> entry : downloads.entrySet()) {
                String currentResource = entry.getKey().getResourceName();
                // Each transitive dependency download be displayed in own progress bars
                if (progressBars.get(currentResource) == null) {
                    String[] files = currentResource.split("/");
                    progressBars.put(currentResource, new ProgressBar(files[files.length - 1],
                            getKB(entry.getKey().getContentLength()), 1000, out, ProgressBarStyle.ASCII, " KB", 1));
                }
                long transferredLength = getKB(event.getTransferredBytes());
                Long previousStep = progresses.get(currentResource) == null ? 0L : progresses.get(currentResource);
                progressBars.get(currentResource).stepBy(transferredLength - previousStep);
                progresses.put(currentResource, transferredLength);
            }
        }
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        TransferResource resource = event.getResource();
        if (resource.getFile().getName().contains(BALA_EXTENSION)) {
            progressBars.get(resource.getResourceName()).close();
        }
    }

    private long getKB(long bytes) {
        return (bytes + 1023) / 1024;
    }
}
