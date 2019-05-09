/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.utils.debug;

import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.VariableDTO;

import java.util.List;

/**
 * Test Util class to hold expected results.
 *
 * @since 0.96
 */
public class ExpectedResults {

    private List<DebugPoint> debugPoints;
    private int expDebugPointsCount;

    private int expGlobalVarCount;

    private List<VariableDTO> expVariables;
    private boolean exactVarMatchRequired;

    public ExpectedResults(List<DebugPoint> debugPoints, int expDebugPointsCount, int expGlobalVarCount,
                           List<VariableDTO> expVariables, boolean exactVarMatchRequired) {
        this.debugPoints = debugPoints;
        this.expDebugPointsCount = expDebugPointsCount;
        this.expGlobalVarCount = expGlobalVarCount;
        this.expVariables = expVariables;
        this.exactVarMatchRequired = exactVarMatchRequired;
    }

    DebugPoint getDebugHit(BreakPointDTO breakPoint) {
        for (DebugPoint debugPoint : debugPoints) {
            if (debugPoint.match(breakPoint)) {
                return debugPoint;
            }
        }
        return null;
    }

    int getDebugCount() {
        return expDebugPointsCount;
    }

    public int getExpGlobalVarCount() {
        return expGlobalVarCount;
    }

    public List<VariableDTO> getExpVariables() {
        return expVariables;
    }

    public boolean isExactVarMatchRequired() {
        return exactVarMatchRequired;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        debugPoints.forEach(v -> builder.append(v).append("\n"));
        return builder.toString();
    }

}
