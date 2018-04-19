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
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
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
        this.debugInfoHolder = new DebugInfoHolder(programFile);
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
     * Helper method to stop execution of current worker.
     *
     * @param ctx of the current worker.
     */
    public void pauseWorker(WorkerExecutionContext ctx) {
        ctx.getDebugContext().setWorkerPaused(true);
        BLangScheduler.workerPaused(ctx);
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

    /**
     * Method to start debugging.
     */
    public void startDebug() {
        executionSem.release();
    }

    /**
     * Method to resume current debug session.
     *
     * @param workerId of the worker to be resumed.
     */
    public void resume(String workerId) {
        WorkerExecutionContext ctx = getWorkerContext(workerId);
        ctx.getDebugContext().setCurrentCommand(DebugCommand.RESUME);
        BLangScheduler.resume(ctx);
    }

    /**
     * Method to do "STEP_IN" command.
     *
     * @param workerId to be resumed.
     */
    public void stepIn(String workerId) {
        WorkerExecutionContext ctx = getWorkerContext(workerId);
        ctx.getDebugContext().setCurrentCommand(DebugCommand.STEP_IN);
        BLangScheduler.resume(ctx);
    }

    /**
     * Method to do "STEP_OVER" command.
     *
     * @param workerId to be resumed.
     */
    public void stepOver(String workerId) {
        WorkerExecutionContext ctx = getWorkerContext(workerId);
        ctx.getDebugContext().setCurrentCommand(DebugCommand.STEP_OVER);
        BLangScheduler.resume(ctx);
    }

    /**
     * Method to do "STEP_OUT" command.
     *
     * @param workerId to be resumed.
     */
    public void stepOut(String workerId) {
        WorkerExecutionContext ctx = getWorkerContext(workerId);
        ctx.getDebugContext().setCurrentCommand(DebugCommand.STEP_OUT);
        BLangScheduler.resume(ctx);
    }

    /**
     * Method to stop debugging process for all threads.
     */
    public void stopDebugging() {
        debugInfoHolder.clearDebugLocations();
        clientHandler.getAllWorkerContexts().forEach((k, v) -> {
            v.getDebugContext().setCurrentCommand(DebugCommand.RESUME);
            BLangScheduler.resume(v);
        });
        clientHandler.clearChannel();
    }

    private WorkerExecutionContext getWorkerContext(String workerId) {
        WorkerExecutionContext ctx = clientHandler.getWorkerContext(workerId);
        if (ctx == null) {
            throw new DebugException(DebugConstants.MSG_INVALID_WORKER_ID + workerId);
        }
        return ctx;
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
     * Add {@link WorkerExecutionContext} to current execution.
     *
     * @param ctx context to run.
     */
    public void addWorkerContext(WorkerExecutionContext ctx) {
        this.clientHandler.addWorkerContext(ctx);
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
     * @param ctx               Current context.
     * @param currentExecLine   Current execution line.
     * @param workerId          Current thread id.
     */
    public void notifyDebugHit(WorkerExecutionContext ctx, LineNumberInfo currentExecLine, String workerId) {
        MessageDTO message = generateDebugHitMessage(ctx, currentExecLine, workerId);
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
     * @param ctx               Current context.
     * @param currentExecLine   Current execution line.
     * @param workerId          Current thread id.
     * @return  message         To be sent.
     */
    private MessageDTO generateDebugHitMessage(WorkerExecutionContext ctx, LineNumberInfo currentExecLine, 
            String workerId) {
        MessageDTO message = new MessageDTO(DebugConstants.CODE_HIT, DebugConstants.MSG_HIT);
        message.setThreadId(workerId);

        BreakPointDTO breakPointDTO = new BreakPointDTO(currentExecLine.getPackageInfo().getPkgPath(),
                currentExecLine.getFileName(), currentExecLine.getLineNumber());
        message.setLocation(breakPointDTO);

        int callingIp = currentExecLine.getIp();

        String pck = ctx.callableUnitInfo.getPackageInfo().getPkgPath();

        String functionName = ctx.callableUnitInfo.getName();
        LineNumberInfo callingLine = getLineNumber(ctx.callableUnitInfo.getPackageInfo().getPkgPath(), callingIp);
        FrameDTO frameDTO = new FrameDTO(pck, functionName, callingLine.getFileName(),
                callingLine.getLineNumber());
        message.addFrame(frameDTO);
        LocalVariableAttributeInfo localVarAttrInfo = (LocalVariableAttributeInfo) ctx.workerInfo
                .getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);

        localVarAttrInfo.getLocalVariables().forEach(l -> {
            VariableDTO variableDTO = new VariableDTO(l.getVariableName(), "Local");
            switch (l.getVariableType().getSuperType().getTag()) {
                case TypeTags.INT_TAG:
                    variableDTO.setBValue(new BInteger(ctx.workerLocal.longRegs[l.getVariableIndex()]));
                    break;
                case TypeTags.FLOAT_TAG:
                    variableDTO.setBValue(new BFloat(ctx.workerLocal.doubleRegs[l.getVariableIndex()]));
                    break;
                case TypeTags.STRING_TAG:
                    variableDTO.setBValue(new BString(ctx.workerLocal.stringRegs[l.getVariableIndex()]));
                    break;
                case TypeTags.BOOLEAN_TAG:
                    variableDTO.setBValue(new BBoolean(ctx.workerLocal.intRegs[l.getVariableIndex()] == 1));
                    break;
                case TypeTags.BLOB_TAG:
                    variableDTO.setBValue(new BBlob(ctx.workerLocal.byteRegs[l.getVariableIndex()]));
                    break;
                default:
                    variableDTO.setBValue(ctx.workerLocal.refRegs[l.getVariableIndex()]);
                    break;
            }
            frameDTO.addVariable(variableDTO);
        });
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
