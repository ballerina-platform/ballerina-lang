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
package org.ballerinalang.bre.coverage.impl;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.coverage.InstructionHandler;
import org.ballerinalang.bre.coverage.ExecutedInstruction;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CoverageInstructionHandlerImpl implements InstructionHandler {

    Map<String, List<ExecutedInstruction>> executedInstructionOrderMap;

    public CoverageInstructionHandlerImpl(Map<String, List<ExecutedInstruction>> executedInstructionOrderMap) {
        this.executedInstructionOrderMap = executedInstructionOrderMap;
    }

    public void handle(WorkerExecutionContext ctx, Instruction instruction) {

        ProgramFile programFile = ctx.programFile;
        Debugger debugger = programFile.getDebugger();
        String pkgPath = ctx.callableUnitInfo.getPkgPath();
        LineNumberInfo lineNumberInfo = debugger.getDebugInfoHolder().getPackageInfoMap()
                .get(pkgPath).getLineNumberInfoHolder().getLineNumberInfo(ctx.ip);

            ExecutedInstruction executedInstruction = new ExecutedInstruction(ctx.ip, pkgPath,
                    lineNumberInfo.getFileName(), ctx.callableUnitInfo.getName());
            if(executedInstructionOrderMap.get(pkgPath) != null) {
                executedInstructionOrderMap.get(pkgPath).add(executedInstruction);
            } else {
                List<ExecutedInstruction> executedInstructionOrder = new LinkedList<>();
                executedInstructionOrder.add(executedInstruction);
                executedInstructionOrderMap.put(pkgPath, executedInstructionOrder);
            }

    }

}
