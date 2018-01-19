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

/**
 * Test Util class to hold expected worker results.
 *
 * @since 0.96
 */
public class WorkerResults {

    private BreakPointDTO[] expBreakPoints;

    private Step[] steps;

    private int pointer = 0;

    public WorkerResults(BreakPointDTO[] expBreakPoints, Step... steps) {
        this.expBreakPoints = expBreakPoints;
        this.steps = steps;
    }

    public boolean allPointsHit() {
        return pointer == expBreakPoints.length;
    }

    public BreakPointDTO getCurrentLocation() {
        if (expBreakPoints.length <= pointer) {
            return null;
        }
        return expBreakPoints[pointer];
    }

    public Step getNextStep() {
        return steps[pointer - 1];
    }

    public void incrementPointer() {
        pointer++;
    }
}
