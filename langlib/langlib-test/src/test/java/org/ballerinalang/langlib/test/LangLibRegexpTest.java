/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for the lang.regexp library.
 *
 * @since 2201.3.0
 */
public class LangLibRegexpTest {

    private CompileResult compileResult, negativeTests;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/regexp_test.bal");
        negativeTests = BCompileUtil.compile("test-src/regexp_negative_test.bal");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test(dataProvider = "testRegexLangLibFunctionList")
    public void testRegexLibFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "testRegexLangLibFunctionList")
    public Object[] testRegexLangLibFunctions() {
        return new Object[]{
                "testFind",
                "testFindGroups",
                "testFindAll",
                "testMatchAt",
                "testMatchGroupsAt",
                "testIsFullMatch",
                "testFullMatchGroups",
                "testReplaceAll",
                "testReplace",
                "testFindAllGroups",
                "testFromString",
                "testFromStringNegative",
                "testLangLibFuncWithNamedArgExpr",
                "testSplit"
        };
    }

    @Test(dataProvider = "negativeRegexpFindIndexProvider")
    public void testNegativeRegexp(String functionName, int startIndex, int length) {
        Object returns = BRunUtil.invoke(negativeTests, functionName);
        Assert.assertEquals(returns.toString(),
                "error(\"IndexOutOfRange\",message=\"invalid start index '" + startIndex + "' provided in string of " +
                        "length '" + length + "', for regular expression find operation.\")");
    }

    @DataProvider(name = "negativeRegexpFindIndexProvider")
    private Object[][] negativeRegexpFindIndexes() {
        return new Object[][] {
                {"testNegativeIndexFind", -3, 5},
                {"testInvalidIndexFind", 12, 5},
                {"testNegativeIndexFindAll", -8, 51},
                {"testInvalidIndexFindAll", 112, 63},
                {"testNegativeIndexFindGroups", -3, 52},
                {"testInvalidIndexFindGroups", 97, 52},
                {"testNegativeIndexFindAllGroups", -4, 31},
                {"testInvalidIndexFindAllGroups", 123, 31},
        };
    }
}
