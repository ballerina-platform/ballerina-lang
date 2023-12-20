/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.test.execution;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for program build for big global variables.
 *
 */
public class LargeInitBuildTest {

    @Test
    public void testFileWithLargeInitMethod() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/large_init.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);
    }

    @Test
    public void testSplittingLargeInitMethod() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/large-init");
        BRunUtil.invoke(compileResult, "main");
    }
}

