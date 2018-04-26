/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre.bvm.persistency;

import org.ballerinalang.bre.bvm.WorkerDataChannel;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerSignal;

public class DummyResponseContext implements WorkerResponseContext {
    @Override
    public WorkerExecutionContext signal(WorkerSignal signal) {
        return null;
    }

    @Override
    public WorkerExecutionContext joinTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes) {
        return null;
    }

    @Override
    public WorkerDataChannel getWorkerDataChannel(String name) {
        return null;
    }
}
