/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.internal.scheduling.State;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.ArrayDeque;

/**
 * {@code VarLock} represents lock object for variables in jvm.
 *
 * @since 1.0.0
 */
public class BLock {

    private ArrayDeque<Strand> current;

    private ArrayDeque<Strand> waitingForLock;

    public BLock() {
        this.current = new ArrayDeque<>();
        this.waitingForLock = new ArrayDeque<>();
    }

    public synchronized boolean lock(Strand strand) {
        if (isLockFree() || lockedBySameContext(strand)) {
            this.current.offerLast(strand);
            strand.acquiredLockCount++;
            return true;
        }

        this.waitingForLock.offerLast(strand);

        // Strand state change
        strand.setState(State.BLOCK_AND_YIELD);
        strand.blockedOnExtern = false;
        return false;
    }

    public synchronized void unlock() {
        //current cannot be empty as unlock cannot be called without lock being called first.
        Strand removedStrand = this.current.removeLast();
        removedStrand.acquiredLockCount--;
        if (!waitingForLock.isEmpty()) {
            Strand strand = this.waitingForLock.removeFirst();
            strand.scheduler.unblockStrand(strand);
        }
    }

    public boolean isLockFree() {
        return this.current.isEmpty();
    }

    public boolean lockedBySameContext(Strand ctx) {
        return this.current.getLast() == ctx;
    }
}
