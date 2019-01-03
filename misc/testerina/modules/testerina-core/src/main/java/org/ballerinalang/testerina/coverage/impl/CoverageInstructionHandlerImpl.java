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
import org.ballerinalang.bre.InstructionHandler;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.testerina.coverage.CoverageManager;
import org.ballerinalang.testerina.coverage.ExecutedInstruction;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.debugger.ProjectLineNumberInfoHolder;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is BVM Ip execution interceptor API for coverage data collection.
 *
 * @since 0.985.0
 */
@JavaSPIService("org.ballerinalang.bre.InstructionHandler")
public class CoverageInstructionHandlerImpl implements InstructionHandler {

    private Map<String, List<ExecutedInstruction>> executedInstructionOrderMap;
    private Map<String, ProjectLineNumberInfoHolder> lineNumberInfoHolderForProject;
    private CoverageManager coverageManager;

    public CoverageInstructionHandlerImpl() {
        coverageManager = CoverageManager.getInstance();
        this.executedInstructionOrderMap = coverageManager.getExecutedInstructionOrderMap();
        this.lineNumberInfoHolderForProject = coverageManager.getLineNumberInfoHolderForProject();
    }

    /**
     * True when coverage reporting is enabled and should engage in handling instruction for coverage.
     *
     * @return true when coverage reporting is enabled.
     */
    public boolean shouldEngageIn() {
        return !coverageManager.isCoverageDisabled();
    }

    /**
     * Ip interceptor method to handle each Ip for the BVM for coverage data
     * collector.
     *
     * @param strand strand of the execution Ip
     */
    public void handle(Strand strand) {

        StackFrame currentStackFrame = strand.currentFrame;
        String pkgPath = currentStackFrame.callableUnitInfo.getPkgPath();
        // removing ballarina/* modules' Ips
        if (!pkgPath.startsWith(Names.BUILTIN_ORG.getValue() + Names.ORG_NAME_SEPARATOR.getValue())) {

            String entryPkgPath = strand.programFile.getEntryPackage().pkgPath;
            ProjectLineNumberInfoHolder projectLineNumberInfoHolderForPkg =
                    lineNumberInfoHolderForProject.get(entryPkgPath);

            LineNumberInfo lineNumberInfoCurrentPkg = projectLineNumberInfoHolderForPkg.getPackageInfoMap()
                    .get(pkgPath).getLineNumberInfo(currentStackFrame.ip);

            ExecutedInstruction executedInstruction = new ExecutedInstruction(currentStackFrame.ip, pkgPath,
                    lineNumberInfoCurrentPkg.getFileName(), currentStackFrame.callableUnitInfo.getName(),
                    lineNumberInfoCurrentPkg);
            if (executedInstructionOrderMap.get(entryPkgPath) != null) {
                executedInstructionOrderMap.get(entryPkgPath).add(executedInstruction);
            } else {
                List<ExecutedInstruction> executedInstructionOrder = Collections.synchronizedList(new ArrayList<>());
                executedInstructionOrder.add(executedInstruction);
                executedInstructionOrderMap.put(entryPkgPath, executedInstructionOrder);
            }

        }
    }

}
