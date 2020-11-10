/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.scheduling;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.values.FutureValue;

/**
 * The callback implementation to handle non-blocking native function behaviour.
 *
 * @since 2.0.0
 */

public abstract class AsyncFunctionCallback implements Callback {

    private FutureValue future;
    private Strand strand;

    public void setReturnValues(Object returnValue) {
        strand.returnValue = returnValue;
        strand.scheduler.unblockStrand(strand);
    }

    public void handleRuntimeErrors(BError error) {
        strand.panic = error;
        strand.scheduler.unblockStrand(strand);
    }

    public Object getFutureResult() {
        return future.result;
    }

    public void setFuture(FutureValue future) {
        this.future = future;
    }

    public void setStrand(Strand strand) {
        this.strand = strand;
    }
}
