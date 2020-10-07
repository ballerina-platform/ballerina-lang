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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {new BInteger(20)};
        BValue[] results = BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 15);

        args = new BValue[]{new BInteger(4)};
        results = BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 5);
    }

    @Test
    public void testInVar() {
        BValue[] args = {new BInteger(100)};
        BValue[] results = BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertEquals(results[0].stringValue(), "large");

        args = new BValue[]{new BInteger(2)};
        results = BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertEquals(results[0].stringValue(), "small");
    }

    @Test
    public void testInVariableDef() {
        BValue[] args = {new BInteger(10)};
        BValue[] results = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertEquals(((BFloat) results[0]).floatValue(), 10.5);

        args = new BValue[]{new BInteger(20)};
        results = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertEquals(((BFloat) results[0]).floatValue(), 9.5);
    }

    @Test
    public void testInIfThen() {
        BValue[] args = {new BInteger(20)};
        BValue[] results = BRunUtil.invoke(compileResult, "test4", args);
        Assert.assertEquals(results[0].stringValue(), "if");

        args = new BValue[]{new BInteger(100)};
        results = BRunUtil.invoke(compileResult, "test4", args);
        Assert.assertEquals(results[0].stringValue(), "else");
    }

    @Test
    public void testInFunctionParamThen() {
        BValue[] args = {new BInteger(10)};
        BValue[] results = BRunUtil.invoke(compileResult, "test5", args);
        Assert.assertEquals(results[0].stringValue(), "10tenfalse");

        args = new BValue[]{new BInteger(100)};
        results = BRunUtil.invoke(compileResult, "test5", args);
        Assert.assertEquals(results[0].stringValue(), "10othertrue");
    }

    @Test
    public void testInStructLiteral() {
        BValue[] args = {new BString("admin")};
        BValue[] results = BRunUtil.invoke(compileResult, "test6", args);
        Assert.assertEquals(results[0].stringValue(), "super");

        args = new BValue[]{new BString("no user")};
        results = BRunUtil.invoke(compileResult, "test6", args);
        Assert.assertEquals(results[0].stringValue(), "tom");
    }

    @Test
    public void testInMapLiteral() {
        BValue[] args = {new BString("one")};
        BValue[] results = BRunUtil.invoke(compileResult, "test7", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);

        args = new BValue[]{new BString("two")};
        results = BRunUtil.invoke(compileResult, "test7", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 2);
    }

    @Test
    public void testInStringTemplateLiteral() {
        BValue[] args = {new BString("world")};
        BValue[] results = BRunUtil.invoke(compileResult, "test8", args);
        Assert.assertEquals(results[0].stringValue(), "hello world...!!");

        args = new BValue[]{new BString("foo")};
        results = BRunUtil.invoke(compileResult, "test8", args);
        Assert.assertEquals(results[0].stringValue(), "hello everyone..!");
    }

    @Test
    public void testInInvocationExpressions() {
        BValue[] args = {new BString("a")};
        BValue[] results = BRunUtil.invoke(compileResult, "test9", args);
        Assert.assertEquals(results[0].stringValue(), "bax");

        args = new BValue[]{new BString("b")};
        results = BRunUtil.invoke(compileResult, "test9", args);
        Assert.assertEquals(results[0].stringValue(), "bar");
    }

    @Test
    public void testInReferenceType() {
        BValue[] args = {new BString("tom")};
        BValue[] results = BRunUtil.invoke(compileResult, "test10", args);
        Assert.assertEquals(results[0].stringValue(), "{name:\"tom\", location:\"US\"}");

        args = new BValue[]{new BString("bob")};
        results = BRunUtil.invoke(compileResult, "test10", args);
        Assert.assertEquals(results[0].stringValue(), "{name:\"bob\", location:\"UK\"}");
    }

    /*  Test cases for Nested ternary   */

    @Test
    public void testNestedTernary1() {
        BInteger[] args = {new BInteger(80)};
        BValue[] results = BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary1Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary1Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(50)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary1Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary1Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(30)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary1Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary1Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(15)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary1", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary1Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary1Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());
    }

    private String getNestedTernary1Value1(long value) {
        return value > 70 ? "morethan70" : value > 40 ? "morethan40" : value > 20 ? "morethan20" : "lessthan20";
    }

    private String getNestedTernary1Value2(long value) {
        return value > 70 ? "morethan70" : (value > 40 ? "morethan40" : (value > 20 ? "morethan20" : "lessthan20"));
    }

    @Test
    public void testNestedTernary2() {
        BInteger[] args = {new BInteger(80)};
        BValue[] results = BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary2Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary2Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(50)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary2Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary2Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(30)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary2Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary2Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(15)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary2", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary2Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary2Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());
    }

    private String getNestedTernary2Value1(long value) {
        return value > 40 ? value > 70 ? "morethan70" : "lessthan70" : value > 20 ? "morethan20" : "lessthan20";
    }

    private String getNestedTernary2Value2(long value) {
        return value > 40 ? (value > 70 ? "morethan70" : "lessthan70") : (value > 20 ? "morethan20" : "lessthan20");
    }

    @Test
    public void testNestedTernary3() {
        BInteger[] args = {new BInteger(80)};
        BValue[] results = BRunUtil.invoke(compileResult, "testNestedTernary3", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary3Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary3Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(35)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary3", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary3Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary3Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(25)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary3", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary3Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary3Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

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
        BInteger[] args = {new BInteger(80)};
        BValue[] results = BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary4Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary4Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(55)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary4Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary4Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(45)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary4Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary4Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(15)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary4", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary4Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary4Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());
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
        BInteger[] args = {new BInteger(80)};
        BValue[] results = BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary5Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary5Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(45)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary5Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary5Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(35)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary5Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary5Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());

        args = new BInteger[]{new BInteger(25)};
        results = BRunUtil.invoke(compileResult, "testNestedTernary5", args);
        Assert.assertEquals(results[0].stringValue(), getNestedTernary5Value1(args[0].value()));
        Assert.assertEquals(results[1].stringValue(), getNestedTernary5Value2(args[0].value()));
        Assert.assertEquals(results[0].stringValue(), results[1].stringValue());
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
        Assert.assertEquals(compileResult.getErrorCount(), 3);
        BAssertUtil.validateError(compileResult, 0, "incompatible types: expected 'string', found 'int'", 6, 29);
        BAssertUtil.validateError(compileResult, 1, "incompatible types: expected 'boolean', found 'int'", 7, 13);
        BAssertUtil.validateError(compileResult, 2, "incompatible types: expected 'string', found 'boolean'", 13, 30);
    }

    @Test
    public void testErrorInTernary() {
        BValue[] results = BRunUtil.invoke(compileResult, "testErrorInTernary");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 7);
    }
}
