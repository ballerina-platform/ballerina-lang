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
import org.ballerinalang.model.NodeLocation;

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

    private ArrayList<NodeLocation> breakPoints;

    //key - threadid
    private Map<String, Context> contextMap;

    public VMDebugSession(Channel channel) {
        this.channel = channel;
        this.contextMap = new HashMap<>();
    }

    public void addContext(String threadId, Context bContext) {
        this.contextMap.put(threadId, bContext);
        if (null != breakPoints) {
            for (NodeLocation point: breakPoints) {
                bContext.getDebugInfoHolder().addDebugPoint(point);
            }
        }
    }

    public Context getContext(String threadId) {
        return this.contextMap.get(threadId);
    }

    /**
     * Sets debug points.
     *
     * @param breakPoints the debug points
     */
    public void addDebugPoints(ArrayList<NodeLocation> breakPoints) {
        this.breakPoints = breakPoints;
        for (Context bContext : this.contextMap.values()) {
            for (NodeLocation point : breakPoints) {
                bContext.getDebugInfoHolder().addDebugPoint(point);
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
