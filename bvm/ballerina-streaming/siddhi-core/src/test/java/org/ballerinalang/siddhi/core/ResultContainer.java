/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.siddhi.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Test util class to hold results.
 */
public class ResultContainer {
    private static final Logger log = LoggerFactory.getLogger(ResultContainer.class);
    private CountDownLatch latch;
    private List<ThrottleInfoHolder> results;

    public ResultContainer(int size) {
        latch = new CountDownLatch(size);
        results = Collections.synchronizedList(new ArrayList<ThrottleInfoHolder>(size));
    }

    public void addResult(String rule, Boolean result) {
        results.add(new ThrottleInfoHolder(rule, result));
        latch.countDown();
    }

    /**
     * Wait for other threads to post results and then return aggregated result.
     *
     * @return isThrottled
     * @throws InterruptedException throw exception if interrupted.
     */
    public Boolean isThrottled() throws InterruptedException {
        this.latch.await();
        for (ThrottleInfoHolder throttleInfoHolder : results) {
            if (throttleInfoHolder.result) {
                return true;
            }
        }
        return false;
    }

    class ThrottleInfoHolder {
        private boolean result;
        private String rule;

        public ThrottleInfoHolder(String rule, Boolean result) {
            this.rule = rule;
            this.result = result;
        }
    }
}
