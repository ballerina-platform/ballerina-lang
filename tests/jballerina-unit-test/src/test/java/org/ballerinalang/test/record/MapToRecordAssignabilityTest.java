/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.record;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for map to record assignability.
 *
 * @since 1.2.1
 */
public class MapToRecordAssignabilityTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/map_to_record.bal");
    }

    @Test
    public void testNegative() {
        CompileResult result = BCompileUtil.compile("test-src/record/negative/map_to_record_negative.bal");
        int indx = 0;

        validateError(result, indx++, "incompatible types: expected 'record {| string...; |}', found 'map<int>'",
                19, 36);
        validateError(result, indx++, "incompatible types: expected 'record {| anydata...; |}', found 'map'",
                22, 26);
        validateError(result, indx++,
                "incompatible types: expected 'record {| string a; string...; |}', found 'map<string>'", 29, 12);
        validateError(result, indx++, "incompatible types: expected 'record {| Bar...; |}', found 'map<Foo>'", 52, 13);
        assertEquals(result.getErrorCount(), indx);
    }

    @Test(dataProvider = "FunctionList")
    public void testMapToRecordAssignability(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*InherentTypeViolation \\{\"message\":\"invalid map insertion: " +
                    "expected value of type 'decimal', found 'float'.*")
    public void testInherentTypeViolationInInclusiveRecords() {
        BRunUtil.invoke(compileResult, "testInherentTypeViolationInInclusiveRecords");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*InherentTypeViolation \\{\"message\":\"invalid map insertion: " +
                    "expected value of type 'int', found 'string'.*")
    public void testInherentTypeViolationInExclusiveRecords() {
        BRunUtil.invoke(compileResult, "testInherentTypeViolationInExclusiveRecords");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*KeyNotFound \\{\"message\":\"invalid field access: field 'cc' " +
                    "not found in record type 'Bar'.*")
    public void testSubtyping() {
        BRunUtil.invoke(compileResult, "testSubtyping");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp =
                    ".*InherentTypeViolation \\{\"message\":\"invalid map insertion: expected" +
                            " value of type 'Bar', found 'record \\{\\| int c; \\|\\}'.*")
    public void testComplexSubtyping2() {
        BRunUtil.invoke(compileResult, "testComplexSubtyping2");
    }

    @Test
    public void testQuotedFieldNamesWithEscapeCharacters() {
        BRunUtil.invoke(compileResult, "testQuotedFieldNamesWithEscapeCharacters");
    }

    @Test
    public void testQuotedSubFieldNamesWithEscapeCharacters() {
        BRunUtil.invoke(compileResult, "testQuotedSubFieldNamesWithEscapeCharacters");
    }

    @DataProvider(name = "FunctionList")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"testBasicDataTypeMaps"},
                {"testInclusiveRecordTypes"},
                {"testRecordsWithOptionalFields"},
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
