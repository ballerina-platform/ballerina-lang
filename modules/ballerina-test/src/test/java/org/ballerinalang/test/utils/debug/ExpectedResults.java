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

import java.util.ArrayList;
import java.util.List;

/**
 * Test Util class to hold expected results.
 *
 * @since 0.96
 */
public class ExpectedResults {

    private List<WorkerResults> workerResults;

    private WorkerResults current;

    private boolean multiThreaded;

    public ExpectedResults(WorkerResults current, boolean multiThreaded) {
        this.workerResults = new ArrayList<>();
        this.workerResults.add(current);
        this.current = current;
        this.multiThreaded = multiThreaded;
    }

    public boolean checkDebugSuccess() {
        for (WorkerResults r : workerResults) {
            if (!r.allPointsHit()) {
                return false;
            }
        }
        return true;
    }

    public void addWorkerResults(WorkerResults workerResults) {
        this.workerResults.add(workerResults);
    }

    public List<WorkerResults> getWorkerResults() {
        return workerResults;
    }

    public void setWorkerResults(List<WorkerResults> workerResults) {
        this.workerResults = workerResults;
    }

    public WorkerResults getCurrent() {
        return current;
    }

    public void setCurrent(WorkerResults current) {
        this.current = current;
    }

    public boolean isMultiThreaded() {
        return multiThreaded;
    }
}
