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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;

import java.util.Map;

/**
 * A class which represents connection pool specific parameters.
 */
public class PoolConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PoolConfiguration.class);

    private static PoolConfiguration poolConfiguration;

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

    public PoolConfiguration(Map<String, Object> transportProperties) {

        numberOfPools = Util.getIntProperty(transportProperties, Constants.NUMBER_OF_POOLS, 0);

        maxActivePerPool = Util.getIntProperty(
                transportProperties, Constants.MAX_ACTIVE_CONNECTIONS_PER_POOL, -1);

        minIdlePerPool = Util.getIntProperty(
                transportProperties, Constants.MIN_IDLE_CONNECTIONS_PER_POOL, 0);

        maxIdlePerPool = Util.getIntProperty(
                transportProperties, Constants.MAX_IDLE_CONNECTIONS_PER_POOL, 100);

        minEvictableIdleTime = Util.getIntProperty(
                transportProperties, Constants.MIN_EVICTION_IDLE_TIME, 5 * 60 * 1000);

        timeBetweenEvictionRuns = Util.getIntProperty(
                transportProperties, Constants.TIME_BETWEEN_EVICTION_RUNS, 30 * 1000);

        executorServiceThreads = Util.getIntProperty(
                transportProperties, Constants.NO_THREADS_IN_EXECUTOR_SERVICE, 20);

        eventGroupExecutorThreads = Util.getIntProperty(
                transportProperties, Constants.EVENT_GROUP_EXECUTOR_THREAD_SIZE, 15);

        maxWaitTime = Util.getIntProperty(
                transportProperties, Constants.MAX_WAIT_FOR_CLIENT_CONNECTION_POOL, 60000);

        logger.debug(Constants.NUMBER_OF_POOLS + ": " + numberOfPools);
        logger.debug(Constants.MAX_ACTIVE_CONNECTIONS_PER_POOL + ":" + maxActivePerPool);
        logger.debug(Constants.MIN_IDLE_CONNECTIONS_PER_POOL + ":" + maxIdlePerPool);
        logger.debug(Constants.MAX_IDLE_CONNECTIONS_PER_POOL + ":" + maxIdlePerPool);
        logger.debug(Constants.MIN_EVICTION_IDLE_TIME + ":" + minEvictableIdleTime);
        logger.debug(Constants.NO_THREADS_IN_EXECUTOR_SERVICE + ":" + executorServiceThreads);
        logger.debug(Constants.TIME_BETWEEN_EVICTION_RUNS + ":" + timeBetweenEvictionRuns);
        logger.debug("Pool exhausted action" + ":" + exhaustedAction);
        logger.debug("Event group executor threads : " + eventGroupExecutorThreads);
    }

    public static PoolConfiguration getInstance() {
        return poolConfiguration;

    }

    public int getMaxActivePerPool() {
        return maxActivePerPool;
    }

    public int getMinIdlePerPool() {
        return minIdlePerPool;
    }

    public int getMaxIdlePerPool() {
        return maxIdlePerPool;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public long getTimeBetweenEvictionRuns() {
        return timeBetweenEvictionRuns;
    }

    public long getMinEvictableIdleTime() {
        return minEvictableIdleTime;
    }

    public byte getExhaustedAction() {
        return exhaustedAction;
    }

    public int getNumberOfPools() {
        return numberOfPools;
    }

    public int getExecutorServiceThreads() {
        return executorServiceThreads;
    }

    public int getEventGroupExecutorThreads() {
        return eventGroupExecutorThreads;
    }

    public long getMaxWait() {
        return maxWaitTime;
    }
}
