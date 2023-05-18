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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Jane");
    }

    @Test(description = "Test using records inside records")
    public void testStructOfStructs() {
        Object returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "USA");
    }

    @Test(description = "Test returning fields of a record")
    public void testReturnStructAttributes() {
        Object returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test using record expression as a index in another record expression")
    public void testStructExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test default value of a record field")
    public void testDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDefaultVal");

        // Check default value of a field where the default value is set
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
    }

    @Test(description = "Test default value of a nested record field")
    public void testNestedFieldDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Smith");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
    }

    @Test(description = "Test default value of a nested record field")
    public void testNestedStructInit() {
        Object returns = BRunUtil.invoke(compileResult, "testNestedStructInit");

        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> person = ((BMap<String, Object>) returns);
        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "aaa");
        Assert.assertEquals((person.get(StringUtils.fromString("age"))), 25L);

        Assert.assertTrue(person.get(StringUtils.fromString("parent")) instanceof BMap);
        BMap<String, Object> parent = ((BMap<String, Object>) person.get(StringUtils.fromString("parent")));
        Assert.assertEquals(parent.get(StringUtils.fromString("name")).toString(), "bbb");
        Assert.assertEquals((parent.get(StringUtils.fromString("age"))), 50L);
    }

    @Test(description = "Test negative default values in record")
    public void testNegativeDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getStructNegativeValues");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        Assert.assertSame(returns.get(2).getClass(), Double.class);
        Assert.assertSame(returns.get(3).getClass(), Double.class);
        Assert.assertEquals(returns.get(0), -9L);
        Assert.assertEquals(returns.get(1), -8L);
        Assert.assertEquals(returns.get(2), -88.234);
        Assert.assertEquals(returns.get(3), -24.99);
    }

    @Test(description = "Test negative default values in record")
    public void testStructToString() {
        Object returns = BRunUtil.invoke(compileResult, "getStruct");
        Assert.assertEquals(returns.toString(), "{\"name\":\"aaa\",\"lname\":\"\",\"adrs\":{},\"age\":25," +
                "\"family\":{\"spouse\":\"\",\"noOfChildren\":0,\"children\":[]},\"parent\":{\"name\":\"bbb\"," +
                "\"lname\":\"ccc\",\"adrs\":{},\"age\":50,\"family\":{\"spouse\":\"\",\"noOfChildren\":0," +
                "\"children\":[]},\"parent\":null}}");
    }

    @Test
    public void testStructLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/sealed_record_literals.bal");
        Object returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns.toString(), "{\"dptName\":\"\",\"employees\":[],\"manager\":{\"name\":\"default " +
                "first name\",\"lname\":\"\",\"adrs\":{},\"age\":999,\"child\":null}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"default first name\",\"lname\":\"\",\"adrs\":{},\"age\":999,\"child\":null}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile("test-src/record/nested_sealed_record_inline_init.bal");
        Object returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"default first name\",\"fname\":\"\",\"lname\":\"Doe\",\"adrs\":{},\"age\":999," +
                        "\"family\":{\"spouse\":\"Jane\",\"noOfChildren\":0,\"children\":[\"Alex\",\"Bob\"]}}");
    }

    @Test(description = "Negative test to test attaching functions to record literal")
    public void testStructLiteralAttachedFunc() {
        CompileResult result =
                BCompileUtil.compile("test-src/record/sealed_record_literal_with_attached_functions_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "redeclared symbol 'Person'", 7, 10);
        BAssertUtil.validateError(result, 1, "resource path in function definition", 7, 16);
        BAssertUtil.validateError(result, 2, "invalid token 'getName'", 7, 17);
        BAssertUtil.validateError(result, 3, "undefined symbol 'self'", 8, 12);
    }

    @Test(description = "Test for records defined using the 'record' keyword")
    public void testRecordDefinedWithKeyword() {
        Object returns = BRunUtil.invoke(compileResult, "testStructWithRecordKeyword");

        Assert.assertEquals(returns.toString(), "{\"name\":\"John\",\"lname\":\"Doe\"," +
                "\"address\":{\"country\":\"USA\",\"state\":\"CA\"},\"age\":25,\"family\":{\"spouse\":\"\"," +
                "\"noOfChildren\":0,\"children\":[]},\"parent\":null,\"designation\":\"Software Engineer\"}");
    }

    @Test(description = "Test function pointer as a record field")
    public void testFuncPtrAsAField() {
        Object returns = BRunUtil.invoke(compileResult, "testFuncPtrAsRecordField");
        Assert.assertEquals(returns.toString(), "Doe, John");
    }

    @Test
    public void testAmbiguityResolution() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAmbiguityResolution");
        Assert.assertEquals(returns.get(0).toString(), "In-memory mode configuration");
        Assert.assertEquals(returns.get(1).toString(), "Server mode configuration");
        Assert.assertEquals(returns.get(2).toString(), "Embedded mode configuration");
    }

    @Test
    public void testAmbiguityResolution2() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAmbiguityResolution2");
        Assert.assertEquals(returns.get(0).toString(), "A");
        Assert.assertEquals(returns.get(1).toString(), "B");
        Assert.assertEquals(returns.get(2).toString(), "B");
        Assert.assertEquals(returns.get(3).toString(), "C");
    }

    @Test
    public void testEmptyClosedRecords() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testEmptyClosedRecords");
        Assert.assertEquals(returns.get(0).toString(), "{}");
        Assert.assertEquals(returns.get(1).toString(), "{}");
        Assert.assertEquals(returns.get(2).toString(), "{}");
        Assert.assertEquals(returns.get(3).toString(), "{}");
    }

    @Test(description = "Test white space between the type name and ellipsis in rest descriptor")
    public void testRestDescriptorSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/record/closed_record_invalid_delimiter.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "invalid token '}'", 5, 1);
        BAssertUtil.validateError(result, i++, "missing close brace pipe token", 5, 2);
        BAssertUtil.validateError(result, i++, "invalid token '|}'", 11, 1);
        BAssertUtil.validateError(result, i++, "missing close brace token", 11, 3);
        BAssertUtil.validateError(result, i++, "missing type descriptor", 13, 23);
        BAssertUtil.validateError(result, i++, "invalid token '|'", 17, 1);
        BAssertUtil.validateError(result, i++, "invalid token '|'", 19, 25);
        BAssertUtil.validateError(result, i++, "invalid token '}'", 19, 27);
        BAssertUtil.validateError(result, i++, "missing close brace pipe token", 19, 28);
        BAssertUtil.validateError(result, i++, "invalid token '||'", 21, 25);
        BAssertUtil.validateError(result, i++, "invalid token '||'", 23, 25);
        BAssertUtil.validateError(result, i++, "missing close brace token", 25, 25);
        BAssertUtil.validateError(result, i++, "missing type descriptor", 25, 27);
        ;
        BAssertUtil.validateError(result, i++, "missing object keyword", 25, 29);
        ;
        BAssertUtil.validateError(result, i, "missing open brace token", 25, 29);
        ;
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
        String errMsg = "undefined function 'fp'";
        int indx = 0;
        BAssertUtil.validateError(result, indx++, errMsg, 29, 17);
        BAssertUtil.validateError(result, indx++, errMsg, 35, 17);
        Assert.assertEquals(result.getErrorCount(), indx);
    }

    @Test(description = "Test closed record mismatch fields")
    public void testClosedRecordMismatchFields() {
        CompileResult result = BCompileUtil.compile("test-src/record/negative/closed_record_mismatch_fields.bal");
        int indx = 0;
        BAssertUtil.validateError(result, indx++, "missing error detail arg for error detail field 'foo'",
                18, 27);
        BAssertUtil.validateError(result, indx++, "missing error detail arg for error detail field 'message'",
                18, 27);
        BAssertUtil.validateError(result, indx, "unknown error detail arg 'bar' passed to closed error " +
                "detail type 'FooErrData'", 18, 53);
    }

    @Test
    public void testLiteralsAsMappingConstructorKeys() {
        Object returns = BRunUtil.invoke(compileResult, "testLiteralsAsMappingConstructorKeys");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testExpressionsAsKeys() {
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsKeys");
        Assert.assertTrue((Boolean) returns);

        returns = BRunUtil.invoke(compileResult, "testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames");
        Assert.assertTrue((Boolean) returns);
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
        Object returns = BRunUtil.invoke(compileResult, "removeOptional");
    }

    @Test
    public void testRestRecordRemove() {
        Object returns = BRunUtil.invoke(compileResult, "removeRest");
    }

    @Test
    public void removeIfHasKeyOptional() {
        BRunUtil.invoke(compileResult, "removeIfHasKeyOptional");
    }

    @Test
    public void removeIfHasKeyRest() {
        BRunUtil.invoke(compileResult, "removeIfHasKeyRest");
    }

    @Test
    public void testCyclicRecordViaFields() {
        CompileResult cyclicBal = BCompileUtil.compile("test-src/record/cyclic_record_via_fields.bal");
        BRunUtil.invoke(cyclicBal, "testCyclicRecordResolution");
    }

    @Test
    public void testOverridingIncludedFieldInRecordWithReadOnlyIntersection() {
        BRunUtil.invoke(compileResult, "testOverridingIncludedFieldInRecordWithReadOnlyIntersection");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
