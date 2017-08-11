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
import org.ballerinalang.bre.bvm.DebuggerExecutor;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.dto.CommandDTO;
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;


/**
 * {@code VMDebugManager} Manages debug sessions and handle debug related actions.
 *
 * @since 0.88
 */
public class VMDebugManager {
    /**
     * The Execution sem. used to block debugger till client connects.
     */
    private volatile Semaphore executionWaitSem;

    private VMDebugServer debugServer;

    private boolean debugEnabled;

    /**
     * Object to hold debug session related context.
     */
    private VMDebugSession debugSession;

    private static VMDebugManager debugManagerInstance = null;

    private boolean debugManagerInitialized = false;

    /**
     * Instantiates a new Debug manager.
     */
    private VMDebugManager() {
        executionWaitSem = new Semaphore(0);
        debugServer = new VMDebugServer();
        debugSession = new VMDebugSession();
    }

    /**
     * Debug manager singleton.
     *
     * @return DebugManager instance
     */
    public static VMDebugManager getInstance() {
        if (debugManagerInstance != null) {
            return debugManagerInstance;
        }
        return initialize();
    }

    private static synchronized VMDebugManager initialize() {
        if (debugManagerInstance == null) {
            debugManagerInstance = new VMDebugManager();
        }
        return debugManagerInstance;
    }

    /**
     * Initializes the debug manager single instance.
     */
    public synchronized void serviceInit() {
        if (this.debugManagerInitialized) {
            throw new BallerinaException("Debugger instance already initialized");
        }
        // start the debug server if it is not started yet.
        this.debugServer.startServer();
        this.debugManagerInitialized = true;
    }

    public synchronized void mainInit(ProgramFile programFile, Context mainThreadContext) {
        if (this.debugManagerInitialized) {
            throw new BallerinaException("Debugger instance already initialized");
        }
        mainThreadContext.setAndInitDebugInfoHolder(new DebugInfoHolder());
        mainThreadContext.setDebugEnabled(true);
        DebuggerExecutor debuggerExecutor = new DebuggerExecutor(programFile, mainThreadContext);
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        mainThreadContext.getDebugInfoHolder().setDebugSessionObserver(debugSession);
        executor.submit(debuggerExecutor);
        // start the debug server if it is not started yet.
        this.debugServer.startServer();
        this.debugManagerInitialized = true;
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
                getHolder(command.getThreadId()).resume();
                break;
            case DebugConstants.CMD_STEP_OVER:
                getHolder(command.getThreadId()).stepOver();
                break;
            case DebugConstants.CMD_STEP_IN:
                getHolder(command.getThreadId()).stepIn();
                break;
            case DebugConstants.CMD_STEP_OUT:
                getHolder(command.getThreadId()).stepOut();
                break;
            case DebugConstants.CMD_STOP:
                // When stopping the debug session, it will clear all debug points and resume all threads.
                debugSession.stopDebug();
                debugSession.clearSession();
                break;
            case DebugConstants.CMD_SET_POINTS:
                // we expect { "command": "SET_POINTS", points: [{ "fileName": "sample.bal", "lineNumber" : 5 },{...}]}
                debugSession.addDebugPoints(command.getPoints());
                sendAcknowledge(this.debugSession, "Debug points updated");
                break;
            case DebugConstants.CMD_START:
                // Client needs to explicitly start the execution once connected.
                // This will allow client to set the breakpoints before starting the execution.
                sendAcknowledge(this.debugSession, "Debug started.");
                debugSession.startDebug();
                break;
            default:
                throw new DebugException(DebugConstants.MSG_INVALID);
        }
    }

    private DebugInfoHolder getHolder(String threadId) {
        if (debugSession.getContext(threadId) == null) {
            throw new DebugException(DebugConstants.MSG_INVALID_THREAD_ID);
        }
        return debugSession.getContext(threadId).getDebugInfoHolder();
    }

    /**
     * Set debug channel.
     *
     * @param channel the channel
     */
    public void addDebugSession(Channel channel) throws DebugException {
        this.debugSession.setChannel(channel);
        sendAcknowledge(this.debugSession, "Channel registered.");
    }

    /**
     *  Hold on to main thread while debugger finishes execution.
     */
    public void holdON() {
        //suspend the current thread till debugging process finishes
        try {
            executionWaitSem.acquire();
        } catch (InterruptedException e) {
            // Do nothing probably someone wants to shutdown the thread.
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Add {@link Context} to current execution.
     *
     * @param bContext context to run
     */
    public void setDebuggerContext(Context bContext) {
        // if we are handling multiple connections
        // we need to check and set to correct debugger session
        if (!isDebugSessionActive()) {
            throw new IllegalStateException("Debug session has not initialize, Unable to set debugger.");
        }
        bContext.getDebugInfoHolder().setDebugSessionObserver(debugSession);
        this.debugSession.addContext(bContext);
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void releaseExecutionLock() {
        this.executionWaitSem.release();
    }

    public boolean isDebugSessionActive() {
        return (this.debugSession.getChannel() != null);
    }


    /**
     * Send a message to the debug client when a breakpoint is hit.
     *
     * @param debugSession current debugging session
     * @param breakPointInfo info of the current break point
     */
    public void notifyDebugHit(VMDebugSession debugSession, BreakPointInfo breakPointInfo) {
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
     *
     * @param debugSession current debugging session
     */
    public void notifyComplete(VMDebugSession debugSession) {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_COMPLETE);
        message.setMessage(DebugConstants.MSG_COMPLETE);
        debugServer.pushMessageToClient(debugSession, message);
    }

    /**
     * Notify client when the debugger is exiting.
     *
     * @param debugSession current debugging session
     */
    public void notifyExit(VMDebugSession debugSession) {
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
     * @param debugSession current debugging session
     * @param messageText message to send to the client
     */
    public void sendAcknowledge(VMDebugSession debugSession, String messageText) {
        MessageDTO message = new MessageDTO();
        message.setCode(DebugConstants.CODE_ACK);
        message.setMessage(messageText);
        debugServer.pushMessageToClient(debugSession, message);
    }
}
