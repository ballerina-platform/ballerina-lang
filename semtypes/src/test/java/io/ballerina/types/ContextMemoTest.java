/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.types;

import io.ballerina.types.definition.Field;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.definition.MappingDefinition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Tests the memo tables of a {@link Context}.
 *
 * @since 2201.14.0
 */
public class ContextMemoTest {

    private static final SemType[] LEAVES = {PredefinedType.INT, PredefinedType.STRING, PredefinedType.BOOLEAN,
            PredefinedType.FLOAT, PredefinedType.DECIMAL, PredefinedType.NIL};

    @Test
    public void testMemoEntriesAreReleasedWithTheirTypes() throws InterruptedException {
        Env env = new Env();
        Context cx = Context.from(env);
        Random random = new Random(1);
        for (int i = 0; i < 2000; i++) {
            SemTypes.isSubtype(cx, record(env, random), record(env, random));
        }
        Assert.assertFalse(cx.mappingMemo.isEmpty());

        for (int i = 0; i < 20 && !cx.mappingMemo.isEmpty(); i++) {
            System.gc();
            Thread.sleep(50);
        }
        Assert.assertTrue(cx.mappingMemo.isEmpty(), "memo entries outlived their types");
    }

    @Test
    public void testConcurrentSubtypeChecksOnOneContext() throws InterruptedException {
        Env env = new Env();
        Context cx = Context.from(env);
        Queue<Throwable> failures = new ConcurrentLinkedQueue<>();
        Thread[] threads = new Thread[8];
        for (int t = 0; t < threads.length; t++) {
            Random random = new Random(t);
            threads[t] = new Thread(() -> {
                try {
                    for (int i = 0; i < 2000; i++) {
                        SemType x = record(env, random);
                        SemTypes.isSubtype(cx, x, record(env, random));
                        SemTypes.isSubtype(cx, tuple(env, random), tuple(env, random));
                        SemTypes.isSubtype(cx, SemTypes.union(x, tuple(env, random)), record(env, random));
                    }
                } catch (Throwable e) {
                    failures.add(e);
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            thread.join(60_000);
            Assert.assertFalse(thread.isAlive(), "a subtype check did not finish");
        }
        Assert.assertTrue(failures.isEmpty(), "concurrent subtype checks failed: " + failures.peek());
    }

    private static SemType record(Env env, Random random) {
        Map<String, Field> fields = new TreeMap<>();
        for (int i = 0, n = 1 + random.nextInt(4); i < n; i++) {
            String name = "f" + random.nextInt(6);
            fields.put(name, new Field(name, LEAVES[random.nextInt(LEAVES.length)], false, random.nextBoolean()));
        }
        return new MappingDefinition().defineMappingTypeWrapped(env, List.copyOf(fields.values()),
                PredefinedType.NEVER);
    }

    private static SemType tuple(Env env, Random random) {
        SemType[] members = new SemType[1 + random.nextInt(3)];
        for (int i = 0; i < members.length; i++) {
            members[i] = LEAVES[random.nextInt(LEAVES.length)];
        }
        return new ListDefinition().tupleTypeWrapped(env, members);
    }
}
