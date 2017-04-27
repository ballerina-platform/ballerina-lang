/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.siddhi.extension.input.transport.jms.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a custom implementation of the Threadpool executor to allow pausing and resuming of the thread pool.
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {
    private boolean paused;
    private ReentrantLock lock;
    private Condition condition;

    public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                      BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        lock.lock();
        try {
            while (paused) condition.await();
        } catch (InterruptedException ie) {
            thread.interrupt();
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Pause the execution.
     */
    public void pause() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resume pool execution.
     */
    public void resume() {
        lock.lock();
        try {
            paused = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
