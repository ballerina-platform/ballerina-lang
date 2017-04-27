/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.siddhi.extension.input.transport.jms;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.extension.input.transport.jms.executor.PausableThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This processes the JMS messages using a pausable thread pool.
 */
public class JMSMessageProcessor implements CarbonMessageProcessor {
    private SourceEventListener sourceEventListener;
    private PausableThreadPoolExecutor executor;
    private LinkedBlockingQueue<Runnable> queue;
    // this is the maximum time that excess idle threads will wait for new tasks before terminating.
    // since the threads will exit after each execution, this is set to a minimal value
    private final long KEEP_ALIVE_TIME = 10;
    private final int MAX_THREAD_POOL_SIZE_MULTIPLIER = 2;

    public JMSMessageProcessor(SourceEventListener sourceEventListener, int coreThreadPoolSize) {
        this.sourceEventListener = sourceEventListener;
        this.queue = new LinkedBlockingQueue<>();
        int maxThreadPoolSize = MAX_THREAD_POOL_SIZE_MULTIPLIER * coreThreadPoolSize;
        this.executor = new PausableThreadPoolExecutor(coreThreadPoolSize, maxThreadPoolSize, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, queue);
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        executor.execute(new JMSWorkerThread(carbonMessage, carbonCallback, sourceEventListener));
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
    }

    @Override
    public String getId() {
        return "JMS-message-processor";
    }

    void pause() {
        executor.pause();
    }

    void resume() {
        executor.resume();
    }

    public void clear() {
        queue.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    void disconnect() {
        executor.shutdown();
    }
}
