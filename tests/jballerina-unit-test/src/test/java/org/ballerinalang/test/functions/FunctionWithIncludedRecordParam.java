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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.functions;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test function signatures and calling with included record params.
 *
 * @since 2.0.0
 */
public class FunctionWithIncludedRecordParam {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/functions_with_included_record_parameters.bal");
    }
    @Test
    public void testFuncSignatureSemanticsNegative() {
        int i = 0;
        CompileResult result = BCompileUtil.compile("test-src/functions/" +
                                                    "functions_with_included_record_parameters_negative.bal");
        BAssertUtil.validateError(result, i++, "redeclared symbol 'firstName'", 47, 82);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'secondName'", 47, 82);
        BAssertUtil.validateError(result, i++, "missing required parameter 'b' in call to " +
                                "'functionWithIncludedRecordParam1'()", 56, 16);
        BAssertUtil.validateError(result, i++, "too many arguments in call to " +
                                            "'functionWithIncludedRecordParam2()'", 57, 105);
        BAssertUtil.validateError(result, i++, "missing required parameter 'firstName' in call to " +
                                "'functionWithIncludedRecordParam2'()", 58, 19);
        BAssertUtil.validateError(result, i++, "missing required parameter 'secondName' in call to " +
                                "'functionWithIncludedRecordParam2'()", 58, 19);
        BAssertUtil.validateError(result, i++, "too many arguments in call to " +
                                            "'functionWithIncludedRecordParam3()'", 59, 95);
        BAssertUtil.validateError(result, i++, "invalid operation: type 'Address' does not support field access" +
                                " for non-required field 'firstName'", 67, 12);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'a'", 80, 47);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 81, 51);
        BAssertUtil.validateError(result, i++, "undefined field 'abc' in 'Bar2'", 90, 12);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 104, 75);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 122, 71);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to " +
                                "'functionWithIncludedRecordParam8'()", 136, 18);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 136, 71);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 144, 63);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'id'", 161, 53);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'name'", 161, 72);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'string'", 166, 49);
        BAssertUtil.validateError(result, i++, "expected a record type as an included parameter", 169, 15);

        Assert.assertEquals(i, result.getErrorCount());
    }



    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam2() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam2");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam3() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam3");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam4() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam4");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam5() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam5");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam6() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam6");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam7() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam7");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam8() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam8");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam9() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam9");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam10() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam10");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam11() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam11");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam12() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam12");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam13() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam13");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam14() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam14");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam15() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam15");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam16() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam16");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam17() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam17");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam18() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam18");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam19() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam19");
    }

    @Test
    public void testFunctionOfFunctionTypedParamWithIncludedRecordParam20() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithIncludedRecordParam20");
    }
}
