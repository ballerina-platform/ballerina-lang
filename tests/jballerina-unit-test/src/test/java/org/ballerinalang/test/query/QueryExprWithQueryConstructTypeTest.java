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
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test query expression with query construct type.
 *
 * @since 1.3.0
 */
public class QueryExprWithQueryConstructTypeTest {

    private CompileResult result, negativeResult, semanticsNegativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-expr-with-query-construct-type.bal");
        negativeResult = BCompileUtil.compile("test-src/query/query-expr-query-construct-type-negative.bal");
        semanticsNegativeResult = BCompileUtil.compile("test-src/query/query-expr-query-construct-type-semantics" +
                "-negative.bal");
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
                "{\"message\":\"a value found for key '[1,\"Melina\"]'\"}");
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
        BRunUtil.invoke(result, "testTableWithDuplicatesAndOnConflictReturnTable");
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testQueryExprWithOtherClausesReturnTable() {
        BRunUtil.invoke(result, "testQueryExprWithOtherClausesReturnTable");
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testQueryExprWithJoinClauseReturnTable() {
        BRunUtil.invoke(result, "testQueryExprWithJoinClauseReturnTable");
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
                54, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'Customer[]', " +
                        "found '(table<Customer> key(id, name)|error)'",
                71, 32);
        validateError(negativeResult, index++, "incompatible types: expected " +
                        "'CustomerTable', found '(table<Customer> key(id, name)|error)'",
                86, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'error', found 'boolean'",
                107, 21);
        validateError(negativeResult, index, "type 'error' not allowed here; expected " +
                "an 'error' or a subtype of 'error'.", 107, 21);
    }

    @Test(description = "Test semantic negative scenarios for query expr with query construct type")
    public void testSemanticNegativeScenarios() {
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), 1);
        validateError(semanticsNegativeResult, 0, "on conflict can only be used with queries which produce tables " +
                        "with key specifiers",
                39, 13);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"cannot update 'readonly' field 'id' in record of type 'record " +
                    "\\{\\| readonly int id; readonly string name; User user; \\|\\}'\".*")
    public void testQueryConstructingTableUpdateKeyPanic1() {
        BRunUtil.invoke(result, "testQueryConstructingTableUpdateKeyPanic1");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"cannot update 'readonly' field 'id' in record of type 'record " +
                    "\\{\\| readonly int id; readonly string name; User user; \\|\\}'\".*")
    public void testQueryConstructingTableUpdateKeyPanic2() {
        BRunUtil.invoke(result, "testQueryConstructingTableUpdateKeyPanic2");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        semanticsNegativeResult = null;
    }
}
