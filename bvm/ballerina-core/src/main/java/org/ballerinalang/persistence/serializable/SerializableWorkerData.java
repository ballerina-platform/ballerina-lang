/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;

/**
 * This class represents a serializable Ballerina worker data.
 *
 * @since 0.981.1
 */
public class SerializableWorkerData {

    public long[] longRegs;

    public double[] doubleRegs;

    public String[] stringRegs;

    public int[] intRegs;

    public ArrayList<Object> refFields;

    public SerializableWorkerData(WorkerData workerData, SerializableState state) {
        doubleRegs = workerData.doubleRegs;
        intRegs = workerData.intRegs;
        longRegs = workerData.longRegs;
        stringRegs = workerData.stringRegs;
        refFields = state.serializeRefFields(workerData.refRegs);
    }

    WorkerData getWorkerData(ProgramFile programFile, SerializableState state, Deserializer deserializer) {
        WorkerData workerData = new WorkerData();
        workerData.longRegs = longRegs;
        workerData.doubleRegs = doubleRegs;
        workerData.stringRegs = stringRegs;
        workerData.intRegs = intRegs;
        workerData.refRegs = state.deserializeRefFields(refFields, programFile, deserializer);
        return workerData;
    }
}
