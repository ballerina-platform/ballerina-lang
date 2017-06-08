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
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMDebugger;
import org.ballerinalang.bre.nonblocking.debugger.BLangExecutionDebugger;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.model.NodeLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * The Debug Session class will be used to hold context for each client.
 * Each client will get its own instance of debug session.
 */
public class VMDebugSession implements DebugSessionObserver {

    private Channel channel = null;

    //key - threadid
    private Map<String, BLangVMDebugger> debuggerMap;

    public VMDebugSession(Channel channel) {
        this.channel = channel;
        this.debuggerMap = new HashMap<>();
    }

    public void addDebugger(String threadId, BLangVMDebugger vmDebugger) {
        this.debuggerMap.put(threadId, vmDebugger);
    }

    public BLangVMDebugger getDebugger(String threadId) {
        return this.debuggerMap.get(threadId);
    }

    /**
     * Sets debug points.
     *
     * @param breakPoints the debug points
     */
    public void setBreakPoints(String threadId, ArrayList<NodeLocation> breakPoints) {
        BLangVMDebugger debugger = this.debuggerMap.get(threadId);
        if (null != breakPoints) {
            for (NodeLocation point : breakPoints) {
                debugger.addDebugPoint(point);
            }
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
