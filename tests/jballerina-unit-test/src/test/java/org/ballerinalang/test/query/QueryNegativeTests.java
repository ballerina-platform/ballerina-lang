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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Negative test cases for query expressions.
 *
 * @since 1.2.0
 */
public class QueryNegativeTests {

    @Test
    public void testFromClauseWithInvalidType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/query/query-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 39);
        int index = 0;

        validateError(compileResult, index++, "incompatible types: expected 'Person', found 'Teacher'",
                64, 18);
        validateError(compileResult, index++, "undeclared field 'lastName' in record 'Teacher'", 67, 30);
        validateError(compileResult, index++, "undeclared field 'age' in record 'Teacher'", 68, 25);
        validateError(compileResult, index++, "unknown type 'XYZ'", 83, 18);
        validateError(compileResult, index++, "undefined field 'lastName' in record 'Teacher'", 103, 20);
        validateError(compileResult, index++, "incompatible types: 'int' is not an iterable collection", 116, 32);
        validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'int'", 117, 19);
        validateError(compileResult, index++, "incompatible types: expected 'Person', found 'int'", 118, 20);
        validateError(compileResult, index++, "missing non-defaultable required record field 'lastName'", 132, 10);
        validateError(compileResult, index++, "incompatible types: expected 'float', found 'int'", 153, 13);
        validateError(compileResult, index++, "undefined function 'calculateScore'", 168, 22);
        validateError(compileResult, index++, "invalid record binding pattern; unknown field " +
                "'fname' in record type 'Student'", 205, 12);
        validateError(compileResult, index++, "undefined symbol 'fname'", 207, 15);
        validateError(compileResult, index++, "incompatible types: expected 'Student', found " +
                "'(string|float)'", 222, 10);
        validateError(compileResult, index++, "incompatible types: expected 'Address', found 'map<string>'", 241, 13);
        validateError(compileResult, index++, "incompatible types: expected 'FullName[]', found 'error?'", 266, 13);
        validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 278, 24);
        validateError(compileResult, index++, "a type compatible with mapping constructor expressions " +
                "not found in type 'string'", 292, 24);
        validateError(compileResult, index++, "ambiguous type '[xml, xml]'", 314, 24);
        validateError(compileResult, index++, "ambiguous type '[string, string]'", 327, 24);
        validateError(compileResult, index++, "redeclared symbol 'fname'", 351, 36);
        validateError(compileResult, index++, "redeclared symbol 'age'", 364, 21);
        validateError(compileResult, index++, "redeclared symbol 'age'", 381, 44);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of 'map<any|error>' " +
                "but found 'int'", 401, 11);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of 'map<any|error>' " +
                "but found 'int'", 401, 22);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of 'map<any|error>' " +
                "but found 'int'", 411, 11);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of 'map<any|error>' " +
                "but found 'int'", 411, 22);
        validateError(compileResult, index++, "incompatible types: 'int' is not an iterable collection",
                416, 29);
        validateError(compileResult, index++, "incompatible types: expected 'error?', " +
                        "found 'stream<record {| int a; |},error?>'", 421, 12);
        validateError(compileResult, index++, "invalid record binding pattern with type 'anydata'", 426, 22);
        validateError(compileResult, index++, "undefined symbol 'k'", 427, 25);
        validateError(compileResult, index++, "invalid record binding pattern with type 'any'", 432, 22);
        validateError(compileResult, index++, "undefined symbol 'k'", 433, 25);
        validateError(compileResult, index++, "field name 'id' used in key specifier is not found in " +
                        "table constraint type 'record {| User user; |}'", 451, 28);
        validateError(compileResult, index++, "field name 'id' used in key specifier is not found in " +
                "table constraint type 'record {| User user; |}'", 456, 24);
        validateError(compileResult, index++, "field name 'id' used in key specifier is not found in " +
                "table constraint type 'record {| User user; |}'", 469, 28);
        validateError(compileResult, index++, "field name 'firstName' used in key specifier is not found in " +
                "table constraint type 'record {| User user; |}'", 469, 32);
        validateError(compileResult, index++, "field name 'id' used in key specifier is not found in " +
                "table constraint type 'record {| User user; |}'", 474, 24);
        validateError(compileResult, index, "field name 'firstName' used in key specifier is not found in " +
                "table constraint type 'record {| User user; |}'", 474, 28);
    }

    @Test
    public void testFromClauseWithInvalidAssignmentToFinalVar() {
        CompileResult compileResult = BCompileUtil.compile("test-src/query/query_dataflow_negative.bal");
        int index = 0;
        validateWarning(compileResult, index++, "unused variable 'x'", 42, 5);
        validateWarning(compileResult, index++, "unused variable 'person'", 42, 21);
        validateError(compileResult, index++, "cannot assign a value to final 'person'", 44, 17);
        validateWarning(compileResult, index++, "unused variable 'outputNameList'", 58, 5);
        validateError(compileResult, index++, "cannot assign a value to final 'twiceScore'", 62, 10);
        Assert.assertEquals(compileResult.getDiagnostics().length, index);
    }
}
