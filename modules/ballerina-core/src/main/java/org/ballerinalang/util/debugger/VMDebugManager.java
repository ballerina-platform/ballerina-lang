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
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.CommandDTO;
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.ballerinalang.util.debugger.info.BreakPointInfo;
import org.ballerinalang.util.debugger.util.DebugMsgUtil;

import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * {@code VMDebugManager} Manages debug sessions and handle debug related actions.
 *
 * @since 0.88
 */
public class VMDebugManager {

    private boolean debugEnabled = false;

    private DebugServer debugServer;

    private DebugClientHandler clientHandler;

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

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * Method to initialize debug manager.
     *
     * @param programFile   used to initialize debug manager.
     * @param clientHandler session used to communicate with client.
     * @param debugServer Debug server
     */
    public void init(ProgramFile programFile, DebugClientHandler clientHandler, DebugServer debugServer) {
        this.executionSem = new Semaphore(0);
        this.debugSem = new Semaphore(1);
        this.initDebugInfoHolder(programFile);
        this.clientHandler = clientHandler;
        this.debugServer = debugServer;
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
     * 
     * @param debugContext Debug context
     */
    public void addDebugContextAndWait(DebugContext debugContext) {
        this.clientHandler.addContext(debugContext);
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

    /**
     * Method to release execution semaphore.
     */
    public void releaseLock() {
        executionSem.release();
    }

    /**
     * Helper method to acquire debug lock.
     */
    public void acquireDebugLock() {
        try {
            debugSem.acquire();
        } catch (InterruptedException e) {
            //ignore
        }
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
            clientHandler.sendCustomMsg(message);
        }
    }

    private void processCommand(String json) {
        CommandDTO command = null;
        try {
            command = DebugMsgUtil.buildCommandDTO(json);
        } catch (Exception e) {
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
                addDebugPoints(command.getPoints());
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

    /**
     * Method to add debug points.
     *
     * @param breakPointDTOS to be added.
     */
    public void addDebugPoints(List<BreakPointDTO> breakPointDTOS) {
        debugInfoHolder.addDebugPoints(breakPointDTOS);
    }

    public void startDebug() {
        clientHandler.updateAllDebugContexts(DebugCommand.RESUME);
        releaseLock();
    }

    public void resume(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.RESUME);
        releaseLock();
    }

    public void stepIn(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_IN);
        releaseLock();
    }

    public void stepOver(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_OVER);
        releaseLock();
    }

    public void stepOut(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_OUT);
        releaseLock();
    }

    public void stopDebugging() {
        debugInfoHolder.clearDebugLocations();
        clientHandler.updateAllDebugContexts(DebugCommand.RESUME);
        clientHandler.clearChannel();
        releaseLock();
    }

    private DebugContext getDebugContext(String threadId) {
        DebugContext debugContext = clientHandler.getContext(threadId);
        if (debugContext == null) {
            throw new DebugException(DebugConstants.MSG_INVALID_THREAD_ID + threadId);
        }
        return debugContext;
    }

    /**
     * Set debug channel.
     *
     * @param channel the channel
     */
    public void addDebugSession(Channel channel) throws DebugException {
        this.clientHandler.setChannel(channel);
        sendAcknowledge("Channel registered.");
    }

    /**
     * Add {@link Context} to current execution.
     *
     * @param debugContext context to run
     */
    public void addDebugContext(DebugContext debugContext) {
        this.clientHandler.addContext(debugContext);
    }

    /**
     * Helper method to know whether debug session active or not.
     *
     * @return true if active.
     */
    public boolean isDebugSessionActive() {
        return this.clientHandler.isChannelActive();
    }

    /**
     * Send a message to the debug client when a breakpoint is hit.
     *
     * @param breakPointInfo info of the current break point
     */
    public void notifyDebugHit(BreakPointInfo breakPointInfo) {
        clientHandler.notifyHalt(breakPointInfo);
    }


    /**
     * Notify client when debugger has finish execution.
     */
    public void notifyComplete() {
        clientHandler.notifyComplete();
    }

    /**
     * Notify client when the debugger is exiting.
     */
    public void notifyExit() {
        if (!isDebugSessionActive()) {
            return;
        }
        clientHandler.notifyExit();
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
        clientHandler.sendCustomMsg(message);
    }
}
