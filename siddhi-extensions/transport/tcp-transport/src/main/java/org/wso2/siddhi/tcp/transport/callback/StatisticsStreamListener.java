/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.siddhi.tcp.transport.callback;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class StatisticsStreamListener implements StreamListener {
    private static final Logger log = Logger.getLogger(StatisticsStreamListener.class);
    private AtomicLong totalDelay = new AtomicLong(0);
    private AtomicLong lastIndex = new AtomicLong(0);
    private AtomicLong lastCounter = new AtomicLong(0);
    private AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong maxLatency = new AtomicLong(0);
    private AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
    private AtomicLong counter = new AtomicLong(0);
    private AtomicBoolean calcInProgress = new AtomicBoolean(false);
    private DecimalFormat decimalFormat = new DecimalFormat("#.#####");
    private int elapsedCount = 100000;
    private PrintWriter writer = null;
    private StreamDefinition streamDefinition;

    public StatisticsStreamListener(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    @Override
    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    @Override
    public void onEvent(Event event) {
//        log.info(event);

        try {
            long currentBatchTotalDelay = 0;
            long currentTime = System.currentTimeMillis();
            long currentEventLatency = currentTime - event.getTimestamp();

            long currentMaxLatency = maxLatency.get();
            if (currentEventLatency > currentMaxLatency) {
                maxLatency.compareAndSet(currentMaxLatency, currentEventLatency);
            }
            long currentMinLatency = minLatency.get();
            if (currentEventLatency < currentMinLatency) {
                minLatency.compareAndSet(currentMinLatency, currentEventLatency);
            }
            currentBatchTotalDelay = currentBatchTotalDelay + currentEventLatency;

            long localCounter = counter.incrementAndGet();
            long localTotalDelay = totalDelay.addAndGet(currentBatchTotalDelay);

            long index = localCounter / elapsedCount;

            if (lastIndex.get() != index) {
                if (calcInProgress.compareAndSet(false, true)) {
                    lastIndex.set(index);
                    long currentWindowEventsReceived = localCounter - lastCounter.getAndSet(localCounter);
                    long elapsedTime = currentTime - lastTime.getAndSet(currentTime);
                    log.info("Received " + currentWindowEventsReceived + " events in " + elapsedTime + " ms; Throughput - Avg : "
                            + decimalFormat.format((currentWindowEventsReceived * 1000.0) / elapsedTime) + " ; Latency - Avg: "
                            + decimalFormat.format(localTotalDelay / (double) currentWindowEventsReceived)
                            + ", Min: " + minLatency.get() + ", Max: " + maxLatency.get());

                    writeResult(localCounter + ","
                            + decimalFormat.format(localTotalDelay / (double) currentWindowEventsReceived) + ","
                            + decimalFormat.format((currentWindowEventsReceived * 1000.0) / elapsedTime));

                    maxLatency.set(0);
                    minLatency.set(Long.MAX_VALUE);
                    totalDelay.addAndGet(-localTotalDelay);
                    calcInProgress.set(false);
                }
            }
        } catch (Exception e) {
            log.info("Error while consuming event on " + streamDefinition.getId() + ", " + e.getMessage());
        } finally {
            //channelHandlerContext.close();
        }
    }

    @Override
    public void onEvents(Event[] events) {
        for (Event event : events) {
            onEvent(event);
        }
    }

    private void writeResult(String data) {
        try {
            if (writer == null) {
                writer = new PrintWriter("results.csv");
            }
        } catch (FileNotFoundException ex) {
            log.error("File not found......");
        }

        writer.println(data);
        writer.flush();
    }


}
