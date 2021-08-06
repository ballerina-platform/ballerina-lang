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
import org.testng.annotations.AfterClass;
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
                                "'functionWithIncludedRecordParam1()'", 56, 16);
        BAssertUtil.validateError(result, i++, "too many arguments in call to " +
                                            "'functionWithIncludedRecordParam2()'", 57, 105);
        BAssertUtil.validateError(result, i++, "missing required parameter 'firstName' in call to " +
                                "'functionWithIncludedRecordParam2()'", 58, 19);
        BAssertUtil.validateError(result, i++, "missing required parameter 'secondName' in call to " +
                                "'functionWithIncludedRecordParam2()'", 58, 19);
        BAssertUtil.validateError(result, i++, "too many arguments in call to " +
                                            "'functionWithIncludedRecordParam3()'", 59, 95);
        BAssertUtil.validateError(result, i++, "undeclared field 'firstName' in record 'Address'", 67, 12);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'a'", 80, 47);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 81, 51);
        BAssertUtil.validateError(result, i++, "undefined field 'abc' in 'Bar2'", 90, 12);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 104, 75);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 122, 71);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to " +
                                "'functionWithIncludedRecordParam8()'", 136, 18);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 136, 71);
        BAssertUtil.validateError(result, i++, "undefined defaultable parameter 'abc'", 144, 63);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'id'", 161, 53);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'name'", 161, 72);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'string'", 166, 49);
        BAssertUtil.validateError(result, i++, "expected a record type as an included parameter", 169, 15);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'int[]', found 'int'", 179, 36);
        BAssertUtil.validateError(result, i++,
                "required parameter not allowed after included record parameters", 182, 62);
        BAssertUtil.validateError(result, i++,
                "defaultable parameter not allowed after included record parameters", 186, 62);
        BAssertUtil.validateError(result, i++,
                "defaultable parameter not allowed after included record parameters", 190, 69);
        BAssertUtil.validateError(result, i++,
                "defaultable parameter not allowed after included record parameters", 194, 81);
        Assert.assertEquals(i, result.getErrorCount());
    }



    @Test
    public void testFuctionWithIncludedRecordParameters() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters2() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters2");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters3() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters3");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters4() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters4");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters5() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters5");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters6() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters6");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters7() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters7");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters8() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters8");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters9() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters9");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters10() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters10");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters11() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters11");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters12() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters12");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters13() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters13");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters14() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters14");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters15() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters15");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters16() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters16");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters17() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters17");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters18() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters18");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters19() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters19");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters20() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters20");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters21() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters21");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters22() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters22");
    }

    @Test
    public void testFuctionWithIncludedRecordParameters23() {
        BRunUtil.invoke(result, "testFuctionWithIncludedRecordParameters23");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
