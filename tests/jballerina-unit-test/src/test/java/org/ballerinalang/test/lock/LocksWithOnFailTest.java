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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Tests for Ballerina locks with on fail clause.
 *
 * @since 0.961.0
 */
public class LocksWithOnFailTest {

    @Test(description = "Tests lock within a lock")
    public void testLockWithinLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/lock-on-fail.bal");

        BValue[] returnsWithFail = BRunUtil.invoke(compileResult, "failLockWithinLock");
        assertEquals(returnsWithFail.length, 2);
        assertSame(returnsWithFail[0].getClass(), BInteger.class);
        assertSame(returnsWithFail[1].getClass(), BString.class);

        assertEquals(((BInteger) returnsWithFail[0]).intValue(), 100);
        assertEquals(returnsWithFail[1].stringValue(), "Error caught");

        BValue[] returnsWithCheck = BRunUtil.invoke(compileResult, "checkLockWithinLock");
        assertEquals(returnsWithCheck.length, 2);
        assertSame(returnsWithCheck[0].getClass(), BInteger.class);
        assertSame(returnsWithCheck[1].getClass(), BString.class);

        assertEquals(((BInteger) returnsWithCheck[0]).intValue(), 100);
        assertEquals(returnsWithCheck[1].stringValue(), "Error caught");

    }
}
