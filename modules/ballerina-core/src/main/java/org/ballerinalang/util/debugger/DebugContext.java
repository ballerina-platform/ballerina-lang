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

import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.util.codegen.LineNumberInfo;

/**
 * {@link DebugContext} holds information relevant to current debugging session.
 *
 * @since 0.95.4
 */
public class DebugContext {
    private volatile DebugCommand currentCommand;

    private LineNumberInfo lastLine;
    private StackFrame stackFrame;

    private String threadId;

    private volatile boolean active = false;

    public DebugCommand getCurrentCommand() {
        return currentCommand;
    }

    public void setCurrentCommand(DebugCommand currentCommand) {
        this.currentCommand = currentCommand;
    }

    public LineNumberInfo getLastLine() {
        return lastLine;
    }

    public StackFrame getStackFrame() {
        return stackFrame;
    }

    public void setStackFrame(StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }

    public void setLastLine(LineNumberInfo lastLine) {
        this.lastLine = lastLine;
    }

    public void clearLastDebugLine() {
        this.lastLine = null;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public boolean isAtive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
