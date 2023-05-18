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
package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Constant type test cases.
 *
 * @since 2.0.0
 */
public class ConstantTypeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/constant-type.bal");
    }

    @Test(dataProvider = "testFunctions")
    public void testTypesOfConstants(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] testFunctions() {
        return new Object[]{
           "testTypesOfSimpleConstants",
           "testTypesOfConstantMaps",
           "testConstTypesInline",
           "testInvalidRuntimeUpdateOfConstMaps",
           "testResolvingConstValForConstantsOfUserDefinedTypes"    
        };
    }

    @Test
    public void constExpressionSemanticAnalysisNegative() {
        CompileResult compileResult1 = BCompileUtil.compile("test-src/types/constant/constant-type-negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '3', found 'int'", 34, 15);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '3.0f', found 'float'", 35, 15);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '3.0d', found 'float'", 36, 15);
        // Activate this after fixing #33889
//        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '3', found 'int'", 37, 15);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'false', found 'boolean'",
                38, 15);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '\"12\"', found 'string'", 39, 15);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE1', found '3'", 41, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE2', found '3.0f'", 42, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE3', found '3.0d'", 43, 16);
        // Activate this after fixing #33889
//        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE4', found '3'", 44, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE5', found 'false'", 45, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE6', found '\"12\"'", 46, 16);

        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE12', found '(record {| " +
                "record {| record {| 1 a; |} b; |} a; record {| record {| 1 a; |} a; record {| 2 b; 3 c; |} CMI2; " +
                "record {| 1 d; |} c; |} b; |} & readonly)'", 117, 17);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE11', found " +
                "'(record {| 0.11f a; 2.12f b; |} & readonly)'", 118, 17);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE10', found " +
                "'(record {| 0.11d a; 2.12d b; |} & readonly)'", 119, 17);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE8', found " +
                "'(record {| true a; false b; |} & readonly)'", 121, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE7', found '(record {| \"C\"" +
                " a; \"S\" b; |} & readonly)'", 122, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE7', found '(record {| \"C\"" +
                " a; \"S\" b; |} & readonly)'", 123, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE7', found '(record {| \"C\"" +
                " a; \"C\" b; \"S\" c; |} & readonly)'", 124, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'TYPE7', found " +
                "'(record {| record {| record {| 1 a; |} b; |} a; record {| record {| 1 a; |} a; record {| 2 b; 3 c; " +
                "|} CMI2; record {| 1 d; |} c; |} b; |} & readonly)'", 125, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'record {| readonly 1 a; |} & " +
                "readonly', found '(record {| 2 b; 3 c; |} & readonly)'", 126, 17);
        BAssertUtil.validateError(compileResult1, i++, "redeclared symbol 'cmi4'", 127, 10);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 128, 17);
        BAssertUtil.validateError(compileResult1, i++, "undefined field 'c' in record 'record {| readonly 0.11f a; " +
                "readonly 2.12f b; |} & readonly'", 128, 28);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '0.11d', found 'float'", 129, 22);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 131, 17);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '\"C\"', found 'string'", 132, 22);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '\"S\"', found 'string'", 132, 31);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'a'", 133, 29);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 133, 29);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'a'", 134, 17);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'CN1'", 135, 15);
        BAssertUtil.validateError(compileResult1, i++, "undefined field 'a' in record 'record {| readonly (record {| " +
                "() a; |} & readonly) CN1; |} & readonly'", 135, 16);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '123', found 'int'", 143, 11);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '123', found 'int'", 144, 11);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '-1', found 'int'", 145, 11);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'int', found 'string'", 155, 9);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'int', found 'string'", 156, 9);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'int', found 'string'", 157, 9);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '123', found '-1'", 161, 20);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '123', found 'int'", 161, 23);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '123', found '-1'", 162, 20);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '123', found 'int'", 162, 23);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '(123|-1)', found 'int'", 163, 21);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '-1', found '123'", 164, 14);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '-1', found '123'", 164, 17);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '-1', found 'int'", 164, 27);
        BAssertUtil.validateError(compileResult1, i++, "expression is not a constant expression", 172, 8);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected " +
                "'record {| string a; anydata...; |}', found 'map<int>'", 176, 26);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'record {| int a; string...; " +
                "|}', found 'map<int>'", 191, 40);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'record {| 1 a; |}', found '" +
                "(record {| 1 a; 2 b; |} & readonly)'", 192, 27);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'record {| readonly (" +
                "record {| 1 a; 2 b; |} & readonly) a; readonly (record {| 3 a; |} & readonly) b; |} & readonly', " +
                "found '(record {| record {| 1 a; 2 b; |} a; record {| 1 a; |} b; |} & readonly)'", 193, 80);
        BAssertUtil.validateError(compileResult1, i++, "cannot update 'readonly' value of type " +
                "'record {| readonly 1 a; readonly 2 b; |} & readonly'", 198, 5);
        BAssertUtil.validateError(compileResult1, i++, "cannot update 'readonly' value of type " +
                "'record {| readonly 1 a; readonly 2 b; |} & readonly'", 199, 5);
        BAssertUtil.validateError(compileResult1, i++, "cannot update 'readonly' value of type 'record {| " +
                                          "readonly (record {| 1 a; 2 b; |} & readonly) a; " +
                                          "readonly (record {| 1 a; |} & readonly) b; |} & readonly'",
                                  202, 5);
        BAssertUtil.validateError(compileResult1, i++, "cannot update 'readonly' value of type 'record {| " +
                                          "readonly (record {| 1 a; 2 b; |} & readonly) a; " +
                                          "readonly (record {| 1 a; |} & readonly) b; |} & readonly'",
                                  203, 5);
        BAssertUtil.validateError(compileResult1, i++, "cannot update 'readonly' value of type 'record {| " +
                                          "readonly (record {| 1 a; 2 b; |} & readonly) a; " +
                                          "readonly (record {| 1 a; |} & readonly) b; |} & readonly'",
                                  204, 5);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '1', found 'int'", 211, 7);

        // https://github.com/ballerina-platform/ballerina-lang/issues/33890
        // BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected '2', found 'int'", 214, 7);
        // BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'a'", 219, 7);
        // BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 219, 7);
        // BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 220, 7);
        // BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'true', found 'boolean'",
        //                          220, 11);

        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'record {| record {| " +
                "\"greetings\" a; \"map\" b; |}...; |}', found '(record {| record {| \"greetings\" a; \"map\" b; |} " +
                "a; record {| \"hello\" x; \"world\" y; |} b; record {| \"from\" x; \"Ballerina\" z; \"!\" b; |} c; " +
                "|} & readonly)'", 241, 59);

        // https://github.com/ballerina-platform/ballerina-lang/issues/33890
        // BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'a'", 243, 7);
        // BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 243, 7);
        // BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'c'", 243, 7);

        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'record {| record {| " +
                "\"greetings\" a; \"map\" b; |}...; |}', found '(record {| record {| \"greetings\" a; \"map\" b; |} " +
                "a; record {| \"hello\" x; \"world\" y; |} b; record {| \"from\" x; \"Ballerina\" z; \"!\" b; |} c; " +
                "|} & readonly)'", 259, 63);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'a'", 261, 11);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'b'", 261, 11);
        BAssertUtil.validateError(compileResult1, i++, "missing non-defaultable required record field 'c'", 261, 11);

        Assert.assertEquals(compileResult1.getErrorCount(), i);
    }

    @Test
    public void constExpressionCodeAnalysisNegative() {
        CompileResult compileResult1 = BCompileUtil.compile(
                "test-src/types/constant/constant_code_analysis_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: '1' will not be matched to '123'", 24, 8);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: '1' will not be matched to '123'", 24, 18);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: '1' will not be matched to '-1'", 24, 28);
        Assert.assertEquals(compileResult1.getErrorCount(), i);
    }
}
