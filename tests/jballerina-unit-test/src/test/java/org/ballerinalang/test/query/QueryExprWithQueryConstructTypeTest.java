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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * This contains methods to test query expression with query construct type.
 *
 * @since 1.3.0
 */
public class QueryExprWithQueryConstructTypeTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-expr-with-query-construct-type.bal");
        negativeResult = BCompileUtil.compile("test-src/query/query-expr-query-construct-type-negative.bal");
    }

    @Test(description = "Test query expr returning a stream ")
    public void testSimpleQueryReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with stream in from clause returning a stream ")
    public void testStreamInFromClauseWithReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testStreamInFromClauseWithReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with multiple from, let and where clauses returning a stream ")
    public void testMultipleFromWhereAndLetReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleFromWhereAndLetReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with inner join returning a stream ")
    public void testInnerJoinAndLimitReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testInnerJoinAndLimitReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr returning table")
    public void testSimpleQueryExprReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testTableWithDuplicateKeys() {

        BValue[] returnValues = BRunUtil.invoke(result, "testTableWithDuplicateKeys");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BError expectedError = (BError) returnValues[0];
        Assert.assertEquals(expectedError.stringValue(), "{ballerina/lang.table}KeyConstraintViolation " +
                "{\"message\":\"A value found for key '1 Melina'\"}");
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testTableNoDuplicatesAndOnConflictReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testTableNoDuplicatesAndOnConflictReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testTableWithDuplicatesAndOnConflictReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testTableWithDuplicatesAndOnConflictReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BError expectedError = (BError) returnValues[0];
        Assert.assertEquals(expectedError.stringValue(), "Key Conflict {\"message\":\"cannot insert.\"}");
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testQueryExprWithOtherClausesReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOtherClausesReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BError expectedError = (BError) returnValues[0];
        Assert.assertEquals(expectedError.stringValue(), "Key Conflict {\"message\":\"cannot insert.\"}");
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testQueryExprWithJoinClauseReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithJoinClauseReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BError expectedError = (BError) returnValues[0];
        Assert.assertEquals(expectedError.stringValue(), "Key Conflict {\"message\":\"cannot insert.\"}");
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testQueryExprWithLimitClauseReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitClauseReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testKeyLessTableWithReturnTable() {
        BRunUtil.invoke(result, "testKeyLessTableWithReturnTable");
        BValue[] returnValues = BRunUtil.invoke(result, "testKeyLessTableWithReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test negative scenarios for query expr with query construct type")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 5);
        int index = 0;

        validateError(negativeResult, index++, "incompatible types: expected 'Person[]', found 'stream<Person>'",
                38, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'Customer[]', " +
                        "found '(table<Customer> key(id, name)|error)'",
                55, 32);
        validateError(negativeResult, index++, "incompatible types: expected " +
                        "'table<Customer> key(id, name)', found '(table<Customer> key(id, name)|error)'",
                70, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'error', found 'boolean'",
                91, 21);
        validateError(negativeResult, index, "type 'error' not allowed here; expected " +
                "an 'error' or a subtype of 'error'.", 91, 21);
    }
}
