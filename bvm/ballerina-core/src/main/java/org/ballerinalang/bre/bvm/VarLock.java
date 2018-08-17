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

import java.util.ArrayDeque;

/**
 * {@code VarLock} represents lock object for variables.
 *
 * @since 0.961.0
 */
public class VarLock {

    private ArrayDeque<WorkerExecutionContext> current;

    private ArrayDeque<WorkerExecutionContext> waitingForLock;

    public VarLock() {
        this.current = new ArrayDeque<>();
        this.waitingForLock = new ArrayDeque<>();
    }

    public synchronized boolean lock(WorkerExecutionContext ctx) {
        if (isLockFree() || lockedBySameContext(ctx) || lockedByParentContext(ctx)) {
            current.offerLast(ctx);
            return true;
        }
        waitingForLock.offerLast(ctx);
        BLangScheduler.workerWaitForLock(ctx);
        return false;
    }

    public synchronized void unlock() {
        //current cannot be empty as unlock cannot be called without lock being called first.
        current.removeLast();
        if (!waitingForLock.isEmpty()) {
            WorkerExecutionContext ctx = waitingForLock.removeFirst();
            BLangScheduler.resume(ctx, ctx.ip - 1, false);
        }
    }

    private boolean isLockFree() {
        return current.isEmpty();
    }

    private boolean lockedByParentContext(WorkerExecutionContext ctx) {
        if (ctx.parent == null) {
            return false;
        }
        return current.getLast() == ctx.parent || lockedByParentContext(ctx.parent);
    }

    private boolean lockedBySameContext(WorkerExecutionContext ctx) {
        return current.getLast() == ctx;
    }
}
