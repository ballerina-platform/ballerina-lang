/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.anydata;

import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;
import static org.ballerinalang.launcher.util.BCompileUtil.compile;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for compile errors with regard to `anydata` type.
 *
 * @since 0.985.0
 */
public class AnydataNegativeTest {

    @Test(description = "General negative test cases for anydata")
    public void testNegativeCases() {
        CompileResult result = compile("test-src/types/anydata/anydata_negative_test.bal");
        int index = 0;

        assertEquals(result.getErrorCount(), 33);

        // Invalid literal assignments to `anydata`
        validateError(result, index++, "invalid usage of record literal with type 'anydata'", 37, 20);
        validateError(result, index++, "invalid usage of record literal with type 'anydata'", 38, 21);
        validateError(result, index++, "invalid usage of record literal with type 'anydata'", 39, 20);
        validateError(result, index++, "invalid usage of tuple literal with type 'anydata'", 40, 22);

        // Invalid map assignments to `anydata`
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map'", 56, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map'", 59, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<Bar>'", 62, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found 'map<function (string) returns (boolean)>'", 65,
                10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<typedesc>'", 68, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<stream>'", 71, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<any[]>'", 74, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<map<map>>'", 77, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<Foo>'", 80, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found 'map<" +
                        "(int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?,string)>'",
                83, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found 'map<(" +
                        "(int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?,string),Bar)>'",
                86, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found " +
                        "'map<int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?>'", 89, 10);

        // Test invalid array assignments
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Bar[]'", 96, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found 'function (string) returns (boolean)?[]'", 99, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'any[]'", 102, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'typedesc[]'", 105, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'stream?[]'", 108, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'any[][]'", 111, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map[]'", 114, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'map<Bar>[]'", 117, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo[]'", 120, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found " +
                        "'int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[][]?'", 123, 10);
        validateError(result, index++,
                "incompatible types: expected 'anydata', found '(" +
                        "(int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[][]?,string)," +
                        "int,float)'", 126, 10);

        // Test invalid union assignments
        validateError(result, index++,
                "incompatible types: expected 'anydata', found " +
                        "'int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?'", 131, 18);

        // Test invalid tuple assignments
        validateError(result, index++, "incompatible types: expected 'anydata', found '(int,float,Bar)'", 136, 18);
        validateError(result, index++, "incompatible types: expected 'anydata', found '" +
                        "(int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?,int)'",
                139, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found '(" +
                        "(int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?,int)," +
                        "string,int)'", 142, 10);

        // Test invalid map insertions
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Bar'", 147, 16);
        validateError(result, index, "incompatible types: expected 'anydata', found " +
                        "'int|float|string|boolean|byte|table<any>|json|xml|Bar|map<anydata>|anydata[]?'", 150, 18);
    }

    @Test(description = "Negative test cases for non-anydata closed record assignment")
    public void testInvalidClosedRecordAssignments() {
        CompileResult result = compile("test-src/types/anydata/anydata_invalid_closed_record_assignment.bal");
        int index = 0;

        assertEquals(result.getErrorCount(), 9);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo'", 21, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo1'", 24, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo2'", 27, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo3'", 30, 10);
        validateError(result, index++, "missing non-defaultable required record field 'td'", 32, 24);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo4'", 33, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo5'", 36, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'ClosedFoo6'", 39, 10);
        validateError(result, index, "incompatible types: expected 'anydata', found 'ClosedFoo7'", 42, 10);
    }

    @Test(description = "Negative test cases for non-anydata open record assignment")
    public void testInvalidOpenRecordAssignments() {
        CompileResult result = compile("test-src/types/anydata/anydata_invalid_open_record_assignment.bal");
        int index = 0;

        assertEquals(result.getErrorCount(), 9);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo'", 21, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo1'", 24, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo2'", 27, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo3'", 30, 10);
        validateError(result, index++, "missing non-defaultable required record field 'td'", 32, 17);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo4'", 33, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo5'", 36, 10);
        validateError(result, index++, "incompatible types: expected 'anydata', found 'Foo6'", 39, 10);
        validateError(result, index, "incompatible types: expected 'anydata', found 'Foo7'", 42, 10);
    }
}
