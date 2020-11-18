/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.record;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
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
 * Test cases for user defined closed record types in Ballerina.
 */
public class ClosedRecordTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/closed_record.bal");
    }

    @Test(description = "Test using expressions as index for record arrays")
    public void testExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");
    }

    @Test(description = "Test using records inside records")
    public void testStructOfStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "USA");
    }

    @Test(description = "Test returning fields of a record")
    public void testReturnStructAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test using record expression as a index in another record expression")
    public void testStructExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test default value of a record field")
    public void testDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultVal");

        // Check default value of a field where the default value is set
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "");

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested record field")
    public void testNestedFieldDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Smith");

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested record field")
    public void testNestedStructInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedStructInit");

        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> person = ((BMap<String, BValue>) returns[0]);
        Assert.assertEquals(person.get("name").stringValue(), "aaa");
        Assert.assertEquals(((BInteger) person.get("age")).intValue(), 25);

        Assert.assertTrue(person.get("parent") instanceof BMap);
        BMap<String, BValue> parent = ((BMap<String, BValue>) person.get("parent"));
        Assert.assertEquals(parent.get("name").stringValue(), "bbb");
        Assert.assertEquals(((BInteger) parent.get("age")).intValue(), 50);
    }

    @Test(description = "Test negative default values in record")
    public void testNegativeDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getStructNegativeValues");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BFloat.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -9);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -8);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), -88.234);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), -24.99);
    }

    @Test(description = "Test negative default values in record")
    public void testStructToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getStruct");
        Assert.assertEquals(returns[0].stringValue(), "{name:\"aaa\", lname:\"\", adrs:{}, age:25, " +
                "family:{spouse:\"\", noOfChildren:0, children:[]}, parent:{name:\"bbb\", lname:\"ccc\", " +
                "adrs:{}, age:50, family:{spouse:\"\", noOfChildren:0, children:[]}, parent:()}}");
    }

    @Test
    public void testStructLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/sealed_record_literals.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns[0].stringValue(), "{dptName:\"\", employees:[], manager:" +
                "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:()}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns[0].stringValue(),
                "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:()}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile("test-src/record/nested_sealed_record_inline_init.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns[0].stringValue(),
                "{name:\"default first name\", fname:\"\", lname:\"Doe\", adrs:{}, age:999, " +
                        "family:{spouse:\"Jane\", noOfChildren:0, children:[\"Alex\", \"Bob\"]}}");
    }

    @Test(description = "Negative test to test attaching functions to record literal",
            groups = { "disableOnOldParser" })
    public void testStructLiteralAttachedFunc() {
        CompileResult result =
                BCompileUtil.compile("test-src/record/sealed_record_literal_with_attached_functions_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "redeclared symbol 'Person'", 7, 10);
        BAssertUtil.validateError(result, 1, "invalid token '.'", 7, 24);
        BAssertUtil.validateError(result, 2, "invalid token 'getName'", 7, 24);
        BAssertUtil.validateError(result, 3, "undefined symbol 'self'", 8, 12);
    }

    @Test(description = "Test for records defined using the 'record' keyword")
    public void testRecordDefinedWithKeyword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructWithRecordKeyword");

        Assert.assertEquals(returns[0].stringValue(), "{name:\"John\", lname:\"Doe\", address:{\"country\":\"USA\", " +
                "\"state\":\"CA\"}, age:25, family:{spouse:\"\", noOfChildren:0, children:[]}, parent:(), " +
                "designation:\"Software Engineer\"}");
    }

    @Test(description = "Test function pointer as a record field")
    public void testFuncPtrAsAField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFuncPtrAsRecordField");
        Assert.assertEquals(returns[0].stringValue(), "Doe, John");
    }

    @Test
    public void testAmbiguityResolution() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAmbiguityResolution");
        Assert.assertEquals(returns[0].stringValue(), "In-memory mode configuration");
        Assert.assertEquals(returns[1].stringValue(), "Server mode configuration");
        Assert.assertEquals(returns[2].stringValue(), "Embedded mode configuration");
    }

    @Test
    public void testAmbiguityResolution2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAmbiguityResolution2");
        Assert.assertEquals(returns[0].stringValue(), "A");
        Assert.assertEquals(returns[1].stringValue(), "B");
        Assert.assertEquals(returns[2].stringValue(), "B");
        Assert.assertEquals(returns[3].stringValue(), "C");
    }

    @Test
    public void testEmptyClosedRecords() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyClosedRecords");
        Assert.assertEquals(returns[0].stringValue(), "{}");
        Assert.assertEquals(returns[1].stringValue(), "{}");
        Assert.assertEquals(returns[2].stringValue(), "{}");
        Assert.assertEquals(returns[3].stringValue(), "{}");
    }

    @Test(description = "Test white space between the type name and ellipsis in rest descriptor",
            groups = { "disableOnOldParser" })
    public void testRestDescriptorSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/record/closed_record_invalid_delimiter.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "missing close brace pipe token", 5, 1);
        BAssertUtil.validateError(result, i++, "invalid token '}'", 5, 2);
        BAssertUtil.validateError(result, i++, "missing close brace token", 11, 1);
        BAssertUtil.validateError(result, i++, "invalid token '|}'", 11, 3);
        BAssertUtil.validateError(result, i++, "missing type desc", 13, 23);
        BAssertUtil.validateError(result, i++, "invalid token '|'", 17, 3);
        BAssertUtil.validateError(result, i++, "missing close brace pipe token", 19, 25);
        BAssertUtil.validateError(result, i++, "invalid token '|'", 19, 28);
        BAssertUtil.validateError(result, i++, "invalid token '}'", 19, 28);
        BAssertUtil.validateError(result, i++, "invalid token '||'", 21, 27);
        BAssertUtil.validateError(result, i++, "invalid token '||'", 23, 28);
        BAssertUtil.validateError(result, i++, "invalid token '|'", 25, 29);
        BAssertUtil.validateError(result, i++, "invalid token '|'", 25, 29);;
    }

    @Test(description = "Test ambiguous type resolution negative cases")
    public void testAmbiguityResolutionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/record/closed_record_ambiguous_types_negative.bal");
        BAssertUtil.validateError(result, 0, "ambiguous type '(InMemoryModeConfig|ServerModeConfig" +
                        "|EmbeddedModeConfig)'", 36, 24);
    }

    @Test(description = "Test invocation of nil-able function pointer fields in a closed record")
    public void testNilableFunctionPtrInvocation() {
        CompileResult result = BCompileUtil.compile("test-src/record/negative/closed_record_nil-able_fn_ptr.bal");
        String errMsg1 = "function invocation on type 'function (string,string) returns (string)?' is not supported";
        String errMsg2 = "incompatible types: expected 'string?', found 'other'";
        int indx = 0;
        BAssertUtil.validateError(result, indx++, errMsg1, 28, 17);
        BAssertUtil.validateError(result, indx++, errMsg2, 28, 17);
        BAssertUtil.validateError(result, indx++, errMsg1, 33, 17);
        BAssertUtil.validateError(result, indx++, errMsg2, 33, 17);
        BAssertUtil.validateError(result, indx++, errMsg1, 47, 17);
        BAssertUtil.validateError(result, indx++, errMsg2, 47, 17);
        BAssertUtil.validateError(result, indx++, errMsg1, 52, 17);
        BAssertUtil.validateError(result, indx, errMsg2, 52, 17);
    }

    @Test
    public void testLiteralsAsMappingConstructorKeys() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLiteralsAsMappingConstructorKeys");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testExpressionsAsKeys() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsKeys");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());

        returns = BRunUtil.invoke(compileResult, "testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testInvalidExprsAsRecordLiteralKeys() {
        CompileResult result = BCompileUtil.compile("test-src/record/closed_record_invalid_key_expr_negative.bal");
        int index = 0;

        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'float'", 34, 27);
        BAssertUtil.validateError(result, index++, "missing non-defaultable required record field 's'", 35, 14);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'int'", 36, 27);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'boolean'", 37, 37);
        BAssertUtil.validateError(result, index++, "missing non-defaultable required record field 's'", 38, 14);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(string|int)', found 'error'",
                                  41, 44);
        BAssertUtil.validateError(result, index++, "undefined field 'invalid' in record 'Foo'", 45, 26);
        BAssertUtil.validateError(result, index++, "undefined field 'x' in record 'Foo'", 46, 28);
        BAssertUtil.validateError(result, index++, "undefined field 'y' in record 'Foo'", 46, 36);
        BAssertUtil.validateError(result, index++, "undefined field 'z' in record 'Foo'", 46, 48);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testOptionalRecordRemove() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeOptional");
    }

    @Test
    public void testRestRecordRemove() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeRest");
    }

    @Test
    public void removeIfHasKeyOptional() {
        BRunUtil.invoke(compileResult, "removeIfHasKeyOptional");
    }

    @Test
    public void removeIfHasKeyRest() {
        BRunUtil.invoke(compileResult, "removeIfHasKeyRest");
    }
}
