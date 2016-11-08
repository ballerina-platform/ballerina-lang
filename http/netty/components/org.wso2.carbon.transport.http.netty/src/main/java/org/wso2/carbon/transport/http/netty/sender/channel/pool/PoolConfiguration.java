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

package org.wso2.carbon.transport.http.netty.sender.channel.pool;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;

import java.util.Iterator;
import java.util.Set;

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

    private byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;

    private int numberOfPools = 0;

    private int executorServiceThreads = 20;

    private PoolConfiguration(Set<TransportProperty> transportPropertySet) {

        if (transportPropertySet != null && !transportPropertySet.isEmpty()) {
            Iterator iterator = transportPropertySet.iterator();
            while (iterator.hasNext()) {
                TransportProperty transportProperty = (TransportProperty) iterator.next();
                if (transportProperty.getName().equals(Constants.NUMBER_OF_POOLS)) {
                    numberOfPools = (Integer) transportProperty.getValue();
                } else if (transportProperty.getName().equals(Constants.MAX_ACTIVE_CONNECTIONS_PER_POOL)) {
                    maxActivePerPool = (Integer) transportProperty.getValue();
                } else if (transportProperty.getName().equals(Constants.MIN_IDLE_CONNECTIONS_PER_POOL)) {
                    minIdlePerPool = (Integer) transportProperty.getValue();
                } else if (transportProperty.getName().equals(Constants.MAX_IDLE_CONNECTIONS_PER_POOL)) {
                    maxIdlePerPool = (Integer) transportProperty.getValue();
                } else if (transportProperty.getName().equals(Constants.MIN_EVICTION_IDLE_TIME)) {
                    minEvictableIdleTime = (Integer) transportProperty.getValue();
                } else if (transportProperty.getName().equals(Constants.NO_THREADS_IN_EXECUTOR_SERVICE)) {
                    executorServiceThreads = (Integer) transportProperty.getValue();
                }
            }
        }

        logger.debug(Constants.NUMBER_OF_POOLS + ": " + numberOfPools);
        logger.debug(Constants.MAX_ACTIVE_CONNECTIONS_PER_POOL + ":" + maxActivePerPool);
        logger.debug(Constants.MIN_IDLE_CONNECTIONS_PER_POOL + ":" + maxIdlePerPool);
        logger.debug(Constants.MAX_IDLE_CONNECTIONS_PER_POOL + ":" + maxIdlePerPool);
        logger.debug(Constants.MIN_EVICTION_IDLE_TIME + ":" + minEvictableIdleTime);
        logger.debug(Constants.NO_THREADS_IN_EXECUTOR_SERVICE + ":" + executorServiceThreads);
        logger.debug("Time between Evictions Runs" + ":" + timeBetweenEvictionRuns);
        logger.debug("Pool exhausted action" + ":" + exhaustedAction);
    }

    public static PoolConfiguration getInstance() {
        return poolConfiguration;

    }

    public static void createPoolConfiguration(Set<TransportProperty> transportPropertySet) {
        poolConfiguration = new PoolConfiguration(transportPropertySet);
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
}
