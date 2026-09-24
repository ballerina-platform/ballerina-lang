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
import java.util.Collections;
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

    @Test
    public void testRegisterRacingWithCleanTransaction() throws Exception {
        TransactionResourceManager manager = TransactionResourceManager.getInstance();
        for (int i = 0; i < 50; i++) {
            String transactionId = UUID.randomUUID().toString();
            List<TestTransactionContext> contexts = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT + 1);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            try {
                for (int worker = 0; worker < WORKER_COUNT; worker++) {
                    futures.add(executor.submit(() -> {
                        start.await();
                        for (int iter = 0; iter < 50; iter++) {
                            TestTransactionContext ctx = new TestTransactionContext();
                            contexts.add(ctx);
                            manager.register(transactionId, BLOCK_ID, ctx);
                        }
                        return null;
                    }));
                }
                futures.add(executor.submit(() -> {
                    start.await();
                    Thread.yield();
                    manager.cleanTransaction(transactionId, BLOCK_ID);
                    return null;
                }));
                start.countDown();
                for (Future<?> f : futures) {
                    f.get(30, TimeUnit.SECONDS);
                }
            } finally {
                executor.shutdownNow();
                manager.cleanTransaction(transactionId, BLOCK_ID);
            }
            // All contexts must be closed exactly once (no detached unclosed contexts)
            for (TestTransactionContext ctx : contexts) {
                Assert.assertEquals(ctx.closes.get(), 1, "Context must be closed exactly once");
            }
        }
    }

    @Test
    public void testRegisterRacingWithNotifyCommit() throws Exception {
        TransactionResourceManager manager = TransactionResourceManager.getInstance();
        for (int i = 0; i < 50; i++) {
            String transactionId = UUID.randomUUID().toString();
            List<RegistrationResult> results = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT + 1);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            try {
                for (int worker = 0; worker < WORKER_COUNT; worker++) {
                    futures.add(executor.submit(() -> {
                        start.await();
                        for (int iter = 0; iter < 50; iter++) {
                            TestTransactionContext ctx = new TestTransactionContext();
                            boolean accepted = manager.register(transactionId, BLOCK_ID, ctx);
                            results.add(new RegistrationResult(ctx, accepted));
                        }
                        return null;
                    }));
                }
                futures.add(executor.submit(() -> {
                    start.await();
                    Thread.yield();
                    manager.notifyCommit(transactionId, BLOCK_ID);
                    return null;
                }));
                start.countDown();
                for (Future<?> f : futures) {
                    f.get(30, TimeUnit.SECONDS);
                }
            } finally {
                executor.shutdownNow();
                manager.cleanTransaction(transactionId, BLOCK_ID);
            }
            for (RegistrationResult result : results) {
                Assert.assertEquals(result.context.closes.get(), 1, "Context must be closed exactly once");
                if (result.accepted) {
                    Assert.assertEquals(result.context.commits.get(), 1, "Accepted context must be committed");
                } else {
                    Assert.assertEquals(result.context.commits.get(), 0, "Rejected context must not be committed");
                }
            }
        }
    }

    @Test
    public void testRegisterRacingWithNotifyAbort() throws Exception {
        TransactionResourceManager manager = TransactionResourceManager.getInstance();
        for (int i = 0; i < 50; i++) {
            String transactionId = UUID.randomUUID().toString();
            List<RegistrationResult> results = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT + 1);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            try {
                for (int worker = 0; worker < WORKER_COUNT; worker++) {
                    futures.add(executor.submit(() -> {
                        start.await();
                        for (int iter = 0; iter < 50; iter++) {
                            TestTransactionContext ctx = new TestTransactionContext();
                            boolean accepted = manager.register(transactionId, BLOCK_ID, ctx);
                            results.add(new RegistrationResult(ctx, accepted));
                        }
                        return null;
                    }));
                }
                futures.add(executor.submit(() -> {
                    start.await();
                    Thread.yield();
                    manager.notifyAbort(transactionId, BLOCK_ID);
                    return null;
                }));
                start.countDown();
                for (Future<?> f : futures) {
                    f.get(30, TimeUnit.SECONDS);
                }
            } finally {
                executor.shutdownNow();
                manager.cleanTransaction(transactionId, BLOCK_ID);
            }
            for (RegistrationResult result : results) {
                Assert.assertEquals(result.context.closes.get(), 1, "Context must be closed exactly once");
                if (result.accepted) {
                    Assert.assertEquals(result.context.rollbacks.get(), 1, "Accepted context must be rolled back");
                } else {
                    Assert.assertEquals(result.context.rollbacks.get(), 0, "Rejected context must not be rolled back");
                }
            }
        }
    }

    private record RegistrationResult(TestTransactionContext context, boolean accepted) {
    }

    @Test
    public void testRegistrationRejectedAfterCompletion() {
        TransactionResourceManager manager = TransactionResourceManager.getInstance();
        String tx1 = UUID.randomUUID().toString();
        TestTransactionContext ctx1 = new TestTransactionContext();
        manager.register(tx1, BLOCK_ID, ctx1);
        manager.notifyCommit(tx1, BLOCK_ID);
        TestTransactionContext lateCtx1 = new TestTransactionContext();
        boolean accepted1 = manager.register(tx1, BLOCK_ID, lateCtx1);
        Assert.assertFalse(accepted1, "Registration after notifyCommit must be rejected");
        Assert.assertEquals(lateCtx1.closes.get(), 1, "Rejected context must be closed immediately");
        manager.cleanTransaction(tx1, BLOCK_ID);

        String tx2 = UUID.randomUUID().toString();
        TestTransactionContext ctx2 = new TestTransactionContext();
        manager.register(tx2, BLOCK_ID, ctx2);
        manager.notifyAbort(tx2, BLOCK_ID);
        TestTransactionContext lateCtx2 = new TestTransactionContext();
        boolean accepted2 = manager.register(tx2, BLOCK_ID, lateCtx2);
        Assert.assertFalse(accepted2, "Registration after notifyAbort must be rejected");
        Assert.assertEquals(lateCtx2.closes.get(), 1, "Rejected context must be closed immediately");

        String tx3 = UUID.randomUUID().toString();
        TestTransactionContext ctx3 = new TestTransactionContext();
        manager.register(tx3, BLOCK_ID, ctx3);
        manager.cleanTransaction(tx3, BLOCK_ID);
        TestTransactionContext lateCtx3 = new TestTransactionContext();
        boolean accepted3 = manager.register(tx3, BLOCK_ID, lateCtx3);
        Assert.assertFalse(accepted3, "Registration after cleanTransaction must be rejected");
        Assert.assertEquals(lateCtx3.closes.get(), 1, "Rejected context must be closed immediately");
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
