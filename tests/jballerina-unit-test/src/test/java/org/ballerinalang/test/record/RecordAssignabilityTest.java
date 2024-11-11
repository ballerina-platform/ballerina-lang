/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test record assignability.
 */
public class RecordAssignabilityTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/record/record_assignability.bal");
    }

    @DataProvider
    public static Object[] recordAssignabilityTestFunctions() {
        return new String[]{
                "testOpenRecordToRecordWithOptionalFieldTypingRuntimeNegative",
                "testRecordToRecordWithOptionalFieldTypingRuntimePositive"
        };
    }

    @Test(dataProvider = "recordAssignabilityTestFunctions")
    public void testRecordAssignability(String testFunction) {
        BRunUtil.invoke(result, testFunction);
    }

    @Test
    public void testRecordAssignabilityNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/record/record_assignability_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; anydata...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; anydata...; |}'", 19, 55);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; anydata...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; anydata...; |}'", 20, 54);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(boolean|string|int|record {| (boolean|int) b; anydata...; |})'," +
                " found '(boolean|string|int|record {| (boolean|string) b; anydata...; |})'", 23, 53);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(boolean|string|int|record {| (boolean|int) b; anydata...; |})'," +
                " found '(boolean|string|int|record {| (boolean|string) b; anydata...; |})'", 24, 52);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; |}'", 29, 57);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; |}'", 30, 56);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean)...; |}'", 33, 58);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean)...; |}'", 34, 57);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; (int|boolean)...; |}'", 37, 72);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'record {| (int|string|boolean) a; (int|string) b; (int|string)...; |}'," +
                " found 'record {| (int|string|boolean) a; (int|boolean) b; (int|boolean)...; |}'", 38, 71);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(boolean|string|int|record {| (boolean|int) b; |})'," +
                " found '(boolean|string|int|record {| (boolean|string) b; |})'", 41, 55);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(boolean|string|int|record {| (boolean|int) b; |})'," +
                " found '(boolean|string|int|record {| (boolean|string) b; |})'", 42, 54);
        validateError(negativeResult, i++, "incompatible types: expected 'R1', found 'R2'", 60, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'R1', found 'record {| (int|string)...; |}'",
                      63, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'R1', found 'record {| int...; |}'", 66, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'R3', found 'record {| int...; |}'", 67, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'record {| int a?; int b; anydata...; |}', " +
                "found 'record {| readonly int? b; int...; |}'", 70, 33);
        assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
