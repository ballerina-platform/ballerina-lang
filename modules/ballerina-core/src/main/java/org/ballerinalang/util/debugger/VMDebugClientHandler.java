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
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.ballerinalang.util.debugger.info.BreakPointInfo;
import org.ballerinalang.util.debugger.util.DebugMsgUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code VMDebugSession} The Debug Session class will be used to hold context for each client.
 * Each client will get its own instance of debug session.
 *
 * @since 0.88
 */
public class VMDebugClientHandler implements DebugClientHandler {

    private Channel channel = null;

    //key - threadid
    private Map<String, DebugContext> contextMap;

    public VMDebugClientHandler() {
        this.contextMap = new HashMap<>();
    }

    @Override
    public void addContext(DebugContext debugContext) {
        String threadId = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        debugContext.setThreadId(threadId);
        //TODO check if that thread id already exist in the map
        this.contextMap.put(threadId, debugContext);
    }

    @Override
    public DebugContext getContext(String threadId) {
        return this.contextMap.get(threadId);
    }

    @Override
    public void updateAllDebugContexts(DebugCommand debugCommand) {
        contextMap.forEach((k, v) -> v.setCurrentCommand(debugCommand));
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
    public void notifyComplete() {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_COMPLETE);
        message.setMessage(DebugConstants.MSG_COMPLETE);
        pushMessageToClient(message);
    }

    @Override
    public void notifyExit() {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_EXIT);
        message.setMessage(DebugConstants.MSG_EXIT);
        pushMessageToClient(message);
    }

    @Override
    public void notifyHalt(BreakPointInfo breakPointInfo) {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_HIT);
        message.setMessage(DebugConstants.MSG_HIT);
        message.setThreadId(breakPointInfo.getThreadId());
        message.setLocation(breakPointInfo.getHaltLocation());
        message.setFrames(breakPointInfo.getCurrentFrames());
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
