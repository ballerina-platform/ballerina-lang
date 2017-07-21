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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
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
    private Map<String, Context> contextMap;

    public VMDebugSession() {
        this.contextMap = new HashMap<>();
    }

    @Override
    public void addContext(Context bContext) {
        String threadId = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        bContext.setThreadId(threadId);
        //TODO check if that thread id already exist in the map
        this.contextMap.put(threadId, bContext);
        setBreakPoints(bContext);
    }

    public Context getContext(String threadId) {
        return this.contextMap.get(threadId);
    }

    /**
     * Sets debug points.
     *
     * @param breakPoints the debug points
     */
    public void addDebugPoints(ArrayList<BreakPointDTO> breakPoints) {
        this.breakPoints = breakPoints;
        for (Context bContext : this.contextMap.values()) {
            setBreakPoints(bContext);
        }
    }

    /**
     * Helper method to set debug points to the given context.
     *
     * @param bContext
     */
    private void setBreakPoints(Context bContext) {
        if (null != breakPoints) {
            bContext.getDebugInfoHolder().addDebugPoints(breakPoints);
        }
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
     * Method to start debugging process in all the threads.
     */
    public void startDebug() {
        this.contextMap.values().stream().forEach(c -> c.getDebugInfoHolder().resume());
    }

    /**
     * Method to stop debugging process in all the threads.
     */
    public void stopDebug() {
        this.contextMap.values().stream().forEach(c -> {
            c.getDebugInfoHolder().clearDebugLocations();
            c.getDebugInfoHolder().resume();
        });
    }

    /**
     * Method to clear the channel so that another debug session can connect.
     */
    public void clearSession() {
        this.channel.close();
        this.channel = null;
    }

    @Override
    public void notifyComplete() {
        VMDebugManager debugManager = VMDebugManager.getInstance();
        debugManager.notifyComplete(this);
    }

    @Override
    public void notifyExit() {
        VMDebugManager debugManager = VMDebugManager.getInstance();
        debugManager.notifyExit(this);
    }

    @Override
    public void notifyHalt(BreakPointInfo breakPointInfo) {
        VMDebugManager debugManager = VMDebugManager.getInstance();
        debugManager.notifyDebugHit(this, breakPointInfo);
    }
}
