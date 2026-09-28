/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.test;

import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BFiniteType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Test cases for {@link BFiniteType}.
 */
public class FiniteTypeTests {

    @Test
    void testConcurrentCreationOfDistinctlyNamedTypes() throws Exception {
        int workers = 8;
        int typesPerWorker = 10_000;
        CyclicBarrier barrier = new CyclicBarrier(workers);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            List<Callable<Void>> tasks = IntStream.range(0, workers).<Callable<Void>>mapToObj(worker -> () -> {
                barrier.await();
                for (int i = 0; i < typesPerWorker; i++) {
                    new BFiniteType("finite-" + worker + "-" + i, Set.of(1L), 0);
                }
                return null;
            }).toList();
            for (Future<Void> each : executor.invokeAll(tasks)) {
                each.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testSameNamedTypesShareTypeId() {
        String name = "finite-" + UUID.randomUUID();
        Assert.assertEquals(new BFiniteType(name, Set.of(1L), 0).typeId(),
                new BFiniteType(name, Set.of(1L), 0).typeId());
    }

    @Test
    void testTypedescOfSimpleValueIsNotCachedByValue() {
        BFiniteType first = (BFiniteType) TypeChecker.getTypedesc(7L).getDescribingType();
        BFiniteType second = (BFiniteType) TypeChecker.getTypedesc(7L).getDescribingType();

        Assert.assertEquals(first.toString(), "7");
        Assert.assertEquals(first, second);
        Assert.assertNotEquals(first.typeId(), second.typeId());
    }
}
