/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.nativeimpl.functions;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for runtime package.
 */
public class RuntimeTest {

    private CompileResult compileResult;
    private CompileResult errorResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/runtime-test.bal");
        errorResult = BCompileUtil.compile("test-src/nativeimpl/functions/runtime-error.bal");
    }

    @Test
    public void testSleep() {
        long startTime = System.currentTimeMillis();
        BRunUtil.invoke(compileResult, "testSleep");
        long endTime = System.currentTimeMillis();
        Assert.assertTrue((endTime - startTime) >= 1000);
    }

    @Test
    public void testGetCallStack() {
        Object val = BRunUtil.invoke(errorResult, "testGetCallStack");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0).toString(), "{callableName:externGetStackTrace, moduleName:ballerina.lang" +
                ".runtime.0, fileName:runtime.bal, lineNumber:121}");
        Assert.assertEquals(returns.get(1).toString(), "{callableName:getStackTrace, moduleName:ballerina.lang" +
                ".runtime.0, fileName:runtime.bal, lineNumber:111}");
        Assert.assertEquals(returns.get(2).toString(), "{callableName:level2Function, moduleName:null, " +
                "fileName:runtime-error.bal, lineNumber:12}");
        Assert.assertEquals(returns.get(3).toString(), "{callableName:level1Function, moduleName:null, " +
                "fileName:runtime-error.bal, lineNumber:8}");
        Assert.assertEquals(returns.get(4).toString(), "{callableName:testGetCallStack, moduleName:null, " +
                "fileName:runtime-error.bal, lineNumber:4}");
    }

    @Test
    public void testErrorCallStack() {
        BArray returns = (BArray) BRunUtil.invoke(errorResult, "testErrorCallStack");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "{callableName:level2Error, moduleName:null, " +
                "fileName:runtime-error.bal, lineNumber:30}");
        Assert.assertEquals(returns.get(1).toString(), "{callableName:level1Error, moduleName:null, " +
                "fileName:runtime-error.bal, lineNumber:25}");
        Assert.assertEquals(returns.get(2).toString(), "{callableName:testErrorCallStack, moduleName:null, " +
                "fileName:runtime-error.bal, lineNumber:16}");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        errorResult = null;
    }
}
