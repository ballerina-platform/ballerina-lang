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

import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.connector.impl.BServerConnectorFuture;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.debugger.DebugContext;

import java.util.Map;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 0.8.0
 */
public interface Context {

    public WorkerExecutionContext getParentWorkerExecutionContext();
    
    public WorkerData getLocalWorkerData();
    
    public DebugContext getDebugContext();
    
    public void setDebugContext(DebugContext debugContext);

    public Object getProperty(String key);
    
    public Map<String, Object> getProperties();

    public void setProperty(String key, Object value);

    public BServerConnectorFuture getConnectorFuture();

    public void setConnectorFuture(BServerConnectorFuture connectorFuture);

    public ServiceInfo getServiceInfo();
    
    public void setServiceInfo(ServiceInfo serviceInfo);

    public void setBallerinaTransactionManager(BallerinaTransactionManager ballerinaTransactionManager);

    public BallerinaTransactionManager getBallerinaTransactionManager();

    public boolean isInTransaction();

    public BStruct getError();

    public void setError(BStruct error);

    public ProgramFile getProgramFile();

    /**
     * Data holder for Non-Blocking Action invocation.
     *
     * @since 0.96.0
     */
    public static class NonBlockingContext {
        public ActionInfo actionInfo;
        public int[] retRegs;

        public NonBlockingContext(ActionInfo actionInfo, int[] retRegs) {
            this.actionInfo = actionInfo;
            this.retRegs = retRegs;
        }
    }
}
