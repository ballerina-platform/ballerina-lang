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

import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.utils.CloneUtils;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * Test cases for {@link BTupleType}.
 */
public class TupleTypeTests {

    @Test
    void testMutableTupleTypeIsNotReadOnlyWhileComputingTypeFlags() {
        AtomicBoolean readOnlyWhileComputingFlags = new AtomicBoolean();
        BTupleType tupleType = new BTupleType(new ArrayList<>());
        // The type flags of a tuple are computed from its members, so this member is asked while that happens.
        Type member = new BFutureType(PredefinedTypes.TYPE_INT) {
            @Override
            public boolean isPureType() {
                readOnlyWhileComputingFlags.set(tupleType.isReadOnly());
                return super.isPureType();
            }
        };
        tupleType.setMemberTypes(List.of(member), null);

        tupleType.getTypeFlags();

        Assert.assertFalse(readOnlyWhileComputingFlags.get());
        Assert.assertFalse(tupleType.isReadOnly());
    }

    @Test
    void testConcurrentCloneReadOnlyOnFirstUseOfTupleType() throws Exception {
        int workers = 8;
        int rounds = 2_000;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            for (int round = 0; round < rounds; round++) {
                // A new type each round, since only the first use of a tuple type computes its type flags.
                BTupleType tupleType = new BTupleType(List.of(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));
                CyclicBarrier barrier = new CyclicBarrier(workers);
                List<Callable<Void>> tasks = IntStream.range(0, workers).<Callable<Void>>mapToObj(worker -> () -> {
                    TupleValueImpl original =
                            new TupleValueImpl(new Object[]{1L, StringUtils.fromString("a")}, tupleType);
                    barrier.await();
                    BArray clone = (BArray) CloneUtils.cloneReadOnly(original);

                    Assert.assertNotSame(clone, original);
                    Assert.assertTrue(clone.getType().isReadOnly());
                    Assert.assertFalse(original.getType().isReadOnly());
                    return null;
                }).toList();
                for (Future<Void> each : executor.invokeAll(tasks)) {
                    each.get();
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
