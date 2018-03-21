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

import org.ballerinalang.util.codegen.LineNumberInfo;

/**
 * {@link DebugContext} holds information relevant to current debugging session.
 *
 * @since 0.95.4
 */
public class DebugContext {
    private volatile DebugCommand currentCommand;

    private LineNumberInfo lastLine;

    private String workerId;

    private boolean workerPaused;

    private boolean cmdChanged = false;

    public DebugContext() {
        this.currentCommand = DebugCommand.RESUME;
    }

    public DebugContext(DebugCommand command) {
        this.currentCommand = command;
    }

    public DebugCommand getCurrentCommand() {
        return currentCommand;
    }

    public void setCurrentCommand(DebugCommand currentCommand) {
        this.cmdChanged = true;
        this.currentCommand = currentCommand;
    }

    public LineNumberInfo getLastLine() {
        return lastLine;
    }

    public void setLastLine(LineNumberInfo lastLine) {
        this.lastLine = lastLine;
    }

    public void clearLastDebugLine() {
        this.lastLine = null;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public boolean isWorkerPaused() {
        return workerPaused;
    }

    public void setWorkerPaused(boolean workerPaused) {
        this.workerPaused = workerPaused;
    }

    public boolean isCmdChanged() {
        return cmdChanged;
    }
}
