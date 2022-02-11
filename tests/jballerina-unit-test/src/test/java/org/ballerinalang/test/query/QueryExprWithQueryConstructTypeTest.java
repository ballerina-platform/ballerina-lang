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

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
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
        Object returnValues = JvmRunUtil.invoke(result, "testSimpleQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with stream in from clause returning a stream ")
    public void testStreamInFromClauseWithReturnStream() {
        Object returnValues = JvmRunUtil.invoke(result, "testStreamInFromClauseWithReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with multiple from, let and where clauses returning a stream ")
    public void testMultipleFromWhereAndLetReturnStream() {
        Object returnValues = JvmRunUtil.invoke(result, "testMultipleFromWhereAndLetReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with inner join returning a stream ")
    public void testInnerJoinAndLimitReturnStream() {
        Object returnValues = JvmRunUtil.invoke(result, "testInnerJoinAndLimitReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr returning table")
    public void testSimpleQueryExprReturnTable() {
        Object returnValues = JvmRunUtil.invoke(result, "testSimpleQueryExprReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testTableWithDuplicateKeys() {

        Object returnValues = JvmRunUtil.invoke(result, "testTableWithDuplicateKeys");
        Assert.assertNotNull(returnValues);

        BError expectedError = (BError) returnValues;
        Assert.assertEquals(expectedError.toString(), "error(\"{ballerina/lang.table}KeyConstraintViolation\"," +
                "message=\"a value found for key '[1,\"Melina\"]'\")");
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testTableNoDuplicatesAndOnConflictReturnTable() {
        Object returnValues = JvmRunUtil.invoke(result, "testTableNoDuplicatesAndOnConflictReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testTableWithDuplicatesAndOnConflictReturnTable() {
        JvmRunUtil.invoke(result, "testTableWithDuplicatesAndOnConflictReturnTable");
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testQueryExprWithOtherClausesReturnTable() {
        JvmRunUtil.invoke(result, "testQueryExprWithOtherClausesReturnTable");
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testQueryExprWithJoinClauseReturnTable() {
        JvmRunUtil.invoke(result, "testQueryExprWithJoinClauseReturnTable");
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testQueryExprWithLimitClauseReturnTable() {
        Object returnValues = JvmRunUtil.invoke(result, "testQueryExprWithLimitClauseReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testKeyLessTableWithReturnTable() {
        JvmRunUtil.invoke(result, "testKeyLessTableWithReturnTable");
        Object returnValues = JvmRunUtil.invoke(result, "testKeyLessTableWithReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
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
        JvmRunUtil.invoke(result, "testQueryConstructingTableUpdateKeyPanic1");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"cannot update 'readonly' field 'id' in record of type 'record " +
                    "\\{\\| readonly int id; readonly string name; User user; \\|\\}'\".*")
    public void testQueryConstructingTableUpdateKeyPanic2() {
        JvmRunUtil.invoke(result, "testQueryConstructingTableUpdateKeyPanic2");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        semanticsNegativeResult = null;
    }
}
