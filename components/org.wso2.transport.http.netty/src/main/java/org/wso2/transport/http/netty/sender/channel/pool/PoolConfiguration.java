/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.transport.http.netty.sender.channel.pool;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * A class which represents connection pool specific parameters.
 */
public class PoolConfiguration {

    private int maxActivePerPool = -1;
    private int minIdlePerPool;
    private int maxIdlePerPool = 100;
    private boolean testOnBorrow = true;
    private boolean testWhileIdle = true;
    private long timeBetweenEvictionRuns = 30 * 1000L;
    private long minEvictableIdleTime = 5 * 60 * 1000L;
    private byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
    private int numberOfPools = 0;
    private int executorServiceThreads = 20;
    private int eventGroupExecutorThreads = 15;
    private long maxWaitTime = 60000L;
    private int http2MaxActiveStreamsPerConnection = Integer.MAX_VALUE;

    public PoolConfiguration() {
    }

    public int getMaxActivePerPool() {
        return maxActivePerPool;
    }

    public void setMaxActivePerPool(int maxActivePerPool) {
        this.maxActivePerPool = maxActivePerPool;
    }

    public int getMinIdlePerPool() {
        return minIdlePerPool;
    }

    public void setMinIdlePerPool(int minIdlePerPool) {
        this.minIdlePerPool = minIdlePerPool;
    }

    public int getMaxIdlePerPool() {
        return maxIdlePerPool;
    }

    public void setMaxIdlePerPool(int maxIdlePerPool) {
        this.maxIdlePerPool = maxIdlePerPool;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public long getTimeBetweenEvictionRuns() {
        return timeBetweenEvictionRuns;
    }

    public void setTimeBetweenEvictionRuns(long timeBetweenEvictionRuns) {
        this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
    }

    public long getMinEvictableIdleTime() {
        return minEvictableIdleTime;
    }

    public void setMinEvictableIdleTime(long minEvictableIdleTime) {
        this.minEvictableIdleTime = minEvictableIdleTime;
    }

    public byte getExhaustedAction() {
        return exhaustedAction;
    }

    public void setExhaustedAction(byte exhaustedAction) {
        this.exhaustedAction = exhaustedAction;
    }

    public int getNumberOfPools() {
        return numberOfPools;
    }

    public void setNumberOfPools(int numberOfPools) {
        this.numberOfPools = numberOfPools;
    }

    public int getExecutorServiceThreads() {
        return executorServiceThreads;
    }

    public void setExecutorServiceThreads(int executorServiceThreads) {
        this.executorServiceThreads = executorServiceThreads;
    }

    public int getEventGroupExecutorThreads() {
        return eventGroupExecutorThreads;
    }

    public void setEventGroupExecutorThreads(int eventGroupExecutorThreads) {
        this.eventGroupExecutorThreads = eventGroupExecutorThreads;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public int getHttp2MaxActiveStreamsPerConnection() {
        return http2MaxActiveStreamsPerConnection;
    }

    public void setHttp2MaxActiveStreamsPerConnection(int http2MaxActiveStreamsPerConnection) {
        this.http2MaxActiveStreamsPerConnection = http2MaxActiveStreamsPerConnection;
    }
}
