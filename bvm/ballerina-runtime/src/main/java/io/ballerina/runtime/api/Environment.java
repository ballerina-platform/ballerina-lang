/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api;

import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.State;
import io.ballerina.runtime.scheduling.Strand;

/**
 * When this class is used as the first argument of an interop method, Ballerina
 * will inject an instance of the class when calling. That instance can be used to
 * communicate with currently executing Ballerina runtime.
 *
 * @since 2.0.0
 */
public class Environment {
    private Strand strand;

    public Environment(Strand strand) {
        this.strand = strand;
    }

    /**
     * Mark the current executing strand as async. Execution of Ballerina code after the current
     * interop will stop until given BalFuture is completed. However the java thread will not be blocked
     * and will be reused for running other Ballerina code in the meantime. Therefor callee of this method
     * must return as soon as possible to avoid starvation of ballerina code execution.
     *
     * @return BalFuture which will resume the current strand when completed.
     */
    public Future markAsync() {
        Strand strand = Scheduler.getStrand();
        strand.blockedOnExtern = true;
        strand.setState(State.BLOCK_AND_YIELD);
        return new Future(this.strand);
    }

    public Runtime getRuntime() {
        return new Runtime(strand.scheduler);
    }
}
