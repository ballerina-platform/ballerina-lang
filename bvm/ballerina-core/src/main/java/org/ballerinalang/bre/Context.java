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

import org.ballerinalang.bre.bvm.ObservableContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.transactions.LocalTransactionInfo;

import java.util.Map;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 0.8.0
 */
public interface Context extends ObservableContext {

    WorkerExecutionContext getParentWorkerExecutionContext();

    CallableUnitInfo getCallableUnitInfo();

    WorkerData getLocalWorkerData();

    DebugContext getDebugContext();

    void setDebugContext(DebugContext debugContext);

    Object getProperty(String key);

    Map<String, Object> getProperties();

    void setProperty(String key, Object value);

    ServiceInfo getServiceInfo();

    void setServiceInfo(ServiceInfo serviceInfo);

    boolean isInTransaction();

    BStruct getError();

    void setError(BStruct error);

    ProgramFile getProgramFile();

    LocalTransactionInfo getLocalTransactionInfo();

    long getIntArgument(int index);

    String getStringArgument(int index);

    String getNullableStringArgument(int index);

    double getFloatArgument(int index);

    boolean getBooleanArgument(int index);

    byte[] getBlobArgument(int index);

    BValue getRefArgument(int index);

    BValue getNullableRefArgument(int index);

    void setReturnValues(BValue... values);

    BValue[] getReturnValues();

}
