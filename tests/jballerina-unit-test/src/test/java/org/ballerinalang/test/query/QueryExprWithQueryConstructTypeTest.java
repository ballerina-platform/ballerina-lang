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

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with stream in from clause returning a stream ")
    public void testStreamInFromClauseWithReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testStreamInFromClauseWithReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with multiple from, let and where clauses returning a stream ")
    public void testMultipleFromWhereAndLetReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testMultipleFromWhereAndLetReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with inner join returning a stream ")
    public void testInnerJoinAndLimitReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testInnerJoinAndLimitReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr returning table")
    public void testSimpleQueryExprReturnTable() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with table having duplicate keys")
    public void testTableWithDuplicateKeys() {
        BRunUtil.invoke(result, "testTableWithDuplicateKeys");
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testTableNoDuplicatesAndOnConflictReturnTable() {
        Object returnValues = BRunUtil.invoke(result, "testTableNoDuplicatesAndOnConflictReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
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
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitClauseReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with table having no duplicates and on conflict clause")
    public void testKeyLessTableWithReturnTable() {
        BRunUtil.invoke(result, "testKeyLessTableWithReturnTable");
        Object returnValues = BRunUtil.invoke(result, "testKeyLessTableWithReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test negative scenarios for query expr with query construct type")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 25);
        int index = 0;

        validateError(negativeResult, index++, "incompatible types: expected 'Person[]', found 'stream<Person>'",
                54, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'Customer[]', " +
                        "found '(table<Customer> key(id, name)|error)'",
                71, 32);
        validateError(negativeResult, index++, "incompatible types: expected " +
                        "'CustomerTable', found '(table<Customer> key(id, name)|error)'",
                86, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'error?', found 'boolean'",
                107, 21);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'User'", 126, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found '[int,User]'", 130, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'int[2]'", 135, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'string[]'", 140, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'User'", 148, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found '[int,User]'", 152, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'int[2]'", 157, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'string[]'", 162, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'string[3]'", 167, 25);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found '[string]'", 171, 25);
        validateError(negativeResult, index++,
                "incompatible types: expected 'int', found 'string'", 180, 50);
        validateError(negativeResult, index++,
                "incompatible types: expected 'map<User>', found '(map<User>|error)'", 182, 20);
        validateError(negativeResult, index++,
                "incompatible types: expected 'map<string>', found '(map<string>|error)'", 186, 22);
        validateError(negativeResult, index++,
                "incompatible types: expected 'int', found 'string'", 193, 50);
        validateError(negativeResult, index++,
                "incompatible types: expected 'map<User>', found '(map<User>|error)'", 195, 20);
        validateError(negativeResult, index++,
                "incompatible types: expected 'map<string>', found '(map<string>|error)'", 199, 22);
        validateError(negativeResult, index++,
                "incompatible types: expected '[string,string]', found '(string[2]|[string,int])'", 207, 29);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'int[2] & readonly'", 217, 29);
        validateError(negativeResult, index++,
                "incompatible type in select clause: expected [string,any|error], found 'int[2]'", 222, 29);
        validateError(negativeResult, index++,
                "incompatible types: expected '([string,int]|[string,string])', found '(string|int)'", 227, 56);
        validateError(negativeResult, index,
                "incompatible types: expected 'map<string>', found '(map<(int|string)>|error)'", 229, 21);
    }

    @Test(description = "Test semantic negative scenarios for query expr with query construct type")
    public void testSemanticNegativeScenarios() {
        int index = 0;
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                        "maps or tables with key specifiers", 39, 13);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 59, 9);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 71, 9);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 84, 9);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 95, 9);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 103, 14);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 119, 9);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 126, 47);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 131, 9);
        validateError(semanticsNegativeResult, index++, "on conflict can only be used with queries which produce " +
                "maps or tables with key specifiers", 144, 9);
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), index);
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

    @Test
    public void testTableOnConflict() {
        BRunUtil.invoke(result, "testTableOnConflict");
    }

    @Test(dataProvider = "dataToTestQueryConstructingTable")
    public void testQueryConstructingTable(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryConstructingTable() {
        return new Object[]{
                "testQueryConstructingTableWithOnConflictClauseHavingNonTableQueryInLetClause",
                "testQueryConstructingTableWithOnConflictClauseHavingNonTableQueryInWhereClause"
        };
    }

    @Test
    public void testMapConstructingQueryExpr() {
        BRunUtil.invoke(result, "testMapConstructingQueryExpr");
    }

    @Test
    public void testMapConstructingQueryExprWithDuplicateKeys() {
        BRunUtil.invoke(result, "testMapConstructingQueryExprWithDuplicateKeys");
    }

    @Test
    public void testMapConstructingQueryExprWithOnConflict() {
        BRunUtil.invoke(result, "testMapConstructingQueryExprWithOnConflict");
    }

    @Test
    public void testMapConstructingQueryExprWithOtherClauses() {
        BRunUtil.invoke(result, "testMapConstructingQueryExprWithOtherClauses");
    }

    @Test
    public void testMapConstructingQueryExprWithJoinClause() {
        BRunUtil.invoke(result, "testMapConstructingQueryExprWithJoinClause");
    }

    @Test
    public void testMapConstructingQueryExprWithLimitClause() {
        BRunUtil.invoke(result, "testMapConstructingQueryExprWithLimitClause");
    }

    @Test
    public void testMapConstructingQueryExpr2() {
        BRunUtil.invoke(result, "testMapConstructingQueryExpr2");
    }

    @Test
    public void testMapConstructingQueryExprWithOrderByClause() {
        BRunUtil.invoke(result, "testMapConstructingQueryExprWithOrderByClause");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        semanticsNegativeResult = null;
    }
}
