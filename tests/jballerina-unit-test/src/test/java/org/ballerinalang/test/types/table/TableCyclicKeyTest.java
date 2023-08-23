/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.table;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class contains the test to check cyclic check for keys.
 */
public class TableCyclicKeyTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/tables-acyclic-key.bal");
    }

    @Test(enabled = false, expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}CyclicValueReferenceError " +
                    "\\{\"message\":\"'Address' value has cyclic reference.*")
    public void testCyclesInRecords() {
        BRunUtil.invoke(result, "testCyclesInRecords");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
