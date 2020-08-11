/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cache Manager class handles and maintains an LRU cache which implements ManageableCache Interface.
 */
public class CacheManager {

    private static final boolean DO_NOT_INTERRUPT_IF_RUNNING = false;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture scheduledFuture = null;
    private ManageableCache cache;
    private int cacheMaxSize;
    private int delay;
    private CacheManagingTask cacheManagingTask;
    private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);

    /**
     * A new cacheManager will be started on the given ManageableCache object.
     *
     * @param cache Manageable cache which can be managed by this cache manager.
     * @param cacheMaxSize Maximum size of the cache. If the cache exceeds this size, LRU values will be
     *                     removed
     * @param delay Cache delay.
     */
    public CacheManager(ManageableCache cache, int cacheMaxSize, int delay) {
        int numThreads = 1;
        scheduler = Executors.newScheduledThreadPool(numThreads);
        this.cache = cache;
        this.cacheMaxSize = cacheMaxSize;
        this.cacheManagingTask = new CacheManagingTask();
        this.delay = delay;
        start();
    }

    /**
     * To Start the CacheManager, it needs to be called only once. Because of that calls in constructor.
     * CacheManager will run its TimerTask every "delay" number of seconds.
     */
    private boolean start() {
        if (scheduledFuture == null || scheduledFuture.isCancelled()) {
            scheduledFuture = scheduler.scheduleWithFixedDelay(cacheManagingTask, delay, delay, TimeUnit.MINUTES);
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} Cache Manager Started.", cache.getClass().getSimpleName());
            }
            return true;
        }
        return false;
    }

    /**
     * To wake cacheManager up at will. If this method is called while its task is running, it will run its task again
     * soon after it is done. CacheManagerTask will be rescheduled as before.
     *
     * @return true if successfully waken up. false otherwise.
     */
    public boolean wakeUpNow() {
        if (scheduledFuture != null) {
            if (!scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(DO_NOT_INTERRUPT_IF_RUNNING);
            }
            scheduledFuture = scheduler.scheduleWithFixedDelay(cacheManagingTask, 0, delay, TimeUnit.MINUTES);
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} Cache Manager woke up.", cache.getClass().getSimpleName());
            }
            return true;
        }
        return false;
    }

    public boolean changeDelay(int delay) {
        int min = Constants.CACHE_MIN_DELAY_MINS;
        int max = Constants.CACHE_MAX_DELAY_MINS;
        if (delay < min || delay > max) {
            throw new IllegalArgumentException(
                    "Delay time should should be between " + min + " and " + max + " minutes");
        }
        this.delay = delay;
        return wakeUpNow();
    }

    public int getDelay() {
        return delay;
    }

    /**
     * Gracefully stop cacheManager.
     * @return true if successfully cancel the scheduled future.
     */
    public boolean stop() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(DO_NOT_INTERRUPT_IF_RUNNING);
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} Cache Manager stopped.", cache.getClass().getSimpleName());
            }
            return true;
        }
        return false;
    }

    public boolean isRunning() {
        return !scheduledFuture.isCancelled();
    }

    /**
     * This is the scheduled task which the CacheManager uses in order to remove invalid cache values and
     * to remove LRU values if the cache reaches cacheMaxSize.
     */
    private class CacheManagingTask implements Runnable {

        public void run() {

            long start = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} Cache Manager Task Started.", cache.getClass().getSimpleName());
            }

            ManageableCacheValue nextCacheValue;
            //cache.getCacheSize() can vary when new entries are added. So get cache size at this point
            int cacheSize = cache.getCacheSize();
            int numberToRemove = (cacheSize > cacheMaxSize) ? cacheSize - cacheMaxSize : 0;

            List<ManageableCacheValue> entriesToRemove = new ArrayList<>();
            LRUEntryCollector lruEntryCollector = new LRUEntryCollector(entriesToRemove, numberToRemove);

            //Start looking at cache entries from the beginning.
            cache.resetIterator();
            //Iteration through the cache entries.
            while ((cacheSize--) > 0) {

                nextCacheValue = cache.getNextCacheValue();
                if (nextCacheValue == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Cache manager iteration through Cache values done");
                    }
                    break;
                }

                //Updating invalid cache values.
                if (!nextCacheValue.isValid()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Updating Invalid Cache Value by Manager");
                    }
                    nextCacheValue.updateCacheWithNewValue();
                }

                //There are LRU entries to be removed since cacheSize > maxCacheSize. So collect them.
                if (numberToRemove > 0) {
                    lruEntryCollector.collectEntriesToRemove(nextCacheValue);
                }
            }

            //LRU entries removing
            for (ManageableCacheValue oldCacheValue : entriesToRemove) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing LRU value from cache");
                }
                oldCacheValue.removeThisCacheValue();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(cache.getClass().getSimpleName() + " Cache Manager Task Done. Took " + (
                        System.currentTimeMillis() - start) + " ms.");
            }
        }

        private class LRUEntryCollector {

            private List<ManageableCacheValue> entriesToRemove;
            private int listMaxSize;

            LRUEntryCollector(List<ManageableCacheValue> entriesToRemove, int numberToRemove) {
                this.entriesToRemove = entriesToRemove;
                this.listMaxSize = numberToRemove;
            }

            /**
             * This method collects the listMaxSize number of LRU values from the cache. These values
             * will be removed from the cache. This uses a part of the logic in Insertion Sort.
             *
             * @param value to be collected.
             */
            private void collectEntriesToRemove(ManageableCacheValue value) {

                entriesToRemove.add(value);
                int j = entriesToRemove.size() - 1;
                for (; j > 0 && (value.getTimeStamp() < entriesToRemove.get(j - 1).getTimeStamp()); j--) {
                    entriesToRemove.remove(j);
                    entriesToRemove.add(j, (entriesToRemove.get(j - 1)));
                }
                entriesToRemove.remove(j);
                entriesToRemove.add(j, value);
                /*
                  First entry in the list will be the oldest. Last is the earliest in the list.
                  So remove the earliest since we need to collect the old (LRU) values to remove
                  from cache later
                 */
                if (entriesToRemove.size() > listMaxSize) {
                    entriesToRemove.remove(entriesToRemove.size() - 1);
                }
            }
        }
    }
}
