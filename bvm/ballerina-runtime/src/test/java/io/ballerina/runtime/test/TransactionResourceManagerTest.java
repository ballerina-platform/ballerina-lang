/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.test;

import io.ballerina.runtime.transactions.BallerinaTransactionContext;
import io.ballerina.runtime.transactions.TransactionResourceManager;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import javax.transaction.xa.XAResource;

/**
 * Tests concurrent transaction resource registration and cleanup.
 */
public class TransactionResourceManagerTest {

    private static final int WORKER_COUNT = 16;
    private static final String BLOCK_ID = "block";

    @DataProvider(name = "completionModes")
    public Object[][] completionModes() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dataProvider = "completionModes")
    public void testConcurrentTransactions(boolean commit) throws Exception {
        TransactionResourceManager manager = TransactionResourceManager.getInstance();
        String prefix = UUID.randomUUID().toString();
        runConcurrently(worker -> {
            for (int iteration = 0; iteration < 1000; iteration++) {
                String transactionId = prefix + ":" + worker + ":" + iteration;
                TestTransactionContext first = new TestTransactionContext();
                TestTransactionContext second = new TestTransactionContext();
                try {
                    manager.register(transactionId, BLOCK_ID, first);
                    manager.register(transactionId, BLOCK_ID, second);
                    completeTransaction(manager, transactionId, commit);
                    assertCompleted(first, commit);
                    assertCompleted(second, commit);
                } finally {
                    manager.cleanTransaction(transactionId, BLOCK_ID);
                }
            }
        });
    }

    @Test(dataProvider = "completionModes")
    public void testConcurrentRegistrationsInSameTransaction(boolean commit) throws Exception {
        TransactionResourceManager manager = TransactionResourceManager.getInstance();
        String transactionId = UUID.randomUUID().toString();
        TestTransactionContext[][] contexts = new TestTransactionContext[WORKER_COUNT][100];
        try {
            runConcurrently(worker -> {
                for (int iteration = 0; iteration < contexts[worker].length; iteration++) {
                    TestTransactionContext context = new TestTransactionContext();
                    contexts[worker][iteration] = context;
                    manager.register(transactionId, BLOCK_ID, context);
                }
            });
            completeTransaction(manager, transactionId, commit);
            for (TestTransactionContext[] workerContexts : contexts) {
                for (TestTransactionContext context : workerContexts) {
                    assertCompleted(context, commit);
                }
            }
        } finally {
            manager.cleanTransaction(transactionId, BLOCK_ID);
        }
    }

    private static void completeTransaction(TransactionResourceManager manager, String transactionId, boolean commit) {
        boolean success = commit ? manager.notifyCommit(transactionId, BLOCK_ID)
                : manager.notifyAbort(transactionId, BLOCK_ID);
        Assert.assertTrue(success, "Transaction completion failed");
    }

    private static void assertCompleted(TestTransactionContext context, boolean commit) {
        Assert.assertEquals(context.commits.get(), commit ? 1 : 0);
        Assert.assertEquals(context.rollbacks.get(), commit ? 0 : 1);
        Assert.assertEquals(context.closes.get(), 1, "Every registered resource must be closed exactly once");
    }

    private static void runConcurrently(IntConsumer action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT,
                Thread.ofPlatform().daemon().factory());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int worker = 0; worker < WORKER_COUNT; worker++) {
                int workerId = worker;
                futures.add(executor.submit(() -> {
                    start.await();
                    action.accept(workerId);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            Assert.assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Transaction workers did not terminate");
        }
    }

    private static class TestTransactionContext implements BallerinaTransactionContext {

        private final AtomicInteger commits = new AtomicInteger();
        private final AtomicInteger rollbacks = new AtomicInteger();
        private final AtomicInteger closes = new AtomicInteger();

        @Override
        public void commit() {
            commits.incrementAndGet();
        }

        @Override
        public void rollback() {
            rollbacks.incrementAndGet();
        }

        @Override
        public void close() {
            closes.incrementAndGet();
        }

        @Override
        public XAResource getXAResource() {
            return null;
        }
    }
}
