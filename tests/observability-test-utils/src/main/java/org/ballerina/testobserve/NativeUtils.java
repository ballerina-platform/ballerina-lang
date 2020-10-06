package org.ballerina.testobserve;

import org.ballerinalang.jvm.api.BalEnv;
import org.ballerinalang.jvm.api.BalFuture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utilities
 */
public class NativeUtils {
    private static final int CORE_THREAD_POOL_SIZE = 1;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(CORE_THREAD_POOL_SIZE);

    public static void sleep(BalEnv env, long delayMillis) {
        BalFuture balFuture = env.markAsync();
        executor.schedule(() -> balFuture.complete(null), delayMillis, TimeUnit.MILLISECONDS);
    }
}
