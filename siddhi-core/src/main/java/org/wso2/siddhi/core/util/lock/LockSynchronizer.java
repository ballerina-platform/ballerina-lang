/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.util.lock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class maintains a {@link Map} of {@link LockWrapper}s and sync the locks among
 * {@link LockWrapper}s if there is a dependency between them.
 */
public class LockSynchronizer {
    /**
     * Adjacent list of depended {@link LockWrapper}s.
     */
    private Map<LockWrapper, List<LockWrapper>> holderMap = new HashMap<LockWrapper, List<LockWrapper>>();

    /**
     * Ensure that both {@link LockWrapper}s are sharing the same lock and also update the underneath
     * lock of all the depended {@link LockWrapper}s of both.
     *
     * @param lockWrapperOne lock wrapper one
     * @param lockWrapperTwo lock wrapper two
     */
    public void sync(LockWrapper lockWrapperOne, LockWrapper lockWrapperTwo) {
        boolean leftHolderExists = holderMap.containsKey(lockWrapperOne);
        boolean rightHolderExists = holderMap.containsKey(lockWrapperTwo);

        if (leftHolderExists && !rightHolderExists) {
            // Share the lock of lockWrapperOne holder to the lockWrapperTwo
            lockWrapperTwo.setLock(lockWrapperOne.getLock());

            // Create a new holder list for lockWrapperTwo
            List<LockWrapper> connectedLockWrappers = new ArrayList<LockWrapper>();

            // Add the lockWrapperOne as one of the link to lockWrapperTwo
            connectedLockWrappers.add(lockWrapperOne);

            // Put the lockWrapperTwo into the map
            holderMap.put(lockWrapperTwo, connectedLockWrappers);

            // Add the lockWrapperTwo as a link to the lockWrapperOne
            holderMap.get(lockWrapperOne).add(lockWrapperTwo);
        } else if (!leftHolderExists && rightHolderExists) {
            // Share the lock of lockWrapperTwo holder to the lockWrapperOne
            lockWrapperOne.setLock(lockWrapperTwo.getLock());

            // Create a new holder list for lockWrapperTwo
            List<LockWrapper> connectedLockWrappers = new ArrayList<LockWrapper>();

            // Add the lockWrapperTwo as one of the link to lockWrapperOne
            connectedLockWrappers.add(lockWrapperTwo);

            // Put the lockWrapperOne into the map
            holderMap.put(lockWrapperOne, connectedLockWrappers);

            // Add the lockWrapperOne as a link to the lockWrapperTwo
            holderMap.get(lockWrapperTwo).add(lockWrapperOne);
        } else if (!leftHolderExists && !rightHolderExists) {
            // Create a new lock and share it
            ReentrantLock lock = new ReentrantLock();
            lockWrapperOne.setLock(lock);
            lockWrapperTwo.setLock(lock);

            // Create a new holder list for lockWrapperTwo
            List<LockWrapper> connectedWrappersOfOne = new ArrayList<LockWrapper>();
            // Create a new holder list for lockWrapperTwo
            List<LockWrapper> connectedWrappersOfTwo = new ArrayList<LockWrapper>();

            // Add the lockWrapperTwo as one of the link to lockWrapperOne
            connectedWrappersOfOne.add(lockWrapperTwo);
            // Add the lockWrapperOne as one of the link to lockWrapperTwo
            connectedWrappersOfTwo.add(lockWrapperOne);


            holderMap.put(lockWrapperOne, connectedWrappersOfOne);
            holderMap.put(lockWrapperTwo, connectedWrappersOfTwo);
        } else {
            // Both exists
            ReentrantLock lockOfWrapperOne = lockWrapperOne.getLock();

            // Update the lockWrapperOne lock to the lockWrapperTwo and all of its links
            lockWrapperTwo.setLock(lockOfWrapperOne);
            List<LockWrapper> rightList = holderMap.get(lockWrapperTwo);
            for (LockWrapper holder : rightList) {
                holder.setLock(lockOfWrapperOne);
            }
        }
    }
}
