/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.ternary;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ternary expressions.
 *
 * @since 0.95.5
 */
public class TernaryExpressionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/ternary/ternary-expr.bal");
    }

    @Test
    public void testInAssignment() {
        Object[] args = {(20)};
        Object results = BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertEquals(results, 15L);

        args = new Object[]{(4)};
        results = BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertEquals(results, 5L);
    }

    @Test
    public void testInVar() {
        Object[] args = {(100)};
        Object results = BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertEquals(results.toString(), "large");

        args = new Object[]{(2)};
        results = BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertEquals(results.toString(), "small");
    }

    @Test
    public void testInVariableDef() {
        Object[] args = {(10)};
        Object results = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertEquals(results, 10.5);

        args = new Object[]{(20)};
        results = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertEquals(results, 9.5);
    }

    @Test
    public void testInIfThen() {
        Object[] args = {(20)};
        Object results = BRunUtil.invoke(compileResult, "test4", args);
        Assert.assertEquals(results.toString(), "if");

        args = new Object[]{(100)};
        results = BRunUtil.invoke(compileResult, "test4", args);
        Assert.assertEquals(results.toString(), "else");
    }

    @Test
    public void testInFunctionParamThen() {
        Object[] args = {(10)};
        Object results = BRunUtil.invoke(compileResult, "test5", args);
        Assert.assertEquals(results.toString(), "10tenfalse");

        args = new Object[]{(100)};
        results = BRunUtil.invoke(compileResult, "test5", args);
        Assert.assertEquals(results.toString(), "10othertrue");
    }

    @Test
    public void testInStructLiteral() {
        Object[] args = {StringUtils.fromString("admin")};
        Object results = BRunUtil.invoke(compileResult, "test6", args);
        Assert.assertEquals(results.toString(), "super");

        args = new Object[]{StringUtils.fromString("no user")};
        results = BRunUtil.invoke(compileResult, "test6", args);
        Assert.assertEquals(results.toString(), "tom");
    }

    @Test
    public void testInMapLiteral() {
        Object[] args = {StringUtils.fromString("one")};
        Object results = BRunUtil.invoke(compileResult, "test7", args);
        Assert.assertEquals(results, 1L);

        args = new Object[]{StringUtils.fromString("two")};
        results = BRunUtil.invoke(compileResult, "test7", args);
        Assert.assertEquals(results, 2L);
    }

    @Test
    public void testInStringTemplateLiteral() {
        Object[] args = {StringUtils.fromString("world")};
        Object results = BRunUtil.invoke(compileResult, "test8", args);
        Assert.assertEquals(results.toString(), "hello world...!!");

        args = new Object[]{StringUtils.fromString("foo")};
        results = BRunUtil.invoke(compileResult, "test8", args);
        Assert.assertEquals(results.toString(), "hello everyone..!");
    }

    @Test
    public void testInInvocationExpressions() {
        Object[] args = {StringUtils.fromString("a")};
        Object results = BRunUtil.invoke(compileResult, "test9", args);
        Assert.assertEquals(results.toString(), "bax");

        args = new Object[]{StringUtils.fromString("b")};
        results = BRunUtil.invoke(compileResult, "test9", args);
        Assert.assertEquals(results.toString(), "bar");
    }

    @Test
    public void testInReferenceType() {
        Object[] args = {StringUtils.fromString("tom")};
        Object results = BRunUtil.invoke(compileResult, "test10", args);
        Assert.assertEquals(results.toString(), "{\"name\":\"tom\",\"location\":\"US\"}");

        args = new Object[]{StringUtils.fromString("bob")};
        results = BRunUtil.invoke(compileResult, "test10", args);
        Assert.assertEquals(results.toString(), "{\"name\":\"bob\",\"location\":\"UK\"}");
    }

    /*  Test cases for Nested ternary   */

    @Test
    public void testNestedTernary1() {
        Long[] args = {80L};
        Object arr = BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0).toString(), getNestedTernary1Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary1Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{50L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary1Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary1Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{30L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary1Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary1Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{15L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary1Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary1Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());
    }

    private String getNestedTernary1Value1(long value) {
        return value > 70 ? "morethan70" : value > 40 ? "morethan40" : value > 20 ? "morethan20" : "lessthan20";
    }

    private String getNestedTernary1Value2(long value) {
        return value > 70 ? "morethan70" : (value > 40 ? "morethan40" : (value > 20 ? "morethan20" : "lessthan20"));
    }

    @Test
    public void testNestedTernary2() {
        Long[] args = {80L};
        Object arr = BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0).toString(), getNestedTernary2Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary2Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{50L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary2Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary2Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{30L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary2Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary2Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{15L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary2Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary2Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());
    }

    private String getNestedTernary2Value1(long value) {
        return value > 40 ? value > 70 ? "morethan70" : "lessthan70" : value > 20 ? "morethan20" : "lessthan20";
    }

    private String getNestedTernary2Value2(long value) {
        return value > 40 ? (value > 70 ? "morethan70" : "lessthan70") : (value > 20 ? "morethan20" : "lessthan20");
    }

    @Test
    public void testNestedTernary3() {
        Long[] args = {80L};
        Object arr = BRunUtil.invoke(compileResult, "testNestedTernary3", args);
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0).toString(), getNestedTernary3Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary3Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{35L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary3", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary3Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary3Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{25L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary3", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary3Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary3Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

    }

    private String getNestedTernary3Value1(long value) {
        return value < 40 ? value > 20 ? value < 30 ? "lessthan30" : "morethan30" : "lessthan20" :
                value > 45 ? "morethan45" : "lessthan45";
    }

    private String getNestedTernary3Value2(long value) {
        return value < 40 ? (value > 20 ? (value < 30 ? "lessthan30" : "morethan30") : "lessthan20") :
                (value > 45 ? "morethan45" : "lessthan45");
    }

    @Test
    public void testNestedTernary4() {
        Long[] args = {80L};
        Object arr = BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0).toString(), getNestedTernary4Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary4Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{55L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary4Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary4Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{45L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary4Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary4Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{15L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary4Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary4Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());
    }

    private String getNestedTernary4Value1(long value) {
        return value > 40 ? value > 70 ? "morethan70" : value > 50 ? "morethan50" : "lessthan50" :
                value > 20 ? "morethan20" : "lessthan20";
    }

    private String getNestedTernary4Value2(long value) {
        return value > 40 ? (value > 70 ? "morethan70" : (value > 50 ? "morethan50" : "lessthan50")) :
                (value > 20 ? "morethan20" : "lessthan20");
    }

    @Test
    public void testNestedTernary5() {
        Long[] args = {80L};
        Object arr = BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0).toString(), getNestedTernary5Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary5Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{45L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary5Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary5Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{35L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary5Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary5Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());

        args = new Long[]{25L};
        results = (BArray) BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results.get(0).toString(), getNestedTernary5Value1(args[0]));
        Assert.assertEquals(results.get(1).toString(), getNestedTernary5Value2(args[0]));
        Assert.assertEquals(results.get(0).toString(), results.get(1).toString());
    }

    private String getNestedTernary5Value1(long value) {
        return value > 50 ? "morethan50" : value > 30 ? value > 40 ? "morethan40" : "lessthan40" :
                value > 20 ? "morethan20" : "lessthan20";
    }

    private String getNestedTernary5Value2(long value) {
        return value > 50 ? "morethan50" : (value > 30 ? (value > 40 ? "morethan40" : "lessthan40") :
                (value > 20 ? "morethan20" : "lessthan20"));
    }

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/expressions/ternary/ternary-expr-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 5L);
        BAssertUtil.validateError(compileResult, 0, "incompatible types: expected 'string', found 'int'", 6, 29);
        BAssertUtil.validateError(compileResult, 1, "incompatible types: expected 'boolean', found 'int'", 7, 13);
        BAssertUtil.validateError(compileResult, 2, "incompatible types: expected 'string', found 'boolean'", 13, 30);
        BAssertUtil.validateError(compileResult, 3, "incompatible types: expected 'int', found 'string'", 21, 26);
        BAssertUtil.validateError(compileResult, 4, "incompatible types: expected 'byte', found 'int'", 24, 27);
    }

    @Test
    public void testErrorInTernary() {
        Object results = BRunUtil.invoke(compileResult, "testErrorInTernary");
        Assert.assertEquals(results, 7L);
    }

    @Test
    public void testPredeclPrefixInTernary() {
        Object results = BRunUtil.invoke(compileResult, "testPredeclPrefixInTernary");
        Assert.assertEquals(results, 11L);
    }

    @Test
    public void testTernaryAsArgument() {
        BRunUtil.invoke(compileResult, "testTernaryAsArgument");
    }

    @Test
    public void testIfAndThenExprBeingFieldAccess() {
        BRunUtil.invoke(compileResult, "testIfAndThenExprBeingFieldAccess");
    }

    @Test
    public void testTernaryWithConfigurableVar() {
        BRunUtil.invoke(compileResult, "testTernaryWithConfigurableVar");
    }

    @Test
    public void testTernaryWithLangValueMethodCallsModuleLevel() {
        BRunUtil.invoke(compileResult, "testTernaryWithLangValueMethodCallsModuleLevel");
    }

    @Test
    public void testTernaryWithLangValueMethodCalls() {
        BRunUtil.invoke(compileResult, "testTernaryWithLangValueMethodCalls");
    }

    @Test
    public void testTernaryWithOtherOperators() {
        BRunUtil.invoke(compileResult, "testTernaryWithOtherOperators");
    }

    @Test
    public void testTernaryInModuleLevel() {
        BRunUtil.invoke(compileResult, "testTernaryInModuleLevel");
    }

    @Test
    public void testTernaryWithQueryWithLocalVariable() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryWithLocalVariable");
    }

    @Test
    public void testTernaryWithQueryWithFunctionParameter() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryWithFunctionParameter");
    }

    @Test
    public void testTernaryWithQueryWithTypeDef() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryWithTypeDef");
    }

    @Test
    public void testTernaryWithQueryWithModuleVariable() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryWithModuleVariable");
    }

    @Test
    public void testTernaryWithQueryForTwoVariables() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryForTwoVariables");
    }

    @Test
    public void testTernaryWithQueryWithFunctionPointers() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryWithFunctionPointers");
    }

    @Test
    public void testTernaryWithQueryWithFunctionAsClosure() {
        BRunUtil.invoke(compileResult, "testTernaryWithQueryWithFunctionAsClosure");
    }

    @Test(description = "Test type narrowing for ternary expression")
    public void testTernaryTypeNarrow() {
        CompileResult compileResult = BCompileUtil.compile("test-src/expressions/ternary/ternary_expr_type_narrow.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                35, 41); // issue #30598, #33217
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                36, 41); // issue #30598, #33217
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'true', found 'boolean'",
                37, 29); // issue #30598, #33217
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '()', found 'boolean?'",
                40, 66); // issue #30598, #33217
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'B', found '(A|B)'",
                120, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'E', found '(D|E)'",
                149, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'F', found '(D|F)'",
                151, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'F', found '(E|F)'",
                153, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '(Y|Z)', found '(W|Y|Z)'",
                262, 35);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'R', found '(Q|R)'",
                288, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '(R|T)', found '(Q|R|T)'",
                291, 32);
        Assert.assertEquals(compileResult.getDiagnostics().length, index);
    }

    @Test(description = "Test type narrowing for ternary expression with no errors")
    public void testTernaryTypeNarrowPositive() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/expressions/ternary/ternary_expr_type_narrow_positive.bal");
        Object returns = BRunUtil.invoke(compileResult, "testTernaryTypeNarrow");
        Assert.assertTrue((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
