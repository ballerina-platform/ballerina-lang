/*
 * Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.runtime.internal.scheduling;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a group of {@link SchedulerItem} that should run on same thread.
 *
 * @since 0.995.0
 */
public class ItemGroup {

    private static final AtomicInteger nextItemGroupId = new AtomicInteger(0);

    private final int id;

    /**
     * Keep the list of items that should run on same thread.
     * Using a stack to get advantage of the locality.
     */
    Stack<SchedulerItem> items = new Stack<>();

    /**
     * Indicates this item is already in runnable list/executing or not.
     */
    AtomicBoolean scheduled = new AtomicBoolean(false);

    private final ReentrantLock groupLock = new ReentrantLock();

    public static final ItemGroup POISON_PILL = new ItemGroup();

    public ItemGroup(SchedulerItem item) {
        this();
        items.push(item);
    }

    public ItemGroup() {
        this.id = nextItemGroupId.incrementAndGet();
    }

    public void add(SchedulerItem item) {
        items.push(item);
    }

    public SchedulerItem get() {
        return items.pop();
    }

    public void lock() {
        this.groupLock.lock();
    }

    public void unlock() {
        this.groupLock.unlock();
    }

    public int getId() {
        return id;
    }

    public static int getCreatedStrandGroupCount() {
        return nextItemGroupId.get();
    }

    public boolean isScheduled() {
        return scheduled.get();
    }
}
