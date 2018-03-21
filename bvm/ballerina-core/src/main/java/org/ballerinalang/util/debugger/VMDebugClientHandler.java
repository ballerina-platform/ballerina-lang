/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.ballerinalang.util.debugger;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.ballerinalang.util.debugger.util.DebugMsgUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code VMDebugSession} The Debug Session class will be used to hold context for each client.
 * Each client will get its own instance of debug session.
 *
 * @since 0.88
 */
public class VMDebugClientHandler implements DebugClientHandler {

    private Channel channel = null;

    //key - workerId, value - worker context
    private Map<String, WorkerExecutionContext> contextMap;

    VMDebugClientHandler() {
        //Using concurrent map because multiple threads may try to access this concurrently
        this.contextMap = new ConcurrentHashMap<>();
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

    public Map<String, WorkerExecutionContext> getAllWorkerContexts() {
        return contextMap;
    }

    @Override
    public synchronized void setChannel(Channel channel) throws DebugException {
        if (this.channel != null) {
            throw new DebugException("Debug session already exist");
        }
        this.channel = channel;
    }

    @Override
    public void clearChannel() {
        this.channel.close();
        this.channel = null;
    }

    @Override
    public boolean isChannelActive() {
        return channel != null;
    }

    @Override
    public void notifyExit() {
        MessageDTO message = new MessageDTO(DebugConstants.CODE_EXIT, DebugConstants.MSG_EXIT);
        pushMessageToClient(message);
    }

    @Override
    public void notifyHalt(MessageDTO message) {
        pushMessageToClient(message);
    }

    @Override
    public void sendCustomMsg(MessageDTO message) {
        pushMessageToClient(message);
    }

    /**
     * Push message to client.
     *
     * @param msg debug point information
     */
    private void pushMessageToClient(MessageDTO msg) {
        String json = DebugMsgUtil.getMsgString(msg);
        channel.write(new TextWebSocketFrame(json));
        channel.flush();
    }
}
