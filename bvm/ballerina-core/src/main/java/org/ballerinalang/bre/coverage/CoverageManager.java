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
package org.ballerinalang.bre.coverage;

import org.ballerinalang.bre.coverage.impl.CoverageInstructionHandlerImpl;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoverageManager {

    private static final Map<String, List<ExecutedInstruction>> executedInstructionOrderMap = new ConcurrentHashMap<>();

    private static final CoverageManager coverageManger = new CoverageManager();

    private static InstructionHandler coverageInstructionHandler;

    private CoverageManager() {}

    public static CoverageManager getInstance() {
        return coverageManger;
    }

    public static InstructionHandler getCoverageInstructionHandler(ProgramFile programFile) {
        if(coverageInstructionHandler == null) {
            coverageInstructionHandler = new CoverageInstructionHandlerImpl(executedInstructionOrderMap, programFile);
        }

        return coverageInstructionHandler;
    }

    public static Map<String, List<ExecutedInstruction>> getExecutedInstructionOrderMap() {
        return executedInstructionOrderMap;
    }
}
