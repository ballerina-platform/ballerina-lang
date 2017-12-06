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
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code VMDebugSession} The Debug Session class will be used to hold context for each client.
 * Each client will get its own instance of debug session.
 *
 * @since 0.88
 */
public class VMDebugSession implements DebugSessionObserver {

    private Channel channel = null;

    private ArrayList<BreakPointDTO> breakPoints;

    //key - threadid
    private Map<String, DebugContext> contextMap;

    public VMDebugSession() {
        this.contextMap = new HashMap<>();
    }

    @Override
    public void addContext(DebugContext debugContext) {
        String threadId = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        debugContext.setThreadId(threadId);
        //TODO check if that thread id already exist in the map
        this.contextMap.put(threadId, debugContext);
    }

    public DebugContext getContext(String threadId) {
        return this.contextMap.get(threadId);
    }

    public void updateAllDebugContexts(DebugCommand debugCommand) {
        contextMap.forEach((k, v) -> v.setCurrentCommand(debugCommand));
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    public synchronized void setChannel(Channel channel) throws DebugException {
        if (this.channel != null) {
            throw new DebugException("Debug session already exist");
        }
        this.channel = channel;
    }

    /**
     * Method to clear the channel so that another debug session can connect.
     */
    public void clearSession() {
        this.channel.close();
        this.channel = null;
    }
}
