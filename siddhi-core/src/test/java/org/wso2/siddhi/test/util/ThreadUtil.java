/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.siddhi.test.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ThreadUtil {
    private static ThreadGroup rootThreadGroup = null;

    public static ThreadGroup getRootThreadGroup() {
        if (rootThreadGroup != null) {
            return rootThreadGroup;
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        ThreadGroup ptg;
        while ((ptg = tg.getParent()) != null) {
            tg = ptg;
        }
        return tg;
    }

    public static List<ThreadGroup> getAllThreadGroups() {
        final ThreadGroup root = ThreadUtil.getRootThreadGroup();
        int allocatedTgCount = root.activeGroupCount();
        int enumeratedTgCount = 0;
        ThreadGroup[] groups;
        do {
            allocatedTgCount *= 2;
            groups = new ThreadGroup[allocatedTgCount];
            enumeratedTgCount = root.enumerate(groups, true);
        } while (enumeratedTgCount == allocatedTgCount);

        List<ThreadGroup> allGroups = new ArrayList<ThreadGroup>(enumeratedTgCount + 1);
        allGroups.add(root);
        for (ThreadGroup threadGroup : groups) {
            if (threadGroup != null) {
                allGroups.add(threadGroup);
            }
        }
        return allGroups;
    }

    public static ThreadGroup getThreadGroup(String name) {
        if (name == null)
            throw new java.lang.IllegalArgumentException("Thread group name cannot be null");
        final List<ThreadGroup> groups = ThreadUtil.getAllThreadGroups();
        for (ThreadGroup group : groups) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public static List<ThreadGroup> getThreadGroupStartingWith(String name) {
        if (name == null)
            throw new IllegalArgumentException("Thread group name cannot be null");
        final List<ThreadGroup> groups = ThreadUtil.getAllThreadGroups();
        List<ThreadGroup> matchingGroups = new ArrayList<ThreadGroup>();
        for (ThreadGroup group : groups) {
            if (group.getName() != null && group.getName().startsWith(name)) {
                matchingGroups.add(group);
            }
        }
        return matchingGroups;
    }

    public static List<Thread> getAllThreads() {
        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        int n = 0;
        List<Thread> threadList = new ArrayList<Thread>();
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        Collections.addAll(threadList, Arrays.copyOf(threads, n));
        return threadList;
    }

    public static List<Thread> getThreadsStartingWith(String name) {
        if (name == null)
            throw new IllegalArgumentException("Thread name cannot be null");
        final List<Thread> threads = ThreadUtil.getAllThreads();
        List<Thread> matchingThreads = new ArrayList<Thread>();
        for (Thread thread : threads) {
            if (thread.getName() != null && thread.getName().startsWith(name)) {
                matchingThreads.add(thread);
            }
        }
        return matchingThreads;
    }

}
