/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.BallerinaTransactionManager;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a Ballerina worker execution context.
 */
public class WorkerExecutionContext {

    public WorkerExecutionContext parent;
    
    public WorkerState state = WorkerState.CREATED;
    
    public Map<String, Object> properties = new HashMap<>();
    
    public int ip;
    
    public ProgramFile programFile;
    
    public ConstantPoolEntry[] constPool;
    
    public Instruction[] code;
    
    public WorkerData workerLocal;
    
    public WorkerData workerResult;
    
    public int[] retRegIndexes;
    
    public BallerinaTransactionManager ballerinaTransactionManager;
    
    public CallableUnitInfo callableUnitInfo;

    public WorkerExecutionContext(WorkerExecutionContext parent, ProgramFile programFile, 
            ConstantPoolEntry[] constPool, Instruction[] code, WorkerData workerLocal,
            WorkerData workerResult, int[] retRegIndexes, CallableUnitInfo callableUnitInfo) {
        this.parent = parent;
        this.programFile = programFile;
        this.constPool = constPool;
        this.code = code;
        this.workerLocal = workerLocal;
        this.workerResult = workerResult;
        this.retRegIndexes = retRegIndexes;
    }
    
    public void setError(BStruct error) {
        //TODO
    }

    public boolean isInTransaction() {
        // TODO 
        return false;
    }
    
}
