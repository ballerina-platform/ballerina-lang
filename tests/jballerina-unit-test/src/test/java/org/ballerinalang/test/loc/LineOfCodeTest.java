/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.loc;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Compiling/Running a program with large lines of code.
 */
public class LineOfCodeTest {
    CompileResult compileResult;

    @BeforeClass(description = "Compiling ballerina program with 37000 lines of code")
    public void testCompileProgramWithHighLineOfCodes() {
        compileResult = BCompileUtil.compile("test-src/loc/high_loc.bal");
    }

    @Test(description = "Validate compilation")
    public void validateCompileResult() {
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(description = "Test running ballerina program with 37000 lines of code")
    public void testRunProgramWithHighLineOfCodes() {
        Object returns = BRunUtil.invoke(compileResult, "largeFunction");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 37000L);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
