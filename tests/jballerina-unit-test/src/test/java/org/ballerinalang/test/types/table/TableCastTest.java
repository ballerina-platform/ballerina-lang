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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] results = BRunUtil.invoke(result, "testKeyConstraintCastToString1");
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testKeyConstraintCastToString2() {
        BValue[] results = BRunUtil.invoke(result, "testKeyConstraintCastToString2");
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testKeyConstraintCastToString3() {
        BValue[] results = BRunUtil.invoke(result, "testKeyConstraintCastToString3");
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testKeyConstraintCastToString4() {
        BValue[] results = BRunUtil.invoke(result, "testKeyConstraintCastToString4");
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 5);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: 'table<(Person|Customer)> key<string>' " +
                "cannot be cast to 'table<Person> key<int>'", 49, 34);
        BAssertUtil.validateError(negativeResult, 1, "invalid key constraint provided for member access. " +
                "key constraint expected with type 'int'", 50, 12);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'int', found 'string'", 50, 16);
        BAssertUtil.validateError(negativeResult, 3, "invalid key constraint provided for member access. " +
                "key constraint expected with type 'string'", 60, 12);
        BAssertUtil.validateError(negativeResult, 4, "incompatible types: expected 'string', found 'int'", 60, 16);
    }
}
