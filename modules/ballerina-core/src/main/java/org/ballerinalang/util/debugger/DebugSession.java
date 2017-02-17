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
import org.ballerinalang.bre.nonblocking.debugger.BLangExecutionDebugger;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.model.NodeLocation;

import java.util.ArrayList;


/**
 * The Debug Session class will be used to hold context for each client.
 * Each client will get its own instance of debug session.
 */
public class DebugSession implements DebugSessionObserver {

    private ArrayList<NodeLocation> breakPoints;

    private Channel channel = null;

    private BLangExecutionDebugger debugger;

    public DebugSession(Channel channel) {
        this.channel = channel;
    }

    public void setDebugger(BLangExecutionDebugger debugger) {
        this.debugger = debugger;
        debugger.setDebugSessionObserver(this);
        if (null != breakPoints) {
            for (NodeLocation point: breakPoints) {
                debugger.addDebugPoint(point);
            }
        }
    }

    public BLangExecutionDebugger getDebugger() {
        return debugger;
    }

    /**
     * Sets debug points.
     *
     * @param breakPoints the debug points
     */
    public void setBreakPoints(ArrayList<NodeLocation> breakPoints) {
        this.breakPoints = breakPoints;
        // if debugger is already set update the existing.
        if (null != debugger) {
            this.debugger.clearDebugPoints();
            if (null != breakPoints) {
                for (NodeLocation point: breakPoints) {
                    debugger.addDebugPoint(point);
                }
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
        DebugManager debugManager = DebugManager.getInstance();
        debugManager.notifyComplete(this);
    }

    @Override
    public void notifyExit() {
        DebugManager debugManager = DebugManager.getInstance();
        debugManager.notifyExit(this);
    }

    @Override
    public void notifyHalt(BreakPointInfo breakPointInfo) {
        DebugManager debugManager = DebugManager.getInstance();
        debugManager.notifyDebugHit(this, breakPointInfo);
    }
}
