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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

import java.util.List;

/**
 * Test cases for the lang.regexp library.
 *
 * @since 2201.3.0
 */
public class LangLibRegexpTest {

    private CompileResult compileResult, negativeTests;
    private static final String NEW_LINE_CHAR = System.lineSeparator();

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
                "testSplit",
                "testEmptyRegexpFind",
                "testRegexpFromString",
                "testEmptyRegexpMatch",
                "testTranslatingDiffNodesInCharClass",
                "testRegexpInterpolation",
                "testRegexpInterpolationNegative",
                "testCharClassesWithMultipleRangesAndAtoms",
                "testModuleLevelPatterns",
                "testTranslatingDiffNodesInCharClass",
                "testRegexpWithUnicodeChars",
        };
    }

    @Test(dataProvider = "negativeRegexpIndexProvider")
    public void testNegativeRegexp(String functionName) {
        BRunUtil.invoke(negativeTests, functionName);
    }

    @DataProvider(name = "negativeRegexpIndexProvider")
    private Object[][] negativeRegexpIndexes() {
        return new Object[][] {
                {"testNegativeIndexFind"},
                {"testNegativeIndexFindAll"},
                {"testNegativeIndexFindGroups"},
                {"testNegativeIndexFindAllGroups"},
                {"testNegativeIndexMatchAt"},
                {"testNegativeIndexMatchGroupsAt"},
                {"testNegativeIndexReplace"},
                {"testNegativeIndexReplaceAll"}
        };
    }

    @Test(dataProvider = "invalidRegexpIndexProvider")
    public void testInvalidRegexp(String functionName) {
        BRunUtil.invoke(negativeTests, functionName);
    }

    @DataProvider(name = "invalidRegexpIndexProvider")
    private Object[][] invalidRegexpIndexes() {
        return new Object[][] {
                {"testInvalidIndexFind"},
                {"testInvalidIndexFindAll"},
                {"testInvalidIndexFindGroups"},
                {"testInvalidIndexFindAllGroups"},
                {"testInvalidIndexMatchAt"},
                {"testInvalidIndexMatchGroupsAt"},
                {"testInvalidIndexReplace"},
                {"testInvalidIndexReplaceAll"}
        };
    }

    @Test(dataProvider = "longRegexpIndexProvider")
    public void testLongIndexRegexp(String functionName) {
        BRunUtil.invoke(negativeTests, functionName);
    }

    @DataProvider(name = "longRegexpIndexProvider")
    private Object[][] longRegexpIndexes() {
        return new Object[][] {
                {"testLongIndexFind"},
                {"testLongIndexFindAll"},
                {"testLongIndexFindGroups"},
                {"testLongIndexFindAllGroups"},
                {"testLongIndexMatchAt"},
                {"testLongIndexMatchGroupsAt"},
                {"testLongIndexReplace"},
                {"testLongIndexReplaceAll"}
        };
    }

    @Test(dataProvider = "invalidRegexpPatternSyntaxProvider")
    public void testInvalidRegexpPatternSyntax(String functionName, String message) {
        Object returns = BRunUtil.invoke(negativeTests, functionName);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina}RegularExpressionParsingError\",message=\"invalid" +
                " regexp pattern: " + message + "\")");
    }

    @DataProvider(name = "invalidRegexpPatternSyntaxProvider")
    private Object[][] getInvalidRegexpPatternSyntax() {
        return new Object[][] {
                {"testInvalidRegexpPatternSyntax1", "Unclosed character class near index 24" + NEW_LINE_CHAR +
                        "(?i-s:[[A\\\\sB\\WC\\Dd\\\\]\\])" + NEW_LINE_CHAR + "                        ^"},
                {"testInvalidRegexpPatternSyntax2", "Unclosed character class near index 27" + NEW_LINE_CHAR +
                        "(?xsmi:[[ABC]\\P{sc=Braille})" + NEW_LINE_CHAR + "                           ^"},
                {"testInvalidRegexpPatternSyntax3", "Unclosed character class near index 27" + NEW_LINE_CHAR +
                        "(?xsmi:[[ABC]\\P{sc=Braille})" + NEW_LINE_CHAR + "                           ^"},
        };
    }

    @Test
    public void testEmptyRegexpCompilationError() {
        CompileResult errResult = BCompileUtil.compile("test-src/regexp_empty_test_negative.bal");
        List<Pair<Integer, Integer>> errorIndexes = getErrorIndexes();
        int errCount = errorIndexes.size();
        Assert.assertEquals(errResult.getErrorCount(), errCount);
        for (int i = 0; i < errCount; i++) {
            BAssertUtil.validateError(errResult, i, "regular expression is not allowed: empty RegExp",
                    errorIndexes.get(i).first(), errorIndexes.get(i).second());
        }
    }

    private List<Pair<Integer, Integer>> getErrorIndexes() {
        return List.of(
                Pair.of(20, 12),
                Pair.of(21, 29),
                Pair.of(22, 29),
                Pair.of(23, 27),
                Pair.of(24, 27),
                Pair.of(25, 30),
                Pair.of(26, 30),
                Pair.of(27, 33),
                Pair.of(28, 33),
                Pair.of(29, 40),
                Pair.of(30, 40),
                Pair.of(31, 32),
                Pair.of(32, 32),
                Pair.of(33, 12),
                Pair.of(34, 12)
        );
    }
    
    @Test(dataProvider = "negativeRegexpNonCapturingGroupProvider")
    public void negativeTestRegexpEmptyCharClass(String functionName) {
        BRunUtil.invoke(negativeTests, functionName);
    }

    @DataProvider(name = "negativeRegexpNonCapturingGroupProvider")
    private Object[][] negativeRegexpEmptyCharClass() {
        return new Object[][]{
                {"testNegativeDuplicateFlags1"},
                {"testNegativeDuplicateFlags2"},
                {"testNegativeDuplicateFlags3"},
                {"testNegativeDuplicateFlags4"},
                {"testNegativeDuplicateFlags5"},
                {"testNegativeInvalidFlags1"},
                {"testNegativeInvalidFlags2"},
                {"testNegativeInvalidFlags3"},
                {"testNegativeInvalidFlags4"}
        };
    }
    
    @Test(dataProvider = "negativeRegexpEmptyCharClassInterpolationProvider")
    public void negativeTestRegexpEmptyCharClassInsertion(String functionName) {
        BRunUtil.invoke(negativeTests, functionName);
    }

    @DataProvider(name = "negativeRegexpEmptyCharClassInterpolationProvider")
    private Object[][] negativeRegexpEmptyCharClassInsertion() {
        return new Object[][] {
                {"testNegativeEmptyCharClass1"},
                {"testNegativeEmptyCharClass2"},
                {"testNegativeEmptyCharClass3"}
        };
    }
}
