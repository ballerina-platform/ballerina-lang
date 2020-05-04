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

import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
    public void testConcurrentSleep() {
        BValueArray result = (BValueArray) BRunUtil.invoke(compileResult, "testConcurrentSleep")[0];
        Assert.assertTrue(checkWithErrorMargin(result.getInt(0), 1000, 500));
        Assert.assertTrue(checkWithErrorMargin(result.getInt(1), 1000, 500));
        Assert.assertTrue(checkWithErrorMargin(result.getInt(2), 2000, 500));
        Assert.assertTrue(checkWithErrorMargin(result.getInt(3), 2000, 500));
        Assert.assertTrue(checkWithErrorMargin(result.getInt(4), 1000, 500));
    }

    private boolean checkWithErrorMargin(long actual, long expected, long error) {
        return actual <= expected + error && actual >= expected - error;
    }

    @Test
    public void testGetProperty() {
        String key = "BALLERINA";
        String expectedValue = "ROCKS";
        System.setProperty(key, expectedValue);

        BValue[] args = new BValue[1];
        args[0] = new BString(key);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetProperty", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetCallStack() {
        BValue[] returns = BRunUtil.invoke(errorResult, "testGetCallStack");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "{callableName:\"getCallStack\", moduleName:\"ballerina" +
                "/runtime\", fileName:\"<native>\", lineNumber:0}");
        Assert.assertEquals(returns[1].stringValue(), "{callableName:\"level2Function\", moduleName:\".\", " +
                "fileName:\"runtime-error.bal\", lineNumber:12}");
        Assert.assertEquals(returns[2].stringValue(), "{callableName:\"level1Function\", moduleName:\".\", " +
                "fileName:\"runtime-error.bal\", lineNumber:8}");
        Assert.assertEquals(returns[3].stringValue(), "{callableName:\"testGetCallStack\", moduleName:\".\", " +
                "fileName:\"runtime-error.bal\", lineNumber:4}");
    }

    @Test
    public void testErrorStackFrame() {
        BValue[] returns = BRunUtil.invoke(errorResult, "testErrorStackFrame");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(),
                "{callableName:\"level2Error\", moduleName:\".\", fileName:\"runtime-error.bal\", lineNumber:30}");
        Assert.assertEquals(returns[1].stringValue(),
                "{callableName:\"level1Error\", moduleName:\".\", fileName:\"runtime-error.bal\", lineNumber:25}");
        Assert.assertEquals(returns[2].stringValue(),
                "{callableName:\"testErrorStackFrame\", moduleName:\".\", fileName:\"runtime-error.bal\", " +
                        "lineNumber:16}");
    }
}
