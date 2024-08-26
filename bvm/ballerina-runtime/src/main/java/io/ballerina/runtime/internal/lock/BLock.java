/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

package io.ballerina.runtime.internal.lock;

import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents lock object for variables in jvm.
 *
 * @since 2201.11.0
 */
public class BLock extends ReentrantLock {

    public void lock(Strand strand) {
        super.lock();
        strand.acquiredLockCount++;

    }

    public void unlock(Strand strand) {
        super.unlock();
        strand.acquiredLockCount--;
    }

}
