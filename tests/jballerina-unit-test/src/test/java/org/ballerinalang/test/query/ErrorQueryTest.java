/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expressions with error returns.
 *
 * @since 2.0.0
 */
public class ErrorQueryTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query_expr_with_errors.bal");
        negativeResult = BCompileUtil.compile("test-src/query/query_expr_with_errors_negative.bal");
    }

    @Test
    public void queryAnErrorStream() {
        BRunUtil.invoke(result, "queryAnErrorStream");
    }

    @Test
    public void queryWithoutErrors() {
        BRunUtil.invoke(result, "queryWithoutErrors");
    }

    @Test
    public void queryWithAnError() {
        BRunUtil.invoke(result, "queryWithAnError");
    }

    @Test
    public void queryWithACheckFail() {
        BRunUtil.invoke(result, "queryWithACheckFailEncl");
    }

    @Test
    public void queryWithAPanic() {
        BRunUtil.invoke(result, "queryWithAPanicEncl");
    }

    @Test
    public void streamFromQueryWithoutErrors() {
        BRunUtil.invoke(result, "streamFromQueryWithoutErrors");
    }

    @Test
    public void streamFromQueryWithAnError() {
        BRunUtil.invoke(result, "streamFromQueryWithAnError");
    }

    @Test
    public void streamFromQueryWithACheckFail() {
        BRunUtil.invoke(result, "streamFromQueryWithACheckFail");
    }

    @Test
    public void streamFromQueryWithAPanic() {
        BRunUtil.invoke(result, "streamFromQueryWithAPanicEncl");
    }

    @Test
    public void testDistinctErrorReturn() {
        BRunUtil.invoke(result, "testDistinctErrorReturn");
    }

    @Test
    public void testCatchingErrorAtOnFail() {
        BRunUtil.invoke(result, "testCatchingErrorAtOnFail");
    }

    @Test
    public void testErrorReturnedFromSelectClause() {
        BRunUtil.invoke(result, "testErrorReturnedFromSelect");
    }

    @Test
    public void testErrorReturnedFromWhereClause() {
        BRunUtil.invoke(result, "testErrorReturnedFromWhereClause");
    }

    @Test
    public void testErrorReturnedFromLetClause() {
        BRunUtil.invoke(result, "testErrorReturnedFromLetClause");
    }

    @Test
    public void testErrorReturnedFromLimitClause() {
        BRunUtil.invoke(result, "testErrorReturnedFromLimitClause");
    }

    @Test
    public void testErrorReturnedFromJoinClause() {
        BRunUtil.invoke(result, "testErrorReturnedFromJoinClause");
    }

    @Test
    public void testErrorReturnedFromOrderByClause() {
        BRunUtil.invoke(result, "testErrorReturnedFromOrderByClause");
    }

    @Test
    public void testErrorReturnedFromStreamConstruction() {
        BRunUtil.invoke(result, "testErrorReturnedFromStreamConstruction");
    }

    @Test
    public void testErrorReturnedFromTableConstruction() {
        BRunUtil.invoke(result, "testErrorReturnedFromTableConstruction");
    }

    @Test
    public void testErrorReturnedFromXmlConstruction() {
        BRunUtil.invoke(result, "testErrorReturnedFromXmlConstruction");
    }

    @Test
    public void testErrorReturnedFromArrayConstruction() {
        BRunUtil.invoke(result, "testErrorReturnedFromArrayConstruction");
    }

    @Test(description = "Test negative scenarios for different constructors with queries")
    public void testNegativeScenarios() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int,error>', " +
                "found 'stream<int>'", 84, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', " +
                "found 'stream<int,CustomError?>'", 91, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int,error>', " +
                "found 'stream<int,CustomError?>'", 95, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', " +
                "found 'stream<int,CustomError?>'", 105, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', " +
                "found 'stream<int,CustomError?>'", 109, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', " +
                "found 'stream<int,CustomError?>'", 113, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int,error>', " +
                "found 'stream<int,CustomError?>'", 118, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int,error>', " +
                "found 'stream<int,(CustomError|error)?>'", 123, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', " +
                "found 'stream<int,error>'", 127, 21);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'CustomerTable', " +
                "found '(table<Customer> key(id)|error)'", 136, 36);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'CustomerTable', " +
                "found '(table<Customer> key(id)|error)'", 143, 36);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'CustomerTable', " +
                "found '(table<Customer> key(id)|error)'", 148, 36);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(CustomerTable|CustomError)', " +
                "found '(table<Customer> key(id)|CustomError|error)'", 152, 48);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', " +
                "found '(int[]|CustomError)'", 161, 18);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', " +
                "found '(int[]|CustomError)'", 171, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', " +
                "found '(int[]|CustomError)'", 175, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', " +
                "found '(int[]|CustomError)'", 179, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', " +
                "found '(int[]|error)'", 192, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', " +
                "found '(int[]|error)'", 196, 15);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'xml', " +
                "found '(xml|error)'", 206, 13);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'xml', " +
                "found '(xml|error)'", 210, 13);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'xml', " +
                "found '(xml|CustomError)'", 214, 13);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: '(xml[]|error)' " +
                "is not an iterable collection", 219, 30);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'xml', " +
                "found '(xml|error)'", 224, 13);

        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
