/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.typeassertion;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;

/**
 * Class to test type assertion expressions.
 *
 * @since 0.985.0
 */
public class TypeAssertionExpressionsTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typeassertion/type_assertion_expr.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/typeassertion/type_assertion_expr_negative.bal");
    }

    @Test(dataProvider = "positiveTests")
    public void testAssertionPositive(String functionName) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected assertion to succeed and return the " +
                "original value");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected '\\(\\)', found 'string'.*")
    public void testNilAssertionNegative() {
        BRunUtil.invoke(result, "testNilAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'string', found '\\(\\)'.*")
    public void testNilValueAssertionAsSimpleBasicTypeNegative() {
        BRunUtil.invoke(result, "testNilValueAssertionAsSimpleBasicTypeNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'map<string>', found '\\(\\)'.*")
    public void testNilValueAssertionAsStructuredTypeNegative() {
        BRunUtil.invoke(result, "testNilValueAssertionAsStructuredTypeNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeAssertionError \\{\"message\":\"assertion " +
                    "error: expected 'string\\[2\\]', found 'string\\|int\\|null\\[2\\]'\"\\}.*")
    public void testArrayAssertionNegative() {
        BRunUtil.invoke(result, "testArrayAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected '\\(string,int,float\\)', found " +
                    "'\\(string,int\\|string,float\\)'.*")
    public void testTupleAssertionNegative() {
        BRunUtil.invoke(result, "testTupleAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'json', found 'int'.*")
    public void testJsonAssertionNegative() {
        BRunUtil.invoke(result, "testJsonAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'map<string>', found 'map'.*")
    public void testMapAssertionNegative() {
        BRunUtil.invoke(result, "testMapAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'Lead', found 'Employee'.*")
    public void testRecordAssertionNegative() {
        BRunUtil.invoke(result, "testRecordAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'table<TableEmployeeTwo>', found " +
                    "'table<TableEmployee>'.*")
    public void testTableAssertionNegative() {
        BRunUtil.invoke(result, "testTableAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'xml', found 'string'.*")
    public void testXmlAssertionNegative() {
        BRunUtil.invoke(result, "testXmlAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: assertion error: expected 'error', found 'MyError'.*",
            enabled = false)
    public void testErrorAssertionNegative() {
        BRunUtil.invoke(result, "testErrorAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
        expectedExceptionsMessageRegExp = ".*assertion error: expected 'function \\(string\\) returns \\" +
                "(string\\)', found 'function \\(string,int\\) returns \\(string\\)'.*")
    public void testFunctionAssertionNegative() {
        BRunUtil.invoke(result, "testFunctionAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: assertion error: expected 'function \\(string\\) returns \\" +
                    "(string\\)', found 'function \\(string,int\\) returns \\(string\\)'.*", enabled = false)
    public void testFutureAssertionNegative() {
        BRunUtil.invoke(result, "testFutureAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'LeadObject', found 'EmployeeObject'.*")
    public void testObjectAssertionNegative() {
        BRunUtil.invoke(result, "testObjectAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
        expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeAssertionError \\{\"message\":\"assertion error:" +
                " expected 'stream<json>', found 'stream<int>'\"\\}.*")
    public void testStreamAssertionNegative() {
        BRunUtil.invoke(result, "testStreamAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'int', found 'typedesc'.*")
    public void testTypedescAssertionNegative() {
        BRunUtil.invoke(result, "testTypedescAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'map<json>', found 'map<string>'.*")
    public void testMapElementAssertionNegative() {
        BRunUtil.invoke(result, "testMapElementAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'int', found 'string'.*")
    public void testListElementAssertionNegative() {
        BRunUtil.invoke(result, "testListElementAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeAssertionError \\{\"message\":\"assertion " +
                    "error: expected 'stream<float\\|json>', found 'stream<int\\|float>'\"\\}.*")
    public void testOutOfOrderUnionConstraintAssertionNegative() {
        BRunUtil.invoke(result, "testOutOfOrderUnionConstraintAssertionNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'int', found 'string'.*")
    public void testStringAsInvalidBasicType() {
        BRunUtil.invoke(result, "testStringAsInvalidBasicType");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*assertion error: expected 'PersonObject', found " +
                    "'EmployeeObject'.*")
    public void testBroaderObjectAssertion() {
        BRunUtil.invoke(result, "testBroaderObjectAssertion");
    }

    @Test
    public void testAssertionPanicWithCheckTrap() {
        BValue[] returns = BRunUtil.invoke(result, "testAssertionPanicWithCheckTrap");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) returns[0]).details).get("message").stringValue(),
                            "assertion error: expected 'PersonObject', found 'EmployeeObject'");
    }

    @Test
    public void testAssertionNegatives() {
        Assert.assertEquals(resultNegative.getErrorCount(), 3);
        int errIndex = 0;
        validateError(resultNegative, errIndex++, "incompatible types: 'Def' cannot be explicitly typed as 'Abc'",
                      19, 15);
        validateError(resultNegative, errIndex++, "incompatible types: 'map<int>' cannot be explicitly typed as 'map'",
                      22, 19);
        validateError(resultNegative, errIndex, "type assertion not yet supported for type 'future<int>'", 28, 22);
    }

    @DataProvider
    public Object[][] positiveTests() {
        return new Object[][]{
                {"testNilAssertionPositive"},
                {"testStringAssertionPositive"},
                {"testIntAssertionPositive"},
                {"testFloatAssertionPositive"},
                {"testDecimalAssertionPositive"},
                {"testBooleanAssertionPositive"},
                {"testArrayAssertionPositive"},
                {"testTupleAssertionPositive"},
                {"testJsonAssertionPositive"},
                {"testMapAssertionPositive"},
                {"testRecordAssertionPositive"},
                {"testTableAssertionPositive"},
                {"testXmlAssertionPositive"},
//                {"testErrorAssertionPositive"},
                {"testFunctionAssertionPositive"},
//                {"testFutureAssertionPositive"},
                {"testObjectAssertionPositive"},
                {"testStreamAssertionPositive"},
                {"testTypedescAssertionPositive"},
                {"testMapElementAssertionPositive"},
                {"testListElementAssertionPositive"},
                {"testOutOfOrderUnionConstraintAssertionPositive"}
        };
    }
}
