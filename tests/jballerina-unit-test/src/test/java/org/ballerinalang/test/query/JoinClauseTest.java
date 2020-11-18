/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.query;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * This contains methods to test join clause in query expression.
 *
 * @since 1.3.0
 */
public class JoinClauseTest {
    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/join-clause.bal");
        negativeResult = BCompileUtil.compile("test-src/query/join-clause-negative.bal");
    }

    @Test(description = "Test join clause with record variable definition type 1")
    public void testSimpleJoinClauseWithRecordVariable() {
        BValue[] values = BRunUtil.invoke(result, "testSimpleJoinClauseWithRecordVariable");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test join clause with record variable definition type 2")
    public void testSimpleJoinClauseWithRecordVariable2() {
        BValue[] values = BRunUtil.invoke(result, "testSimpleJoinClauseWithRecordVariable2");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test join clause with record variable definition type 3")
    public void testSimpleJoinClauseWithRecordVariable3() {
        BValue[] values = BRunUtil.invoke(result, "testSimpleJoinClauseWithRecordVariable3");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test join clause with simple variable definition and stream")
    public void testJoinClauseWithStream() {
        BValue[] values = BRunUtil.invoke(result, "testJoinClauseWithStream", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test query expr with join and limit clause")
    public void testJoinClauseWithLimit() {
        BValue[] values = BRunUtil.invoke(result, "testJoinClauseWithLimit");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test outer join clause with record variable definition type 1")
    public void testOuterJoinClauseWithRecordVariable() {
        BValue[] values = BRunUtil.invoke(result, "testOuterJoinClauseWithRecordVariable");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test join clause with record variable definition type 2")
    public void testOuterJoinClauseWithRecordVariable2() {
        BValue[] values = BRunUtil.invoke(result, "testOuterJoinClauseWithRecordVariable2");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test join clause with record variable definition type 3")
    public void testOuterJoinClauseWithRecordVariable3() {
        BValue[] values = BRunUtil.invoke(result, "testOuterJoinClauseWithRecordVariable3");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test join clause with simple variable definition and stream")
    public void testOuterJoinClauseWithStream() {
        BValue[] values = BRunUtil.invoke(result, "testOuterJoinClauseWithStream", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test query expr with join and limit clause")
    public void testOuterJoinClauseWithLimit() {
        BValue[] values = BRunUtil.invoke(result, "testOuterJoinClauseWithLimit");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test equals clause with a variable defined from a let clause")
    public void testSimpleJoinClauseWithLetAndEquals() {
        BValue[] values = BRunUtil.invoke(result, "testSimpleJoinClauseWithLetAndEquals");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test equals clause with a function invocation")
    public void testSimpleJoinClauseWithFunctionInAnEquals() {
        BValue[] values = BRunUtil.invoke(result, "testSimpleJoinClauseWithFunctionInAnEquals");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(groups = {"disableOnOldParser"}, description = "Test negative scenarios for query expr with join clause")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 32);
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'Department', found 'Person'", 30, 13);
        validateError(negativeResult, i++, "invalid operation: type 'Person' does not support field access for " +
                "non-required field 'name'", 35, 19);
        validateError(negativeResult, i++, "unknown type 'XYZ'", 53, 13);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 54, 28);
        validateError(negativeResult, i++, "undefined symbol 'deptId'", 77, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 77, 25);
        validateError(negativeResult, i++, "undefined symbol 'id'", 100, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 100, 21);
        validateError(negativeResult, i++, "undefined symbol 'name'", 124, 11);
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 124, 23);
        validateError(negativeResult, i++, "undefined symbol 'deptId'", 147, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 147, 25);
        validateError(negativeResult, i++, "undefined symbol 'id'", 170, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 170, 21);
        validateError(negativeResult, i++, "undefined symbol 'name'", 194, 11);
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 194, 23);
        validateError(negativeResult, i++, "undefined symbol 'id'", 218, 23);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'other'", 218, 34);
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 218, 34);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'other'", 250, 1);
        validateError(negativeResult, i++, "missing equals keyword", 250, 1);
        validateError(negativeResult, i++, "missing identifier", 250, 1);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'other'", 273, 1);
        validateError(negativeResult, i++, "missing equals keyword", 273, 1);
        validateError(negativeResult, i++, "missing identifier", 273, 1);
        validateError(negativeResult, i++, "missing equals keyword", 293, 1);
        validateError(negativeResult, i++, "missing identifier", 293, 1);
        validateError(negativeResult, i++, "missing identifier", 293, 1);
        validateError(negativeResult, i++, "missing on keyword", 293, 1);
        validateError(negativeResult, i++, "undefined symbol 'dept'", 313, 24);
        validateError(negativeResult, i++, "missing equals keyword", 314, 1);
        validateError(negativeResult, i, "missing identifier", 314, 1);
    }
}
