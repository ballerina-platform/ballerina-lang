/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.utils.debug;

import io.netty.channel.Channel;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugClientHandler;
import org.ballerinalang.util.debugger.DebugException;
import org.ballerinalang.util.debugger.dto.MessageDTO;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Test debug client handler for debugger tests.
 *
 * @since 0.95.4
 */
public class TestDebugClientHandler implements DebugClientHandler {

    private volatile boolean isExit;
    private volatile Semaphore executionSem;

    private Queue<MessageDTO> debugHits;

    //key - workerId
    private Map<String, WorkerExecutionContext> contextMap;

    TestDebugClientHandler() {
        this.debugHits = new LinkedBlockingQueue<>();
        this.contextMap = new ConcurrentHashMap<>();
        this.executionSem = new Semaphore(0);
    }

    public void aquireSem() {
        try {
            executionSem.acquire();
        } catch (InterruptedException e) {
            //ignore
        }
    }
    public boolean isExit() {
        return isExit && debugHits.isEmpty();
    }

    public MessageDTO getDebugHit() {
        return debugHits.poll();
    }

    @Override
    public void addWorkerContext(WorkerExecutionContext ctx) {
        String workerId = generateAndGetWorkerId(ctx.workerInfo);
        ctx.getDebugContext().setWorkerId(workerId);
        //TODO check if that thread id already exist in the map
        this.contextMap.put(workerId, ctx);
    }

    private String generateAndGetWorkerId (WorkerInfo workerInfo) {
        UUID uuid = UUID.randomUUID();
        return workerInfo.getWorkerName() + "-" + uuid.toString();
    }

    @Override
    public WorkerExecutionContext getWorkerContext(String workerId) {
        return this.contextMap.get(workerId);
    }

    @Override
    public Map<String, WorkerExecutionContext> getAllWorkerContexts() {
        return contextMap;
    }

    @Override
    public void setChannel(Channel channel) throws DebugException {
    }

    @Override
    public void clearChannel() {
    }

    @Override
    public boolean isChannelActive() {
        return true;
    }

    @Override
    public void notifyExit() {
        isExit = true;
        executionSem.release();
    }

    @Override
    public void notifyHalt(MessageDTO message) {
        debugHits.offer(message);
        executionSem.release();
    }

    @Override
    public void sendCustomMsg(MessageDTO message) {
    }
}

