/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm.values.connector;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.State;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.api.BError;

/**
 * The callback implementation to handle non-blocking function behaviour.
 *
 * @since 0.995.0
 */
public class NonBlockingCallback {

    private final Strand strand;
    private final Scheduler scheduler;

    public NonBlockingCallback(Strand strand) {
        strand.blockedOnExtern = true;
        strand.setState(State.BLOCK_AND_YIELD);
        this.strand = strand;
        this.scheduler = strand.scheduler;
        this.strand.setReturnValues(null);
    }

    public void notifySuccess() {
        this.scheduler.unblockStrand(strand);
    }

    public void notifyFailure(BError error) {
        this.strand.setReturnValues(error);
        this.scheduler.unblockStrand(strand);
    }

    public void setReturnValues(Object returnValue) {
        this.strand.setReturnValues(returnValue);
    }
}
