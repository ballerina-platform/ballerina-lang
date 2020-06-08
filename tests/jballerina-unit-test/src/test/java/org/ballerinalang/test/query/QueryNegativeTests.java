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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Negative test cases for query expressions.
 *
 * @since 1.2.0
 */
public class QueryNegativeTests {

    @Test
    public void testFromClauseWithInvalidType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/query/query-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 21);
        int index = 0;

        validateError(compileResult, index++, "incompatible types: expected 'Person', found 'Teacher'",
                                  48, 18);
        validateError(compileResult, index++, "invalid operation: type 'Teacher' does not support field access for " +
                              "non-required field 'lastName'", 51, 30);
        validateError(compileResult, index++, "invalid operation: type 'Teacher' does not support field access for " +
                              "non-required field 'age'", 52, 25);
        validateError(compileResult, index++, "unknown type 'XYZ'", 67, 18);
        validateError(compileResult, index++, "undefined field 'lastName' in record 'Teacher'", 87, 20);
        validateError(compileResult, index++, "incompatible types: 'int' is not an iterable collection", 100, 32);
        validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'int'", 101, 19);
        validateError(compileResult, index++, "incompatible types: expected 'Person', found 'int'", 102, 20);
        validateError(compileResult, index++, "cannot assign a value to final 'person'", 117, 17);
        validateError(compileResult, index++, "missing non-defaultable required record field 'lastName'", 132, 10);
        validateError(compileResult, index++, "incompatible types: expected 'float', found 'int'", 153, 13);
        validateError(compileResult, index++, "undefined function 'calculateScore'", 168, 22);
        validateError(compileResult, index++, "invalid record binding pattern; unknown field " +
                "'fname' in record type 'Student'", 205, 12);
        validateError(compileResult, index++, "undefined symbol 'fname'", 207, 15);
        validateError(compileResult, index++, "incompatible types: expected 'Student', found " +
                "'(string|float)'", 222, 10);
        validateError(compileResult, index++, "incompatible types: expected 'Address', found 'map<string>'", 241, 13);
        validateError(compileResult, index++, "cannot assign a value to final 'twiceScore'", 259, 10);
        validateError(compileResult, index++, "incompatible types: expected 'FullName[]', found 'error?'", 266, 13);
        validateError(compileResult, index++, "incompatible types: expected 'xml', found '(xml|string)'", 283, 24);
        validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 297, 24);
        validateError(compileResult, index, "a type compatible with mapping constructor expressions " +
                "not found in type 'string'", 311, 24);
    }
}
