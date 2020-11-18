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
 * This class contains tests for tables as function arguments.
 *
 * @since 1.3.0
 */
public class TablesAsFuncArgs {
    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/tables-as-func-arguments.bal");
        negativeResult = BCompileUtil.compile("test-src/types/table/tables-as-func-args-negative.bal");
    }

    @Test
    public void testSameKeySpecifierInParamAndArg() {
        BValue[] values = BRunUtil.invoke(result, "testSameKeySpecifierInParamAndArg", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testKeySpecifierAndKeyConstraint() {
        BValue[] values = BRunUtil.invoke(result, "testKeySpecifierAndKeyConstraint", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testSameKeyConstraintInArgAndParam() {
        BValue[] values = BRunUtil.invoke(result, "testSameKeyConstraintInArgAndParam", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testNoKeyConstraintParam() {
        BValue[] values = BRunUtil.invoke(result, "testNoKeyConstraintParam1", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());

        values = BRunUtil.invoke(result, "testNoKeyConstraintParam2", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testUnionTypeInParam() {
        BValue[] values = BRunUtil.invoke(result, "testUnionTypeInParam", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testUnionConstraintParam() {
        BValue[] values = BRunUtil.invoke(result, "testUnionConstraintParam", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testUnionKeyConstraintParam() {
        BValue[] values = BRunUtil.invoke(result, "testUnionKeyConstraintParam", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testDifferentKeySpecifierInParamAndArg() {
        Assert.assertEquals(7, negativeResult.getErrorCount());
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'table<Person> key(name)', found 'table<Person> key(age)'",
                45, 43);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'table<Person> key<string>', found 'table<Person> key(age)'",
                54, 43);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'table<Person> key<string>', found 'table<Person> key<int>'",
                63, 43);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'table<Person> key<int>', found 'table<Person>'",
                72, 43);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'table<Person> key<int>', found 'table<Person>'",
                81, 43);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'table<Person> key<string>', found 'table<Person> key(age)'",
                90, 13);
        BAssertUtil.validateError(negativeResult, i,
                "incompatible types: expected 'table<Person> key(name)', found 'table<Person> key(age)'",
                98, 13);

    }
}
