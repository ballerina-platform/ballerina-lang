/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.lock;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests for Ballerina locks with on fail clause.
 *
 * @since 0.961.0
 */
public class LocksWithOnFailTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/lock/lock-on-fail.bal");
    }

    @Test(description = "Tests lock within a lock")
    public void testLockWithinLock() {
        Object val = BRunUtil.invoke(compileResult, "failLockWithinLock");
        BArray returnsWithFail = (BArray) val;
        assertEquals(returnsWithFail.size(), 2);
        assertSame(returnsWithFail.get(0).getClass(), Long.class);
        assertTrue(returnsWithFail.get(1) instanceof BString);

        assertEquals(returnsWithFail.get(0), 100L);
        assertEquals(returnsWithFail.get(1).toString(), "Error caught");

        Object val2 = BRunUtil.invoke(compileResult, "checkLockWithinLock");
        BArray returnsWithCheck = (BArray) val2;
        assertEquals(returnsWithCheck.size(), 2);
        assertSame(returnsWithCheck.get(0).getClass(), Long.class);
        assertTrue(returnsWithCheck.get(1) instanceof BString);

        assertEquals(returnsWithCheck.get(0), 100L);
        assertEquals(returnsWithCheck.get(1).toString(), "Error caught");
    }

    @Test
    public void testOnFailLockWithinLockWithoutVariable() {
        BRunUtil.invoke(compileResult, "onFailLockWithinLockWithoutVariable");
    }

    @Test(dataProvider = "onFailClauseWithErrorBPTestDataProvider")
    public void testOnFailWithErrorBP(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "onFailClauseWithErrorBPTestDataProvider")
    public Object[] onFailClauseWithErrorBPTestDataProvider() {
        return new Object[]{
                "testSimpleOnFailWithErrorBP",
                "testSimpleOnFailWithErrorBPWithVar",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithError",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithVar",
                "testOnFailWithErrorBPHavingUserDefinedType",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail1",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail2",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail4",
                "testOnFailWithErrorBPHavingAnonDetailRecord",
                "testOnFailWithErrorBPHavingAnonDetailRecordWithVar",
                "testOnFailWithErrorBPHavingAnonDetailRecordWithUnionType",
                "testOnFailWithErrorBPWithErrorArgsHavingBP1",
                "testOnFailWithErrorBPWithErrorArgsHavingBP2",
                "testOnFailWithErrorBPWithErrorArgsHavingBP3",
                "testOnFailWithErrorBPWithErrorArgsHavingBP4",
                "testOnFailWithErrorBPWithErrorArgsHavingBP5",
                "testMultiLevelOnFailWithErrorBP",
                "testMultiLevelOnFailWithoutErrorInOneLevel"
        };
    }

    @Test(description = "Check incompatible types.")
    public void testNegative() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/lock/lock-on-fail-negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "invalid error variable; expecting an " +
                "error type but found '(SampleComplexError|SampleError)' in type definition", 39, 15);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
