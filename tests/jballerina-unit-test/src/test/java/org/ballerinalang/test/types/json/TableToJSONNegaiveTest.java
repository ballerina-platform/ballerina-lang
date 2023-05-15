/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.json;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test class for negative tests with json types in Ballerina tables.
 */
public class TableToJSONNegaiveTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/table_to_json_negative.bal");
    }

    @Test
    public void testNegatives() {
        int indx = 0;
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| readonly int id; string firstName; string lastName; int salary; |}>'", 18, 8);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| readonly int id; string firstName; string lastName; int salary; |}>'", 25, 8);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| int id; string firstName; string lastName; int salary; |}>'", 32, 8);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| |}>'", 39, 8);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| |}>'", 43, 8);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| readonly int id; string firstName; string lastName; int salary; |}>'", 48, 12);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| readonly int id; string firstName; string lastName; int salary; |}>'", 55, 12);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| int id; string firstName; string lastName; int salary; |}>'", 62, 12);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| |}>'", 69, 12);
        validateError(compileResult, indx++, "incompatible types: expected 'json', found " +
                "'table<record {| |}>'", 73, 12);
        assertEquals(compileResult.getErrorCount(), indx);
    }
}
