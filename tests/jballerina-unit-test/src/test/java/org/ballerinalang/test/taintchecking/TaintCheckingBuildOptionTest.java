/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.taintchecking;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test taint checking build option.
 *
 * @since 2.0.0
 */
public class TaintCheckingBuildOptionTest {
    @Test
    public void testTaintErrorsWithoutTaintCheckingBuildOptionInTomlFile() {
        CompileResult result = BCompileUtil.compileAndCacheBalo("test-src/taintchecking/proj/noTaintCheckOption");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testTaintErrorsWithTaintCheckingBuildOptionInTomlFile() {
        CompileResult result = BCompileUtil.compileAndCacheBalo("test-src/taintchecking/proj/taintCheckOptionEnabled");
        Assert.assertEquals(result.getErrorCount(), 1);
    }
}
