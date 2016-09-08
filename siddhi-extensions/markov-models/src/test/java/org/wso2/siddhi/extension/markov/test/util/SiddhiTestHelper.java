/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.siddhi.extension.markov.test.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SiddhiTestHelper {
    public static void waitForEvents(long sleepTime, int expectedCount, AtomicInteger actualCount, long timeout)
            throws InterruptedException {
        long currentWaitTime = 0;
        long startTime = System.currentTimeMillis();
        while ((actualCount.get() < expectedCount) && (currentWaitTime <= timeout)) {
            Thread.sleep(sleepTime);
            currentWaitTime = System.currentTimeMillis() - startTime;
        }
    }

    public static void waitForEvents(long sleepTime, int expectedSize, Collection<?> collection, long timeout)
            throws InterruptedException {
        long currentWaitTime = 0;
        long startTime = System.currentTimeMillis();
        while ((collection.size() < expectedSize) && (currentWaitTime <= timeout)) {
            Thread.sleep(sleepTime);
            currentWaitTime = System.currentTimeMillis() - startTime;
        }
    }

    public static boolean isEventsMatch(List<Object[]> actual, List<Object[]> expected) {
        if (actual.size() != expected.size()) {
            return false;
        } else {
            for (int i = 0; i < actual.size(); i++) {
                if (!Arrays.equals(actual.get(i), expected.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
