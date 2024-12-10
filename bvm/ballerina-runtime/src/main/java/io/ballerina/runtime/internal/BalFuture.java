/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A future that will resume the underling strand when completed.
 *
 * @since 2201.6.0
 */
public class BalFuture extends Future {
    private final Strand strand;
    private final AtomicBoolean visited = new AtomicBoolean();

    public BalFuture(Strand strand) {
        this.strand = strand;
    }

    public void complete(Object returnValue) {
        if (visited.getAndSet(true)) {
            throw new IllegalStateException("cannot complete the same future twice.");
        }
        strand.returnValue = returnValue;
        strand.scheduler.unblockStrand(strand);
    }
}
