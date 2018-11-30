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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.bvm.Strand.State;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * {@code VarLock} represents lock object for variables.
 *
 * @since 0.961.0
 */
public class VarLock {

    private ArrayDeque<Strand> current;

    private ArrayDeque<Strand> waitingForLock;

    public VarLock() {
        this.current = new ArrayDeque<>();
        this.waitingForLock = new ArrayDeque<>();
    }

    public synchronized boolean lock(Strand ctx) {
        if (isLockFree() || lockedBySameContext(ctx)) {
            current.offerLast(ctx);
            return true;
        }
        waitingForLock.offerLast(ctx);
        //TODO: need to improve on state change
        BVMScheduler.stateChange(ctx, Arrays.asList(State.NEW, State.RUNNABLE), State.PAUSED);
//        BLangScheduler.workerWaitForLock(ctx);
        return false;
    }

    public synchronized void unlock() {
        //current cannot be empty as unlock cannot be called without lock being called first.
        current.removeLast();
        if (!waitingForLock.isEmpty()) {
            Strand ctx = waitingForLock.removeFirst();
            //So the lock instruction will run again
            ctx.currentFrame.ip--;
            BVMScheduler.stateChange(ctx, State.PAUSED, State.RUNNABLE);
            BVMScheduler.schedule(ctx);
        }
    }

    private boolean isLockFree() {
        return current.isEmpty();
    }

//    private boolean lockedByParentContext(Strand ctx) {
//        if (ctx.parent == null) {
//            return false;
//        }
//        return current.getLast() == ctx.parent || lockedByParentContext(ctx.parent);
//    }

    private boolean lockedBySameContext(Strand ctx) {
        return current.getLast() == ctx;
    }
}
