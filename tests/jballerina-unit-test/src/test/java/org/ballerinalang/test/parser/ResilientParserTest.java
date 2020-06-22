/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.parser;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the resilient parsing.
 *
 * @since 2.0.0
 */
public class ResilientParserTest {

    @Test(groups = { "disableOnOldParser" })
    public void testResilientParsing() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resilient-parser.bal");
        Assert.assertEquals(result.getErrorCount(), 20);

        BAssertUtil.validateError(result, 0, "missing function name", 1, 17);
        BAssertUtil.validateError(result, 1, "missing identifier", 5, 25);
        BAssertUtil.validateError(result, 2, "missing identifier", 9, 29);
        BAssertUtil.validateError(result, 3, "missing type desc", 9, 29);
        BAssertUtil.validateError(result, 4, "redeclared symbol 'foo2'", 13, 17);
        BAssertUtil.validateError(result, 5, "missing identifier", 13, 29);
        BAssertUtil.validateError(result, 6, "redeclared symbol 'x1'", 15, 9);
        BAssertUtil.validateError(result, 7, "incompatible types: expected '()', found 'int'", 19, 12);
        BAssertUtil.validateError(result, 8, "missing type desc", 22, 37);
        BAssertUtil.validateError(result, 9, "missing identifier", 25, 29);
        BAssertUtil.validateError(result, 10, "missing type desc", 25, 29);
        BAssertUtil.validateError(result, 11, "missing identifier", 25, 47);
        BAssertUtil.validateError(result, 12, "missing identifier", 27, 25);
        BAssertUtil.validateError(result, 13, "missing close bracket token", 28, 11);
        BAssertUtil.validateError(result, 14, "missing close bracket token", 29, 18);
        BAssertUtil.validateError(result, 15, "undefined symbol 'ar'", 30, 5);
        BAssertUtil.validateError(result, 16, "undefined symbol 'arr1'", 31, 13);
        BAssertUtil.validateError(result, 17, "invalid token ']'", 31, 18);
        BAssertUtil.validateError(result, 18, "missing equal token", 43, 12);
        BAssertUtil.validateError(result, 19, "undefined symbol 'ff'", 44, 13);
    }
}
