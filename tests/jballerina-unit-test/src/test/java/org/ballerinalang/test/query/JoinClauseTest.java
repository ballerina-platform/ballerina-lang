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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

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

    @Test(description = "Test join clause with record variable definition")
    public void testSimpleJoinClauseWithRecordVariable() {
        Object values = BRunUtil.invoke(result, "testSimpleJoinClauseWithRecordVariable");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test join clause having mapping binding with field name : variable name")
    public void testSimpleJoinClauseWithRecordVariable2() {
        Object values = BRunUtil.invoke(result, "testSimpleJoinClauseWithRecordVariable2");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test join clause having mapping binding with variable name")
    public void testSimpleJoinClauseWithRecordVariable3() {
        Object values = BRunUtil.invoke(result, "testSimpleJoinClauseWithRecordVariable3");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test join clause with simple variable definition and stream")
    public void testJoinClauseWithStream() {
        Object values = BRunUtil.invoke(result, "testJoinClauseWithStream", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test query expr with join and limit clause")
    public void testJoinClauseWithLimit() {
        Object values = BRunUtil.invoke(result, "testJoinClauseWithLimit");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test outer join clause with record variable definition")
    public void testOuterJoinClauseWithRecordVariable() {
        Object values = BRunUtil.invoke(result, "testOuterJoinClauseWithRecordVariable");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test join clause having mapping binding with field name : variable name")
    public void testOuterJoinClauseWithRecordVariable2() {
        Object values = BRunUtil.invoke(result, "testOuterJoinClauseWithRecordVariable2");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test join clause having mapping binding with variable name")
    public void testOuterJoinClauseWithRecordVariable3() {
        Object values = BRunUtil.invoke(result, "testOuterJoinClauseWithRecordVariable3");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test join clause with simple variable definition and stream")
    public void testOuterJoinClauseWithStream() {
        Object values = BRunUtil.invoke(result, "testOuterJoinClauseWithStream", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test query expr with join and limit clause")
    public void testOuterJoinClauseWithLimit() {
        Object values = BRunUtil.invoke(result, "testOuterJoinClauseWithLimit");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test equals clause with a variable defined from a let clause")
    public void testSimpleJoinClauseWithLetAndEquals() {
        Object values = BRunUtil.invoke(result, "testSimpleJoinClauseWithLetAndEquals");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test equals clause with a function invocation")
    public void testSimpleJoinClauseWithFunctionInAnEquals() {
        Object values = BRunUtil.invoke(result, "testSimpleJoinClauseWithFunctionInAnEquals");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test outer join with null results")
    public void testOuterJoinWithNullResults() {
        BRunUtil.invoke(result, "testOuterJoin");
    }

    @Test(description = "Test join clause with a large list")
    public void testJoinClauseWithLargeList() {
        BRunUtil.invoke(result, "testJoinClauseWithLargeList");
    }
    
    @Test(description = "Test negative scenarios for query expr with join clause")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 40);
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'Department', found 'Person'", 46, 13);
        validateError(negativeResult, i++, "undeclared field 'name' in record 'Person'", 51, 19);
        validateError(negativeResult, i++, "unknown type 'XYZ'", 69, 13);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 70, 28);
        validateError(negativeResult, i++, "undefined symbol 'deptId'", 93, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 93, 25);
        validateError(negativeResult, i++, "undefined symbol 'id'", 116, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 116, 21);
        validateError(negativeResult, i++, "undefined symbol 'name'", 140, 11);
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 140, 23);
        validateError(negativeResult, i++, "undefined symbol 'deptId'", 163, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 163, 25);
        validateError(negativeResult, i++, "undefined symbol 'id'", 186, 11);
        validateError(negativeResult, i++, "undefined symbol 'person'", 186, 21);
        validateError(negativeResult, i++, "undefined symbol 'name'", 210, 11);
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 210, 23);
        validateError(negativeResult, i++, "undefined symbol 'id'", 234, 23);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'other'", 234, 34);
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 234, 34);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'other'", 266, 1);
        validateError(negativeResult, i++, "missing equals keyword", 266, 1);
        validateError(negativeResult, i++, "missing identifier", 266, 1);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'other'", 289, 1);
        validateError(negativeResult, i++, "missing equals keyword", 289, 1);
        validateError(negativeResult, i++, "missing identifier", 289, 1);
        validateError(negativeResult, i++, "missing equals keyword", 309, 1);
        validateError(negativeResult, i++, "missing identifier", 309, 1);
        validateError(negativeResult, i++, "missing identifier", 309, 1);
        validateError(negativeResult, i++, "missing on keyword", 309, 1);
        validateError(negativeResult, i++, "undefined symbol 'dept'", 329, 24);
        validateError(negativeResult, i++, "missing equals keyword", 330, 1);
        validateError(negativeResult, i++, "missing identifier", 330, 1);
        validateError(negativeResult, i++, "outer join must be declared with 'var'", 353, 19);
        validateError(negativeResult, i++, "undefined symbol 'dept'", 357, 19);
        validateError(negativeResult, i++, "invalid operation: type 'Person?' does not support field access", 374, 16);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 389, 59);
        validateError(negativeResult, i++, "invalid operation: type 'Person?' does not support field access", 389, 59);
        validateError(negativeResult, i++, "invalid operation: type 'Person?' does not support field access", 395, 22);
        validateError(negativeResult, i++, "order by not supported for complex type fields, order key should belong" +
                " to a basic type", 395, 22);
        validateError(negativeResult, i++, "invalid operation: type 'Person?' does not support field access", 397, 36);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
