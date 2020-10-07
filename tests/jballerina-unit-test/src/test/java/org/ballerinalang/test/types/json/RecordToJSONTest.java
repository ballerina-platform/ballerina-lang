/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.json;

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for assignability of records to JSON.
 *
 * @since 1.2.1
 */
public class RecordToJSONTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/record_to_json.bal");
    }

    @Test
    public void testNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/types/jsontype/record_to_json_negative.bal");
        int indx = 0;
        validateError(result, indx++,
                      "incompatible types: expected 'json', found 'record {| string name; anydata...; |}'", 22, 15);
        validateError(result, indx++, "incompatible types: expected 'json', found 'record {| map m; |}'", 28, 15);
        validateError(result, indx++,
                      "incompatible types: expected 'json', found 'record {| string name; record {| record {| int x; " +
                              "any y; |} nestedL2; |} nestedL1; |}'",
                      40, 15);
        validateError(result, indx++,
                      "incompatible types: expected 'json', found 'record {| string name; record {| record {| int x; " +
                              "(int|string|typedesc)...; |} nestedL2; |} nestedL1; |}'",
                      52, 15);
        validateError(result, indx++, "incompatible types: expected 'json', found 'Bar'", 63, 14);
        assertEquals(result.getErrorCount(), indx);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*KeyNotFound \\{\"message\":\"invalid field access: field 'newField' not found in " +
                          "record type 'Foo'.*")
    public void testClosedRecordToJson() {
        BRunUtil.invoke(compileResult, "testClosedRecordToJson");
    }

    @Test
    public void testOpenRecordToJson() {
        BRunUtil.invoke(compileResult, "testOpenRecordToJson");
    }

    @Test
    public void testNestedRecordModification() {
        BRunUtil.invoke(compileResult, "testNestedRecordModification");
    }

    @Test
    public void testRecursiveCheckAgainstJson() {
        BRunUtil.invoke(compileResult, "testRecursiveCheckAgainstJson");
    }
}
