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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contain tests for table casting.
 *
 * @since 1.3.0
 */
public class TableCastTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/tables-cast.bal");
        negativeResult = BCompileUtil.compile("test-src/types/table/tables-cast-negative.bal");
    }

    @Test
    public void testKeyConstraintCastToString1() {
        Object results = BRunUtil.invoke(result, "testKeyConstraintCastToString1");
        Assert.assertTrue((Boolean) results);
    }

    @Test
    public void testKeyConstraintCastToString2() {
        Object results = BRunUtil.invoke(result, "testKeyConstraintCastToString2");
        Assert.assertTrue((Boolean) results);
    }

    @Test
    public void testKeyConstraintCastToString3() {
        Object results = BRunUtil.invoke(result, "testKeyConstraintCastToString3");
        Assert.assertTrue((Boolean) results);
    }

    @Test
    public void testKeyConstraintCastToString4() {
        Object results = BRunUtil.invoke(result, "testKeyConstraintCastToString4");
        Assert.assertTrue((Boolean) results);
    }

    @Test
    public void testCastingWithEmptyKeyedKeylessTbl() {
        BRunUtil.invoke(result, "testCastingWithEmptyKeyedKeylessTbl");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 5);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: 'PersonTable1' " +
                "cannot be cast to 'table<Person> key<int>'", 49, 34);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'int', found 'string'", 50, 16);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'string', found 'int'", 60, 16);
        BAssertUtil.validateError(negativeResult, 3, "incompatible types: 'CustomerTable' cannot be " +
                "cast to 'table<Customer2>'", 77, 34);
        BAssertUtil.validateError(negativeResult, 4, "incompatible types: 'CustomerTable' cannot be cast to " +
                "'table<record {| int id; string name; string lname; anydata...; |}>'", 83, 20);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
