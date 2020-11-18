/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.string;

import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the migration of bValues' stringValue implementations.
 */
public class BStringToStringTest extends BStringTestCommons {

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/bstring-tostring-test.bal");
    }

    @Test
    public void testDecimalToString() {
        testAndAssert("testDecimalToString", 7);
    }

    @Test
    public void testFunctionPointerToString() {
        testAndAssert("testFunctionPointerToString", 41);
    }

    @Test
    public void testMapToString() {
        testAndAssert("testMapToString", 81);
    }

    @Test
    public void testMapToStringWithSymbol() {
        testAndAssert("testMapToStringWithSymbol", 84);
    }

    @Test
    public void testArrayToString() {
        testAndAssert("testArrayToString", 22);
    }

    @Test
    public void testTupleToString() {
        testAndAssert("testTupleToString", 14);
    }

    @Test
    public void testJsonToString() {
        testAndAssert("testJsonToString", 18);
    }

    @Test
    public void testObjectToString() {
        testAndAssert("testObjectToString", 13);
    }

    @Test
    public void testArrayValueToString() {
        testAndAssert("testArrayValueToString", 30);
    }
}
