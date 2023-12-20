/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.record;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Test cases for user defined open record types in Ballerina.
 */
public class OpenRecordTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/open_record.bal");
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
        BRunUtil.invoke(compileResult, "testDefaultVal");
    }

    @Test
    public void testWithMultipleTypeInclusions() {
        BRunUtil.invoke(compileResult, "testWithMultipleTypeInclusions");
    }

    @Test
    public void testSpreadOperatorWithOpenRecord() {
        BRunUtil.invoke(compileResult, "testSpreadOperatorWithOpenRecord");
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
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_literals.bal");
        Object returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns.toString(), "{\"dptName\":\"\",\"employees\":[],\"manager\":{\"name\":\"default " +
                "first name\",\"lname\":\"\",\"adrs\":{},\"age\":999,\"child\":null}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"default first name\",\"lname\":\"\",\"adrs\":{},\"age\":999,\"child\":null}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile("test-src/record/nested_record_inline_init.bal");
        Object returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"default first name\",\"fname\":\"\",\"lname\":\"Doe\",\"adrs\":{}," +
                        "\"age\":999,\"family\":{\"spouse\":\"Jane\",\"noOfChildren\":0," +
                        "\"children\":[\"Alex\",\"Bob\"]}}");
    }

    @Test(description = "Negative test to test attaching functions to record literal")
    public void testStructLiteralAttachedFunc() {
        CompileResult result =
                BCompileUtil.compile("test-src/record/record_literal_with_attached_functions_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "redeclared symbol 'Person'", 7, 10);
        BAssertUtil.validateError(result, 1, "resource path in function definition", 7, 16);
        BAssertUtil.validateError(result, 2, "invalid token 'getName'", 7, 17);
        BAssertUtil.validateError(result, 3, "undefined symbol 'self'", 8, 12);
    }

    @Test(description = "Test addition of different types for default rest field type")
    public void testAdditionOfARestField() {
        Object returns = BRunUtil.invoke(compileResult, "testAdditionOfARestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("mname")) instanceof BString);
        Assert.assertEquals(person.get(StringUtils.fromString("mname")).toString(), "Bar");

        Assert.assertTrue(person.get(StringUtils.fromString("height")) instanceof Double);
        Assert.assertEquals((person.get(StringUtils.fromString("height"))), 5.9D);

        Assert.assertTrue(person.get(StringUtils.fromString("firstName")) instanceof BString);
        Assert.assertEquals(person.get(StringUtils.fromString("firstName")).toString(), "John");

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"lname\":\"\",\"adrs\":{},\"age\":25," +
                "\"family\":{\"spouse\":\"\",\"noOfChildren\":0,\"children\":[]},\"parent\":null,\"mname\":\"Bar\"," +
                "\"height\":5.9,\"firstName\":\"John\"}");
    }

    @Test(description = "Test non-existent anydata or error rest field RHS index-based access")
    public void testAnydataOrErrorRestFieldRHSIndexAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testAnydataOrErrorRestFieldRHSIndexAccess");
        Assert.assertNull(returns);
    }

    @Test(description = "Test string constrained rest field")
    public void testStringRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testStringRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("lname")) instanceof BString);
        Assert.assertEquals(person.get(StringUtils.fromString("lname")).toString(), "Bar");

        Assert.assertTrue(person.get(StringUtils.fromString("address")) instanceof BString);
        Assert.assertEquals(person.get(StringUtils.fromString("address")).toString(), "Colombo");

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"lname\":\"Bar\"," +
                "\"address\":\"Colombo\"}");
    }

    @Test(description = "Test non-existent String rest field RHS index-based access")
    public void testStringRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStringRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "");
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test int constrained rest field")
    public void testIntRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testIntRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("year")) instanceof Long);
        Assert.assertEquals((person.get(StringUtils.fromString("year"))), 3L);

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"year\":3}");
    }

    @Test(description = "Test non-existent int rest field RHS index-based access")
    public void testIntRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testIntRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0), 0L);
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test float constrained rest field")
    public void testFloatRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("height")) instanceof Double);
        Assert.assertEquals((person.get(StringUtils.fromString("height"))), 5.9D);

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"height\":5.9}");
    }

    @Test(description = "Test non-existent float rest field RHS index-based access")
    public void testFloatRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testFloatRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0), 61.5);
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test boolean constrained rest field")
    public void testBooleanRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("isEmployed")) instanceof Boolean);
        Assert.assertTrue((Boolean) person.get(StringUtils.fromString("isEmployed")));

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"isEmployed\":true}");
    }

    @Test(description = "Test non-existent boolean rest field RHS access")
    public void testBooleanRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testBooleanRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0), true);
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test map constrained rest field")
    public void testMapRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testMapRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("misc")) instanceof BMap);
        Assert.assertEquals(person.get(StringUtils.fromString("misc")).toString(),
                "{\"lname\":\"Bar\",\"height\":5.9,\"isEmployed\":true}");

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"misc\":{\"lname\":\"Bar\",\"height\":5" +
                ".9,\"isEmployed\":true}}");
    }

    @Test(description = "Test non-existent map rest field RHS index-based access")
    public void testMapRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testMapRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(((BMap) returns.get(0)).size(), 0);
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test union constrained rest field")
    public void testUnionRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testUnionRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("lname")) instanceof BString);
        Assert.assertTrue(person.get(StringUtils.fromString("height")) instanceof Double);
        Assert.assertTrue(person.get(StringUtils.fromString("isEmployed")) instanceof Boolean);

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"lname\":\"Bar\",\"height\":5.9," +
                "\"isEmployed\":true}");
    }

    @Test(description = "Test non-existent union rest field RHS access")
    public void testUnionRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testUnionRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "Foo");
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test nil constrained rest field")
    public void testNilRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testNilRestField");

        BMap person = (BMap) returns;
        Assert.assertNull(person.get(StringUtils.fromString("lname")));

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"lname\":null}");
    }

    @Test(description = "Test record constrained rest field")
    public void testRecordRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testRecordRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("dpt")) instanceof BMap);
        Assert.assertEquals(person.get(StringUtils.fromString("dpt")).toString(), "{\"dptName\":\"Engineering\"," +
                "\"employees\":[]}");

        Assert.assertEquals(person.toString(),
                "{\"name\":\"Foo\",\"age\":25,\"dpt\":{\"dptName\":\"Engineering\",\"employees\":[]}}");
    }

    @Test(description = "Test non-existent record rest field RHS access")
    public void testRecordRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testRecordRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test object constrained rest field")
    public void testObjectRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testObjectRestField");

        BMap person = (BMap) returns;
        Assert.assertTrue(person.get(StringUtils.fromString("pet")) instanceof BObject);
        Assert.assertEquals(person.get(StringUtils.fromString("pet")).toString(), "{kind:Cat, name:Miaw}");

        Assert.assertEquals(person.toString(), "{\"name\":\"Foo\",\"age\":25,\"pet\":object Animal}");
    }

    @Test(description = "Test non-existent object rest field RHS index-based access")
    public void testObjectRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(getType(returns.get(0)).getName(), "Animal");
        Assert.assertEquals(((BObject) returns.get(0)).get(StringUtils.fromString("kind")).toString(), "Dog");
        Assert.assertEquals(((BObject) returns.get(0)).get(StringUtils.fromString("name")).toString(), "Rocky");
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test tuple constrained rest field")
    public void testTupleRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleRestField");

        BMap person = (BMap) returns;
        BArray miscInfo = (BArray) person.get(StringUtils.fromString("misc"));
        Assert.assertTrue(person.get(StringUtils.fromString("misc")) instanceof BArray);

        Assert.assertTrue(miscInfo.getRefValue(0) instanceof Double);
        Assert.assertTrue(miscInfo.getRefValue(1) instanceof BString);
        Assert.assertTrue(miscInfo.getRefValue(2) instanceof BObject);

        Assert.assertEquals(person.toString(),
                "{\"name\":\"Foo\",\"age\":25,\"misc\":[5.9,\"Bar\",object Animal]}");
    }

    @Test(description = "Test non-existent tuple rest field RHS index-based access")
    public void testTupleRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testTupleRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns.get(0));

        BArray tup = (BArray) returns.get(0);
        Assert.assertEquals(tup.getRefValue(0), 4.5);
        Assert.assertEquals(tup.getRefValue(1).toString(), "foo");
        Assert.assertEquals(getType(tup.getRefValue(2)).getName(), "Animal");

        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test any rest field")
    public void testAnyRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testAnyRestField");

        BMap person = (BMap) returns;
        BArray pets = (BArray) person.get(StringUtils.fromString("pets"));
        Assert.assertEquals(getType(pets).toString(), "Animal?[]");
        Assert.assertEquals(person.toString(),
                "{\"name\":\"Foo\",\"age\":25,\"pets\":[object Animal,object Animal]}");
    }

    @Test(description = "Test non-existent any rest field RHS index-based access")
    public void testAnyRestFieldRHSIndexAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAnyRestFieldRHSIndexAccess");
        BArray tup = (BArray) returns.get(0);

        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(getType(tup).toString(), "Animal?[]");
        Assert.assertEquals(tup.toString(), "[object Animal,object Animal]");

        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test function pointer as a record field")
    public void testFuncPtrAsAField() {
        Object returns = BRunUtil.invoke(compileResult, "testFuncPtrAsRecordField");
        Assert.assertEquals(returns.toString(), "Doe, John");
    }

    @Test(description = "Test case for default value initializing in type referenced fields")
    public void testDefaultValueInit() {
        Object returns = BRunUtil.invoke(compileResult, "testDefaultValueInit");
        BMap manager = (BMap) returns;
        Assert.assertEquals(manager.get(StringUtils.fromString("name")).toString(), "John Doe");
        Assert.assertEquals((manager.get(StringUtils.fromString("age"))), 25L);
        Assert.assertEquals(manager.get(StringUtils.fromString("adr")).toString(), "{\"city\":\"Colombo\"," +
                "\"country\":\"Sri Lanka\"}");
        Assert.assertEquals(manager.get(StringUtils.fromString("company")).toString(), "WSO2");
        Assert.assertEquals(manager.get(StringUtils.fromString("dept")).toString(), "");
    }

    @Test
    public void testRecordInitWithFuncCalls() {
        Object returns = BRunUtil.invoke(compileResult, "testRecordInitWithFuncCalls");
        Assert.assertEquals(returns.toString(), "{\"b\":56,\"a\":777}");
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
    public void testLangFuncOnRecord() {
        Object returns = BRunUtil.invoke(compileResult, "testLangFuncOnRecord");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("toJson")), 44L);
    }

    @Test
    public void testTypeInclusionWithOpenRecord() {
        BRunUtil.invoke(compileResult, "testTypeInclusionWithOpenRecord");
    }

    @Test
    public void testExprsAsRecordLiteralKeysSemanticsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_invalid_key_expr_semantics_negative" +
                ".bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string', found 'float'", 34, 27);
        BAssertUtil.validateError(result, 1, "missing non-defaultable required record field 's'", 35, 14);
        BAssertUtil.validateError(result, 2, "incompatible types: expected 'string', found 'int'", 36, 27);
        BAssertUtil.validateError(result, 3, "incompatible types: expected 'string', found 'boolean'", 37, 37);
        BAssertUtil.validateError(result, 4, "missing non-defaultable required record field 's'", 38, 14);
        BAssertUtil.validateError(result, 5, "incompatible types: expected '(string|int|anydata)', found 'error'", 41,
                44);
    }

    @Test
    public void testInvalidExprsAsRecordLiteralKeys() {
        CompileResult result = BCompileUtil.compile("test-src/record/open_record_invalid_key_expr_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "invalid key 's2': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 27, 25);
        BAssertUtil.validateError(result, 1, "invalid key 'i2': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 27, 38);
        BAssertUtil.validateError(result, 2, "invalid key 's2': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 28, 25);
    }

    @Test
    public void testCustomErrorTypeDescFieldOnRecord() {
        BRunUtil.invoke(compileResult, "testCustomErrorTypeDescFieldOnRecord");
    }

    @Test
    public void testOptionalRecordRemove() {
        BRunUtil.invoke(compileResult, "removeOptional");
    }

    @Test
    public void testRestRecordRemove() {
        BRunUtil.invoke(compileResult, "removeRest");
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
    public void testScopingRules() {
        BRunUtil.invoke(compileResult, "testScopingRules");
    }

    @Test
    public void testIntersectionOfReadonlyAndRecordTypeWithDefaultValues() {
        BRunUtil.invoke(compileResult, "testIntersectionOfReadonlyAndRecordTypeWithDefaultValues");
    }

    @Test
    public void testRecordsWithFieldsWithBuiltinNames() {
        BRunUtil.invoke(compileResult, "testRecordsWithFieldsWithBuiltinNames");
    }

    @Test
    public void testDefaultableRecordFieldsWithQuotedIdentifiersForTypeKeywords() {
        BRunUtil.invoke(compileResult, "testDefaultableRecordFieldsWithQuotedIdentifiersForTypeKeywords");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
