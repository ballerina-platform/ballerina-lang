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
package org.ballerinalang.bre.bvm.persistency;

import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableRefFields;
import org.ballerinalang.util.codegen.ProgramFile;

public class SerializableWorkerData {

    public long[] longRegs;

    public double[] doubleRegs;

    public String[] stringRegs;

    public int[] intRegs;

    public byte[][] byteRegs;

    public SerializableRefFields refFields;

    public SerializableWorkerData(WorkerData workerData, SerializableState state) {
        byteRegs = workerData.byteRegs;
        doubleRegs = workerData.doubleRegs;
        intRegs = workerData.intRegs;
        longRegs = workerData.longRegs;
        stringRegs = workerData.stringRegs;

        if (workerData.refRegs != null) {
            refFields = new SerializableRefFields(workerData.refRegs, state);
        }
    }

    public WorkerData getWorkerData(ProgramFile programFile, SerializableState state) {
        WorkerData workerData = new WorkerData();
        workerData.longRegs = longRegs;
        workerData.doubleRegs = doubleRegs;
        workerData.stringRegs = stringRegs;
        workerData.intRegs = intRegs;
        workerData.byteRegs = byteRegs;
        if (refFields != null) {
            workerData.refRegs = refFields.getRefFeilds(programFile, state);
        }
        return workerData;
    }
}