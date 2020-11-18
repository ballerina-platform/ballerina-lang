/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.lock;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test field based locking.
 * @since 0.985.0
 */
public class FieldLockTest {

    CompileResult compileResult;
    CompileResult compileResult2;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/lock/locks-in-object.bal");
        compileResult2 = BCompileUtil.compile("test-src/lock/locks-in-records.bal");
    }

    @Test(description = "Test locking by self var")
    public void testLockInAttachedFunc() {

        BValue[] returns = BRunUtil.invoke(compileResult, "lockFieldInSameObject");
        assertFalse(returns[0].stringValue().contains("*#*") || returns[0].stringValue().contains("#*#"));
    }

    @Test(description = "Test locking by passing object as param")
    public void testObjectLock() {

        BValue[] returns = BRunUtil.invoke(compileResult, "fieldLock");
        assertTrue((returns[0].stringValue().equals("1001000") || returns[0].stringValue().equals("500500")));

        BValue[] returns2 = BRunUtil.invoke(compileResult, "objectParamLock");
        assertTrue((returns2[0].stringValue().equals("1001000") || returns2[0].stringValue().equals("500500")));
    }

    @Test(description = "Test locking based on a record field")
    public void testLockInRecords() {

        BValue[] returns = BRunUtil.invoke(compileResult2, "fieldLock");
        assertTrue((returns[0].stringValue().equals("1001000") || returns[0].stringValue().equals("500500")));

        BValue[] returns2 = BRunUtil.invoke(compileResult2, "arrayFieldLock");
        assertTrue((returns2[0].stringValue().equals("1001000") || returns2[0].stringValue().equals("500500")));
    }
}
