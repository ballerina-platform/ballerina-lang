/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.maven;

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
public class TransferListener extends AbstractTransferListener {
    private PrintStream out;
    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<>();
    private long previousStep = 0;
    private ProgressBar progressBar;

    public TransferListener() {
        this(System.out);
    }

    public TransferListener(PrintStream out) {
        this.out = out;
    }

    @Override
    public void transferProgressed(TransferEvent event) {
        TransferResource resource = event.getResource();
        downloads.put(resource, event.getTransferredBytes());
        for (Map.Entry<TransferResource, Long> entry : downloads.entrySet()) {
            if (progressBar == null) {
                progressBar = new ProgressBar("Downloading ", getKB(entry.getKey().getContentLength()),
                        1000, out, ProgressBarStyle.ASCII, " KB", 1);
            }
        }
        long transferredLength = getKB(event.getTransferredBytes());
        progressBar.stepBy(transferredLength - previousStep);
        previousStep = transferredLength;
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        progressBar.close();
    }

    private long getKB(long bytes) {
        return (bytes + 1023) / 1024;
    }
}
