/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.serverconnector.framework.polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * The {@link Runnable} which executes a periodic poll.
 */
public class PollingTaskRunner implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PollingTaskRunner.class);

    private volatile boolean execute = true;

    private final long interval;
    private final PollingServerConnector connector;

    public PollingTaskRunner(PollingServerConnector connector) {
        this.connector = connector;
        this.interval = connector.getInterval();
    }

    public void start() {
        Thread runningThread = new Thread(this);
        runningThread.start();
    }

    @Override
    public void run() {
        log.debug("Starting the polling task for server connector ID: " + connector.getId());

        // Run the poll cycles
        while (execute) {
            log.debug("Executing the polling task for server connector ID: " + connector.getId());
            long lastInvokedTime = getTime();
            try {
                connector.poll();
            } catch (Exception e) {
                log.error("Error executing the polling cycle for " +
                        "server connector ID: " + connector.getId(), e);
            }
            long currentTime = getTime();
            long cycleInterval = interval - (currentTime - lastInvokedTime);
            if (cycleInterval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to sleep the polling task for interval of : "
                                + interval + "ms. Server connector ID: " + connector.getId());
                    }
                }
            }
        }
        log.debug("Exit the polling task running loop for server connector ID: " + connector.getId());
    }

    /**
     * Exit the running while loop and terminate the thread
     */
    protected void terminate() {
        execute = false;
    }

    private Long getTime() {
        return new Date().getTime();
    }
}
