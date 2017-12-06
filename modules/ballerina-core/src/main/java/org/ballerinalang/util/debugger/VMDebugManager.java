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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.dto.CommandDTO;
import org.ballerinalang.util.debugger.dto.MessageDTO;

import java.io.IOException;
import java.util.concurrent.Semaphore;


/**
 * {@code VMDebugManager} Manages debug sessions and handle debug related actions.
 *
 * @since 0.88
 */
public class VMDebugManager {

    private boolean debugEnabled = false;

    private VMDebugServer debugServer;

    private VMDebugSession debugSession;

    private DebugInfoHolder debugInfoHolder;

    private volatile Semaphore executionSem;

    private volatile Semaphore debugSem;

    public VMDebugManager() {
        String debug = System.getProperty(Constants.SYSTEM_PROP_BAL_DEBUG);
        if (debug != null && !debug.isEmpty()) {
            debugEnabled = true;
        }
    }

    /**
     * Method to check debug enabled or not.
     *
     * @return debugEnabled value.
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Method to initialize debug manager.
     *
     * @param programFile used to initialize debug manager.
     */
    public void init(ProgramFile programFile) {
        this.executionSem = new Semaphore(0);
        this.debugSem = new Semaphore(1);
        this.initDebugInfoHolder(programFile);
        this.debugSession = new VMDebugSession();
        this.debugServer = new VMDebugServer();
        this.debugServer.startServer(this);
    }

    private void initDebugInfoHolder(ProgramFile programFile) {
        if (this.debugInfoHolder != null) {
            return;
        }
        synchronized (this) {
            if (this.debugInfoHolder != null) {
                return;
            }
            this.debugInfoHolder = new DebugInfoHolder();
            this.debugInfoHolder.init(programFile);
        }
    }

    /**
     * Helper method to add debug context and wait until debugging starts.
     */
    public synchronized void addDebugContextAndWait() {
        this.debugSession.addContext(new DebugContext());
        this.waitTillDebuggeeResponds();
    }

    /**
     * Helper method to wait till debuggee responds.
     */
    public void waitTillDebuggeeResponds() {
        try {
            executionSem.acquire();
        } catch (InterruptedException e) {
            //Ignore
        }
    }

    private void releaseLock() {
        executionSem.release();
    }

    /**
     * Helper method to acquire debug lock.
     *
     * @return true if acquired successfully.
     */
    public boolean acquireDebugLock() {
        return debugSem.tryAcquire();
    }

    /**
     * Helper method to release debug lock.
     */
    public void releaseDebugLock() {
        debugSem.release();
    }

    /**
     * Helper method to get line number for given ip.
     * @param packagePath   Executing package.
     * @param ip            Instruction pointer.
     * @return  line number.
     */
    public LineNumberInfo getLineNumber(String packagePath, int ip) {
        return this.debugInfoHolder.getLineNumber(packagePath, ip);
    }

    /**
     * Process debug command.
     *
     * @param json the json
     */
    public void processDebugCommand(String json) {
        try {
            processCommand(json);
        } catch (Exception e) {
            MessageDTO message = new MessageDTO();
            message.setCode(DebugConstants.CODE_INVALID);
            message.setMessage(e.getMessage());
            debugServer.pushMessageToClient(debugSession, message);
        }
    }

    private void processCommand(String json) {
        ObjectMapper mapper = new ObjectMapper();
        CommandDTO command = null;
        try {
            command = mapper.readValue(json, CommandDTO.class);
        } catch (IOException e) {
            //invalid message will be passed
            throw new DebugException(DebugConstants.MSG_INVALID);
        }

        switch (command.getCommand()) {
            case DebugConstants.CMD_RESUME:
                resume(command.getThreadId());
                break;
            case DebugConstants.CMD_STEP_OVER:
                stepOver(command.getThreadId());
                break;
            case DebugConstants.CMD_STEP_IN:
                stepIn(command.getThreadId());
                break;
            case DebugConstants.CMD_STEP_OUT:
                stepOut(command.getThreadId());
                break;
            case DebugConstants.CMD_STOP:
                // When stopping the debug session, it will clear all debug points and resume all threads.
                stopDebugging();
                break;
            case DebugConstants.CMD_SET_POINTS:
                // we expect { "command": "SET_POINTS", points: [{ "fileName": "sample.bal", "lineNumber" : 5 },{...}]}
                debugInfoHolder.addDebugPoints(command.getPoints());
                sendAcknowledge("Debug points updated");
                break;
            case DebugConstants.CMD_START:
                // Client needs to explicitly start the execution once connected.
                // This will allow client to set the breakpoints before starting the execution.
                sendAcknowledge("Debug started.");
                startDebug();
                break;
            default:
                throw new DebugException(DebugConstants.MSG_INVALID);
        }
    }

    private void startDebug() {
        debugSession.updateAllDebugContexts(DebugCommand.RESUME);
        releaseLock();
    }

    private void resume(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.RESUME);
        releaseLock();
    }

    private void stepIn(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_IN);
        releaseLock();
    }

    private void stepOver(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_OVER);
        releaseLock();
    }

    private void stepOut(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_OUT);
        releaseLock();
    }

    private void stopDebugging() {
        debugInfoHolder.clearDebugLocations();
        debugSession.updateAllDebugContexts(DebugCommand.RESUME);
        debugSession.clearSession();
        releaseLock();
    }

    private DebugContext getDebugContext(String threadId) {
        DebugContext debugContext = debugSession.getContext(threadId);
        if (debugContext == null) {
            throw new DebugException(DebugConstants.MSG_INVALID_THREAD_ID);
        }
        return debugContext;
    }

    /**
     * Set debug channel.
     *
     * @param channel the channel
     */
    public void addDebugSession(Channel channel) throws DebugException {
        this.debugSession.setChannel(channel);
        sendAcknowledge("Channel registered.");
    }

    /**
     * Add {@link Context} to current execution.
     *
     * @param debugContext context to run
     */
    public void addDebugContext(DebugContext debugContext) {
        this.debugSession.addContext(debugContext);
    }

    public boolean isDebugSessionActive() {
        return (this.debugSession.getChannel() != null);
    }

    /**
     * Send a message to the debug client when a breakpoint is hit.
     *
     * @param breakPointInfo info of the current break point
     */
    public void notifyDebugHit(BreakPointInfo breakPointInfo) {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_HIT);
        message.setMessage(DebugConstants.MSG_HIT);
        message.setThreadId(breakPointInfo.getThreadId());
        message.setLocation(breakPointInfo.getHaltLocation());
        message.setFrames(breakPointInfo.getCurrentFrames());
        debugServer.pushMessageToClient(debugSession, message);
    }


    /**
     * Notify client when debugger has finish execution.
     */
    public void notifyComplete() {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_COMPLETE);
        message.setMessage(DebugConstants.MSG_COMPLETE);
        debugServer.pushMessageToClient(debugSession, message);
    }

    /**
     * Notify client when the debugger is exiting.
     */
    public void notifyExit() {
        if (!isDebugSessionActive()) {
            return;
        }
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_EXIT);
        message.setMessage(DebugConstants.MSG_EXIT);
        debugServer.pushMessageToClient(debugSession, message);
    }

    /**
     * Send a generic acknowledge message to the client.
     *
     * @param messageText message to send to the client
     */
    public void sendAcknowledge(String messageText) {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_ACK);
        message.setMessage(messageText);
        debugServer.pushMessageToClient(debugSession, message);
    }
}
