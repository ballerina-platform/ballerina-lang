/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bre;

import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.connector.impl.BServerConnectorFuture;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.debugger.DebugContext;

import java.util.Map;

/**
 * This class represents a {@link Context} implementation for native calls.
 */
public class NativeCallContext implements Context {

    private static final String SERVICE_INFO_KEY = "SERVICE_INFO";

    private static final String CONNECTOR_FUTURE_KEY = "CONNECTOR_FUTURE";

    private WorkerExecutionContext parentCtx;
    
    private WorkerData workerLocal;
    
    public NativeCallContext(WorkerExecutionContext parentCtx, WorkerData workerLocal) {
        this.parentCtx = parentCtx;
        this.workerLocal = workerLocal;
    }
    
    @Override
    public WorkerExecutionContext getParentWorkerExecutionContext() {
        return parentCtx;
    }
    
    public WorkerData getLocalWorkerData() {
        return workerLocal;
    }
    
    @Override
    public DebugContext getDebugContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDebugContext(DebugContext debugContext) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object getProperty(String key) {
        return this.parentCtx.globalProps.get(key);
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.parentCtx.globalProps;
    }

    @Override
    public void setProperty(String key, Object value) {
        this.parentCtx.globalProps.put(key, value);
    }

    @Override
    public BServerConnectorFuture getConnectorFuture() {
        return (BServerConnectorFuture) this.getProperty(CONNECTOR_FUTURE_KEY);
    }

    @Override
    public void setConnectorFuture(BServerConnectorFuture connectorFuture) {
        this.setProperty(CONNECTOR_FUTURE_KEY, connectorFuture);        
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return (ServiceInfo) this.getProperty(SERVICE_INFO_KEY);
    }

    @Override
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.setProperty(SERVICE_INFO_KEY, serviceInfo);
    }

    @Override
    public void setBallerinaTransactionManager(BallerinaTransactionManager ballerinaTransactionManager) {
        this.parentCtx.setBallerinaTransactionManager(ballerinaTransactionManager);
    }

    @Override
    public BallerinaTransactionManager getBallerinaTransactionManager() {
        return this.parentCtx.getBallerinaTransactionManager();
    }

    @Override
    public boolean isInTransaction() {
        return this.parentCtx.isInTransaction();
    }

    @Override
    public BStruct getError() {
        return this.parentCtx.getError();
    }

    @Override
    public void setError(BStruct error) {
        this.parentCtx.setError(error);
    }
    
    @Override
    public ProgramFile getProgramFile() {
        return this.parentCtx.programFile;
    }

}
