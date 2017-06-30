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

import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.util.codegen.LineNumberInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Semaphore;

/**
 * {@link DebugInfoHolder} holds information relevant to current debugging session.
 *
 * @since 0.88
 */
public class DebugInfoHolder {
    public static final int FUNCTION_CALL_STACK_INIT_SIZE = 10;

    private volatile Semaphore executionSem;
    private Map<String, NodeLocation> breakPoints;
    private DebugSessionObserver debugSessionObserver;
    private Stack<LineNumberInfo> currentLineStack;
    private DebugCommand currentCommand;
    private int previousIp = -1;
    private int nextIp = -1;
    private int[] functionCalls;
    private int functionCallPointer = -1;
    private boolean mainProgram = false;


    public DebugInfoHolder() {
        this.breakPoints = new HashMap<>();
        this.currentLineStack = new Stack<>();
        this.functionCalls = new int[FUNCTION_CALL_STACK_INIT_SIZE];
        this.executionSem = new Semaphore(0);
    }

    public void waitTillDebuggeeResponds() {
        try {
            executionSem.acquire();
        } catch (InterruptedException e) {
            //TODO error handle
        }
    }

    public void releaseLock() {
        executionSem.release();
    }

    public void addDebugPoint(NodeLocation nodeLocation) {
        breakPoints.put(nodeLocation.toString(), nodeLocation);
    }

    public void addDebugPoints(List<NodeLocation> nodeLocations) {
        for (NodeLocation nodeLocation : nodeLocations) {
            addDebugPoint(nodeLocation);
        }
    }

    public void clearDebugLocations() {
        breakPoints.clear();
    }

    public NodeLocation getDebugPoint(String key) {
        return breakPoints.get(key);
    }

    public void setDebugSessionObserver(DebugSessionObserver debugSessionObserver) {
        this.debugSessionObserver = debugSessionObserver;
    }

    public DebugSessionObserver getDebugSessionObserver() {
        return debugSessionObserver;
    }

    public Stack<LineNumberInfo> getCurrentLineStack() {
        return currentLineStack;
    }

    public DebugCommand getCurrentCommand() {
        return currentCommand;
    }

    public void setCurrentCommand(DebugCommand currentCommand) {
        this.currentCommand = currentCommand;
    }

    public int getPreviousIp() {
        return previousIp;
    }

    public void setPreviousIp(int previousIp) {
        this.previousIp = previousIp;
    }

    public int getNextIp() {
        return nextIp;
    }

    public void setNextIp(int nextIp) {
        this.nextIp = nextIp;
    }

    public void pushFunctionCallNextIp(int nextIp) {
        if (functionCallPointer >= functionCalls.length) {
            functionCalls = Arrays.copyOf(functionCalls,
                    functionCalls.length + FUNCTION_CALL_STACK_INIT_SIZE);
        }
        functionCalls[++functionCallPointer] = nextIp;
    }

    public int popFunctionCallNextIp() {
        int nextIp = -1;
        if (functionCallPointer > 0) {
            nextIp = functionCalls[functionCallPointer];
            functionCalls[functionCallPointer] = -1;
        }
        functionCallPointer--;
        return nextIp;
    }

    public int peekFunctionCallNextIp() {
        int nextIp = -1;
        if (functionCallPointer >= 0) {
            nextIp = functionCalls[functionCallPointer];
        }
        return nextIp;
    }

    public boolean isMainProgram() {
        return mainProgram;
    }

    public void setMainProgram(boolean mainProgram) {
        this.mainProgram = mainProgram;
    }

    public void resume() {
        currentCommand = DebugInfoHolder.DebugCommand.RESUME;
        releaseLock();
    }

    public void stepIn() {
        currentCommand = DebugInfoHolder.DebugCommand.STEP_IN;
        releaseLock();
    }

    public void stepOver() {
        currentCommand = DebugInfoHolder.DebugCommand.STEP_OVER;
        releaseLock();
    }

    public void stepOut() {
        currentCommand = DebugInfoHolder.DebugCommand.STEP_OUT;
        releaseLock();
    }

    /**
     * Debugging steps.
     */
    public enum DebugCommand {
        STEP_IN,
        STEP_OVER,
        STEP_OUT,
        RESUME,
        NEXT_LINE
    }
}
