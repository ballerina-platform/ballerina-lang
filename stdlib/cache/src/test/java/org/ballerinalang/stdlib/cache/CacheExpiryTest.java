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
package org.ballerinalang.stdlib.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Tests for cache expiry.
 */
public class CacheExpiryTest {

    private CompileResult compileResult;
    private static final Log log = LogFactory.getLog(CacheExpiryTest.class);

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compileAndSetup("test-src/cache/cache-expiry-test.bal");
        printDiagnostics(compileResult);
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }

    @Test
    public void testCacheExpiry() {
        BRunUtil.invokeStateful(compileResult, "initCache");

        // Check that the cache size gradually decreases due to cache expiry
        await().atMost(5, SECONDS).until(() -> {
            BValue[] cacheSizes = BRunUtil.invokeStateful(compileResult, "getCacheSize");
            return ((BInteger) cacheSizes[0]).intValue() == 4;
        });
        await().atMost(20, SECONDS).until(() -> {
            BValue[] cacheSizes = BRunUtil.invokeStateful(compileResult, "getCacheSize");
            return ((BInteger) cacheSizes[0]).intValue() < 4;
        });
        await().atMost(30, SECONDS).until(() -> {
            BValue[] cacheSizes = BRunUtil.invokeStateful(compileResult, "getCacheSize");
            return ((BInteger) cacheSizes[0]).intValue() == 0;
        });
    }
}
