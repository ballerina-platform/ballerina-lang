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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test field based locking.
 */
public class FieldLockTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/lock/locks-in-object.bal");

    }

    @Test(description = "Test locking by self var")
    public void testLockInAttachedFunc() {
        BValue[] returns =
                BRunUtil.invoke(compileResult, "lockFieldInSameObject");
        assertFalse(returns[0].stringValue().contains("*#*") || returns[0].stringValue().contains("#*#"));


    }

    @Test(description = "Test locking by passes object as param")
    public void testObjectLock() {
        BValue[] returns2 =
                BRunUtil.invoke(compileResult, "fieldLock");
        assertTrue((returns2[0].stringValue().equals("1001000") || returns2[0].stringValue().equals("500500")));

        BValue[] returns3 =
                BRunUtil.invoke(compileResult, "objectParamLock");
        assertTrue((returns3[0].stringValue().equals("1001000") || returns3[0].stringValue().equals("500500")));
    }


}
