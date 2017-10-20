/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre;

import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.bvm.WorkerCounter;
import org.ballerinalang.connector.impl.BServerConnectorFuture;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 0.8.0
 */
public class Context {

    //TODO: Rename this into BContext and move this to runtime package
    private ControlStackNew controlStackNew;
    //TODO remove below after jms and ftp full migration.
    private CarbonMessage cMsg;
    private BServerConnectorFuture connectorFuture;
    protected Map<String, Object> properties = new HashMap<>();
    private ServiceInfo serviceInfo;
    private BallerinaTransactionManager ballerinaTransactionManager;
    private DebugInfoHolder debugInfoHolder;
    private boolean debugEnabled = false;

    private int startIP;
    private BStruct unhandledError;

    protected WorkerCounter workerCounter;

    // TODO : Temporary solution to make non-blocking working.
    public BValue[] nativeArgValues;
    public ProgramFile programFile;
    public FunctionCallCPEntry funcCallCPEntry;
    public ActionInfo actionInfo;
    private String threadId;
    // TODO : Fix this. Added this for fork-join. Issue #3718.
    public boolean blockingInvocation;

    @Deprecated
    public Context() {
        this.controlStackNew = new ControlStackNew();
    }

    public Context(ProgramFile programFile) {
        this.programFile = programFile;
        this.controlStackNew = new ControlStackNew();
        this.workerCounter = new WorkerCounter();
    }

    public DebugInfoHolder getDebugInfoHolder() {
        return debugInfoHolder;
    }

    public void setAndInitDebugInfoHolder(DebugInfoHolder debugInfoHolder) {
        if (this.debugInfoHolder != null) {
            return;
        }
        synchronized (Context.class) {
            if (this.debugInfoHolder != null) {
                return;
            }
            this.setDebugInfoHolder(debugInfoHolder);
            this.debugInfoHolder.init(programFile);
        }
    }
    
    public void setDebugInfoHolder(DebugInfoHolder debugInfoHolder) {
        this.debugInfoHolder = debugInfoHolder;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public ControlStackNew getControlStackNew() {
        return controlStackNew;
    }

    public CarbonMessage getCarbonMessage() {
        return this.cMsg;
    }

    public void setCarbonMessage(CarbonMessage cMsg) {
        this.cMsg = cMsg;
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public BServerConnectorFuture getConnectorFuture() {
        return connectorFuture;
    }

    public void setConnectorFuture(BServerConnectorFuture connectorFuture) {
        this.connectorFuture = connectorFuture;
    }

    public ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public void setBallerinaTransactionManager(BallerinaTransactionManager ballerinaTransactionManager) {
        this.ballerinaTransactionManager = ballerinaTransactionManager;
    }

    public BallerinaTransactionManager getBallerinaTransactionManager() {
        return this.ballerinaTransactionManager;
    }

    public boolean isInTransaction() {
        return this.ballerinaTransactionManager != null;
    }

    public BStruct getError() {
        if (controlStackNew.currentFrame != null) {
            return controlStackNew.currentFrame.getErrorThrown();
        }
        return this.unhandledError;
    }

    public void setError(BStruct error) {
        if (controlStackNew.currentFrame != null) {
            controlStackNew.currentFrame.setErrorThrown(error);
        } else {
            this.unhandledError = error;
        }
    }

    public int getStartIP() {
        return startIP;
    }

    public void setStartIP(int startIP) {
        this.startIP = startIP;
    }

    public ProgramFile getProgramFile() {
        return programFile;
    }

    /**
     * start tracking current worker.
     */
    public void startTrackWorker() {
        workerCounter.countUp();
    }

    /**
     * end tracking current worker.
     */
    public void endTrackWorker() {
        workerCounter.countDown();
    }

    /**
     * Wait until all spawned workers are completed.
     */
    public void await() {
        workerCounter.await();
    }

    /**
     * Wait until all spawned worker are completed within the given waiting time.
     *
     * @param timeout time out duration in seconds.
     * @return {@code true} if a all workers are completed within the given waiting time, else otherwise.
     */
    public boolean await(int timeout) {
        return workerCounter.await(timeout);
    }

    /**
     * Mark this context is associated with a resource.
     */
    public void setAsResourceContext() {
        this.workerCounter.setResourceContext(this);
    }

    public void resetWorkerContextFlow() {
        this.workerCounter = new WorkerCounter();
    }

    public WorkerCounter getWorkerCounter() {
        return workerCounter;
    }
}
