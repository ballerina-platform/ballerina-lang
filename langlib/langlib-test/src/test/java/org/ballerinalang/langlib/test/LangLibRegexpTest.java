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
        negativeTests = null;
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
    public void testNegativeRegexp(String functionName) {
        Object returns = BRunUtil.invoke(negativeTests, functionName);
        Assert.assertEquals(returns.toString(),
                "error(\"IndexOutOfRange\",message=\"start index cannot be less than 0\")");
    }

    @DataProvider(name = "negativeRegexpFindIndexProvider")
    private Object[][] negativeRegexpFindIndexes() {
        return new Object[][] {
                {"testNegativeIndexFind"},
                {"testNegativeIndexFindAll"},
                {"testNegativeIndexFindGroups"},
                {"testNegativeIndexFindAllGroups"},
        };
    }

    @Test(dataProvider = "invalidRegexpFindIndexProvider")
    public void testInvalidRegexp(String functionName, int startIndex, int length) {
        Object returns = BRunUtil.invoke(negativeTests, functionName);
        Assert.assertEquals(returns.toString(),
                "error(\"IndexOutOfRange\",message=\"start index '" + startIndex + "' cannot be greater than input " +
                        "string length '" + length + "'\")");
    }

    @DataProvider(name = "invalidRegexpFindIndexProvider")
    private Object[][] invalidRegexpFindIndexes() {
        return new Object[][] {
                {"testInvalidIndexFind", 12, 5},
                {"testInvalidIndexFindAll", 112, 63},
                {"testInvalidIndexFindGroups", 97, 52},
                {"testInvalidIndexFindAllGroups", 123, 31},
        };
    }

    @Test(dataProvider = "longRegexpFindIndexProvider")
    public void testLongIndexRegexp(String functionName, long startIndex) {
        Object returns = BRunUtil.invoke(negativeTests, functionName);
        Assert.assertEquals(returns.toString(),
                String.format("error(\"{ballerina/lang.regexp}RegularExpressionOperationError\",message=\"index " +
                        "number too large: %,d\")", startIndex));
    }

    @DataProvider(name = "longRegexpFindIndexProvider")
    private Object[][] longRegexpFindIndexes() {
        return new Object[][] {
                {"testLongIndexFind", 68719476704L},
                {"testLongIndexFindAll", 137438953408L},
                {"testLongIndexFindGroups", 274877906816L},
                {"testLongIndexFindAllGroups", 549755813632L},
        };
    }
}
