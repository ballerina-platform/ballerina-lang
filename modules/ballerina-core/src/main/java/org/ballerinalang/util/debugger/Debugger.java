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
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.CommandDTO;
import org.ballerinalang.util.debugger.dto.FrameDTO;
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.ballerinalang.util.debugger.dto.VariableDTO;
import org.ballerinalang.util.debugger.util.DebugMsgUtil;

import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * {@code VMDebugManager} Manages debug sessions and handle debug related actions.
 *
 * @since 0.88
 */
public class Debugger {

    private boolean debugEnabled = false;

    private ProgramFile programFile;

    private DebugClientHandler clientHandler;

    private DebugInfoHolder debugInfoHolder;

    private volatile Semaphore executionSem;

    private volatile Semaphore debugSem;

    public Debugger(ProgramFile programFile) {
        this.programFile = programFile;
        String debug = System.getProperty(Constants.SYSTEM_PROP_BAL_DEBUG);
        if (debug != null && !debug.isEmpty()) {
            debugEnabled = true;
        }
    }

    /**
     * Method to initialize debug manager.
     */
    public void init() {
        this.setupDebugger();
        this.setClientHandler(new VMDebugClientHandler());
        DebugServer debugServer = new DebugServer(this);
        debugServer.startServer();
        /*
          registering a shutdown hook to shut down netty debug listener when shutting down the system.
         */
        Runtime.getRuntime().addShutdownHook(new DebuggerShutDownHook(this, debugServer));
    }

    protected void setupDebugger() {
        this.executionSem = new Semaphore(0);
        this.debugSem = new Semaphore(1);
        this.debugInfoHolder = new DebugInfoHolder(programFile);
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
     * Method to try acquire debug session.
     */
    public boolean tryAcquireDebugSessionLock() {
        return debugSem.tryAcquire();
    }

    /**
     * Method to release the session and debug lock.
     */
    public void releaseDebugSessionLock() {
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
    void processDebugCommand(String json) {
        try {
            processCommand(json);
        } catch (Exception e) {
            MessageDTO message = new MessageDTO(DebugConstants.CODE_INVALID, e.getMessage());
            clientHandler.sendCustomMsg(message);
            //in case exception occurs, debug will resume and session disconnected
            stopDebugging();
        }
    }

    private void processCommand(String json) {
        CommandDTO command;
        try {
            command = DebugMsgUtil.buildCommandDTO(json);
        } catch (Exception e) {
            //invalid message will be passed
            throw new DebugException(DebugConstants.MSG_INVALID);
        }

        switch (command.getCommand()) {
            case DebugConstants.CMD_RESUME:
                resume();
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

    /**
     * Method to start debugging.
     */
    public void startDebug() {
        resume();
    }

    /**
     * Method to resume current debug session.
     */
    public void resume() {
        clientHandler.updateAllDebugContexts(DebugCommand.RESUME);
        executionSem.release();
    }

    /**
     * Method to do "STEP_IN" command.
     *
     * @param threadId to be resumed.
     */
    public void stepIn(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_IN);
        executionSem.release();
    }

    /**
     * Method to do "STEP_OVER" command.
     *
     * @param threadId to be resumed.
     */
    public void stepOver(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_OVER);
        executionSem.release();
    }

    /**
     * Method to do "STEP_OUT" command.
     *
     * @param threadId to be resumed.
     */
    public void stepOut(String threadId) {
        getDebugContext(threadId).setCurrentCommand(DebugCommand.STEP_OUT);
        executionSem.release();
    }

    /**
     * Method to stop debugging process for all threads.
     */
    public void stopDebugging() {
        debugInfoHolder.clearDebugLocations();
        clientHandler.updateAllDebugContexts(DebugCommand.RESUME);
        clientHandler.clearChannel();
        //releasing session lock and execution lock to resume debug
        debugSem.release();
        executionSem.release();
    }

    private DebugContext getDebugContext(String threadId) {
        DebugContext debugContext = clientHandler.getContext(threadId);
        if (debugContext == null) {
            throw new DebugException(DebugConstants.MSG_INVALID_THREAD_ID + threadId);
        }
        return debugContext;
    }

    /**
     * Set debug session by adding the channel.
     *
     * @param channel the channel
     */
    void addDebugSession(Channel channel) throws DebugException {
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
    public boolean isClientSessionActive() {
        return this.clientHandler.isChannelActive();
    }

    /**
     * Send a message to the debug client when a breakpoint is hit.
     *
     * @param frame             Current stack frame.
     * @param currentExecLine   Current execution line.
     * @param threadId          Current thread id.
     */
    public void notifyDebugHit(StackFrame frame, LineNumberInfo currentExecLine, String threadId) {
        MessageDTO message = generateDebugHitMessage(frame, currentExecLine, threadId);
        clientHandler.notifyHalt(message);
    }

    /**
     * Notify client when the debugger is exiting.
     */
    public void notifyExit() {
        if (!isClientSessionActive()) {
            return;
        }
        clientHandler.notifyExit();
    }

    /**
     * Send a generic acknowledge message to the client.
     *
     * @param messageText message to send to the client
     */
    private void sendAcknowledge(String messageText) {
        MessageDTO message = new MessageDTO(DebugConstants.CODE_ACK, messageText);
        clientHandler.sendCustomMsg(message);
    }

    /**
     * Generate debug hit message.
     *
     * @param frame             Current stack frame.
     * @param currentExecLine   Current execution line.
     * @param threadId          Current thread id.
     * @return  message         To be sent.
     */
    private MessageDTO generateDebugHitMessage(StackFrame frame, LineNumberInfo currentExecLine, String threadId) {
        MessageDTO message = new MessageDTO(DebugConstants.CODE_HIT, DebugConstants.MSG_HIT);
        message.setThreadId(threadId);

        BreakPointDTO breakPointDTO = new BreakPointDTO(currentExecLine.getPackageInfo().getPkgPath(),
                currentExecLine.getFileName(), currentExecLine.getLineNumber());
        message.setLocation(breakPointDTO);

        int callingIp = currentExecLine.getIp();
        while (frame != null) {
            String pck = frame.getPackageInfo().getPkgPath();
            if (frame.getCallableUnitInfo() == null) {
                frame = frame.prevStackFrame;
                continue;
            }
            //TODO is this ok to show function name when it's a worker which is executing?
            String functionName = frame.getCallableUnitInfo().getName();
            LineNumberInfo callingLine = getLineNumber(frame.getPackageInfo().getPkgPath(), callingIp);
            FrameDTO frameDTO = new FrameDTO(pck, functionName, callingLine.getFileName(),
                    callingLine.getLineNumber());
            message.addFrame(frameDTO);
            LocalVariableAttributeInfo localVarAttrInfo = (LocalVariableAttributeInfo) frame.getWorkerInfo()
                    .getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
            if (localVarAttrInfo == null) {
                frame = frame.prevStackFrame;
                continue;
            }
            StackFrame fcp = frame;
            localVarAttrInfo.getLocalVariables().forEach(l -> {
                VariableDTO variableDTO = new VariableDTO(l.getVariableName(), "Local");
                switch (l.getVariableType().getTag()) {
                    case TypeTags.INT_TAG:
                        variableDTO.setBValue(new BInteger(fcp.getLongRegs()[l.getVariableIndex()]));
                        break;
                    case TypeTags.FLOAT_TAG:
                        variableDTO.setBValue(new BFloat(fcp.getDoubleRegs()[l.getVariableIndex()]));
                        break;
                    case TypeTags.STRING_TAG:
                        variableDTO.setBValue(new BString(fcp.getStringRegs()[l.getVariableIndex()]));
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        variableDTO.setBValue(new BBoolean(fcp.getIntRegs()[l.getVariableIndex()] == 1));
                        break;
                    case TypeTags.BLOB_TAG:
                        variableDTO.setBValue(new BBlob(fcp.getByteRegs()[l.getVariableIndex()]));
                        break;
                    default:
                        variableDTO.setBValue(fcp.getRefRegs()[l.getVariableIndex()]);
                        break;
                }
                frameDTO.addVariable(variableDTO);
            });
            callingIp = frame.getRetAddrs() - 1;
            if (callingIp < 0) {
                callingIp = 0;
            }
            frame = frame.prevStackFrame;
        }
        return message;
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
     * Method to set debug enabled or not.
     */
    public void setDebugEnabled() {
        this.debugEnabled = true;
    }

    /**
     * Method to set client handler.
     *
     * @param clientHandler instance.
     */
    protected void setClientHandler(DebugClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    /**
     * Shutdown hook to clean up debug server when shut down happens.
     */
    static class DebuggerShutDownHook extends Thread {

        private Debugger debugger;
        private DebugServer debugServer;

        DebuggerShutDownHook(Debugger debugger, DebugServer debugServer) {
            this.debugger = debugger;
            this.debugServer = debugServer;
        }

        @Override
        public void run() {
            debugger.notifyExit();
            debugServer.closeServerChannel();
        }
    }
}
