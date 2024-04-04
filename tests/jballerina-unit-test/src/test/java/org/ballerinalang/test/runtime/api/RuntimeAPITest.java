/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.runtime.api;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Test cases for runtime api.
 *
 * @since 2.0.0
 */
public class RuntimeAPITest {

    @Test(dataProvider = "packageNameProvider")
    public void testRuntimeAPIs(String packageName) {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/" + packageName);
        BRunUtil.invoke(result, "main");
    }

    @DataProvider
    public Object[] packageNameProvider() {
        return new String[]{
                "values",
                "errors",
                "types",
                "invalid_values",
                "async",
                "utils",
                "identifier_utils",
                "environment",
                "stream",
                "json"
        };
    }

    @Test
    public void testRecordNoStrandDefaultValue() {
        CompileResult strandResult = BCompileUtil.compile("test-src/runtime/api/no_strand");
        final Scheduler scheduler = new Scheduler(false);
        AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        Thread thread1 = new Thread(() -> BRunUtil.runOnSchedule(strandResult, "main", scheduler));
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1000);
                BMap<BString, Object> recordValue = ValueCreator.createRecordValue(new Module("testorg",
                        "no_strand", "1"), "MutualSslHandshake");
                Assert.assertEquals(recordValue.getType().getName(), "MutualSslHandshake");
                Assert.assertEquals(recordValue.get(StringUtils.fromString("status")),
                        StringUtils.fromString("passed"));
                Assert.assertNull(recordValue.get(StringUtils.fromString("base64EncodedCert")));
            } catch (Throwable e) {
                exceptionRef.set(e);
            } finally {
                scheduler.poison();
            }
        });
        try {
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            Throwable storedException = exceptionRef.get();
            if (storedException != null) {
                throw new AssertionError("Test failed due to an exception in a thread", storedException);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while invoking function 'main'", e);
        }
    }

    @Test
    public void testRuntimeManagementAPI() {
        CompileResult strandResult = BCompileUtil.compile("test-src/runtime/api/runtime_mgt");
        AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        final Scheduler scheduler = new Scheduler(false);
        Thread thread1 = new Thread(() -> {
            BRunUtil.runOnSchedule(strandResult, "main", scheduler);
        });
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (Throwable e) {
                exceptionRef.set(e);
            } finally {
                scheduler.poison();
            }
        });

        try {
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            Throwable storedException = exceptionRef.get();
            if (storedException != null) {
                throw new AssertionError("Test failed due to an exception in a thread", storedException);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while invoking function 'main'", e);
        }
    }
}
