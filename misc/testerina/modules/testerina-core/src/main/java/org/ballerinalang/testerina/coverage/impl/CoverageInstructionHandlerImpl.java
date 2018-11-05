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
package org.ballerinalang.testerina.coverage.impl;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.coverage.InstructionHandler;
import org.ballerinalang.bre.coverage.ExecutedInstruction;
import org.ballerinalang.testerina.coverage.CoverageManager;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.debugger.LineNumberInfoHolder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JavaSPIService("org.ballerinalang.bre.coverage.InstructionHandler")
public class CoverageInstructionHandlerImpl implements InstructionHandler {

    Map<String, List<ExecutedInstruction>> executedInstructionOrderMap;

    Map<String, LineNumberInfoHolder> lineNumberInfoHolderForProject;

    public CoverageInstructionHandlerImpl() {
        CoverageManager coverageManager = CoverageManager.getInstance();
        this.executedInstructionOrderMap = coverageManager.getExecutedInstructionOrderMap();
        this.lineNumberInfoHolderForProject = coverageManager.getLineNumberInfoHolderForProject();
    }

    public void handle(WorkerExecutionContext ctx) {

        String entryPkgPath = ctx.programFile.getEntryPackage().pkgPath;
        LineNumberInfoHolder lineNumberInfoHolderForPkg = lineNumberInfoHolderForProject.get(entryPkgPath);

        String pkgPath = ctx.callableUnitInfo.getPkgPath();
        LineNumberInfo lineNumberInfoCurrentPkg = lineNumberInfoHolderForPkg.getPackageInfoMap().get(pkgPath).getLineNumberInfo(ctx.ip);

        ExecutedInstruction executedInstruction = new ExecutedInstruction(ctx.ip, pkgPath,
                lineNumberInfoCurrentPkg.getFileName(), ctx.callableUnitInfo.getName(), lineNumberInfoCurrentPkg);
        if(executedInstructionOrderMap.get(entryPkgPath) != null) {
            executedInstructionOrderMap.get(entryPkgPath).add(executedInstruction);
        } else {
            List<ExecutedInstruction> executedInstructionOrder = new LinkedList<>();
            executedInstructionOrder.add(executedInstruction);
            executedInstructionOrderMap.put(entryPkgPath, executedInstructionOrder);
        }

    }

}
