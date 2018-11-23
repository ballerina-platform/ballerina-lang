/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.vm;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.DebugContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Semaphore;

/**
 * This represents a Ballerina execution strand in the new VM.
 */
public class Strand {
    private static final int DEFAULT_CONTROL_STACK_SIZE = 2000;

    public String name;

    public volatile State state;

    private StackFrame[] callStack;

    // Stack frame pointer;
    public int fp = -1;

    public StackFrame currentFrame;

    public ProgramFile programFile;

    private DebugContext debugContext;

    public BVMCallback respCallback;

    private Semaphore executionLock;

    private BError error;

    public Map<String, Object> globalProps;

    public List<Integer> callbacksToWaitFor;

    public boolean waitCompleted;

    public int callBacksRemaining;

    public Strand(ProgramFile programFile, Map<String, Object> properties, StrandCallback respCallback) {
        this.programFile = programFile;
        this.respCallback = respCallback;
        this.callStack = new StackFrame[DEFAULT_CONTROL_STACK_SIZE];
        this.state = State.NEW;
        if (properties == null) {
            this.globalProps = new HashMap<>();
        } else {
            this.globalProps = properties;
        }
        this.callBacksRemaining = 0;
    }

    public StackFrame pushFrame(StackFrame frame) {
        callStack[++fp] = frame;
        currentFrame = frame;
        return currentFrame;
    }

    public StackFrame popFrame() {
        StackFrame poppedFrame = currentFrame;
        callStack[fp] = null;
        if (fp > 0) {
            currentFrame = callStack[--fp];
        } else {
            currentFrame = null;
            fp--;
        }
        return poppedFrame;
    }

    public StackFrame peekFrame(int offset) {
        StackFrame peekFrame = null;
        if (fp - offset >= 0 && fp - offset < callStack.length) {
            peekFrame = callStack[fp - offset];
        }
        return peekFrame;
    }

    public StackFrame getRootFrame() {
        return callStack[0];
    }

    public StackFrame getCurrentFrame() {
        return currentFrame;
    }

    public StackFrame[] getStack() {
        return callStack;
    }

    public void setError(BError error) {
        this.error = error;
    }

    public BError getError() {
        return error;
    }

    public void createLock() {
        this.executionLock = new Semaphore(1);
    }

    public void acquireExecutionLock() {
        try {
            executionLock.acquire();
        } catch (InterruptedException e) {
            /* ignore */
        }
    }

    public void releaseExecutionLock() {
        executionLock.release();
    }

    /**
     * Strand execution states.
     */
    public static enum State {
        NEW,
        RUNNABLE,
        PAUSED,
        TERMINATED
    }
}

