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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_literals.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns[0].stringValue(), "{dptName:\"\", employees:[], manager:" +
                "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:()}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns[0].stringValue(),
                            "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:()}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile("test-src/record/nested_record_inline_init.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns[0].stringValue(),
                            "{name:\"default first name\", fname:\"\", lname:\"Doe\", adrs:{}, age:999, " +
                                    "family:{spouse:\"Jane\", noOfChildren:0, children:[\"Alex\", \"Bob\"]}}");
    }

    @Test(description = "Negative test to test attaching functions to record literal",
            groups = { "disableOnOldParser" })
    public void testStructLiteralAttachedFunc() {
        CompileResult result =
                BCompileUtil.compile("test-src/record/record_literal_with_attached_functions_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "redeclared symbol 'Person'", 7, 10);
        BAssertUtil.validateError(result, 1, "invalid token '.'", 7, 24);
        BAssertUtil.validateError(result, 2, "invalid token 'getName'", 7, 24);
        BAssertUtil.validateError(result, 3, "undefined symbol 'self'", 8, 12);
    }

    @Test(description = "Test addition of different types for default rest field type")
    public void testAdditionOfARestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAdditionOfARestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("mname") instanceof BString);
        Assert.assertEquals(person.get("mname").stringValue(), "Bar");

        Assert.assertTrue(person.get("height") instanceof BFloat);
        Assert.assertEquals(((BFloat) person.get("height")).floatValue(), 5.9D);

        Assert.assertTrue(person.get("firstName") instanceof BString);
        Assert.assertEquals(person.get("firstName").stringValue(), "John");

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", lname:\"\", adrs:{}, age:25, family:{spouse:\"\", " +
                "noOfChildren:0, children:[]}, parent:(), mname:\"Bar\", height:5.9, firstName:\"John\"}");
    }

    @Test(description = "Test non-existent anydata or error rest field RHS index-based access")
    public void testAnydataOrErrorRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnydataOrErrorRestFieldRHSIndexAccess");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test string constrained rest field")
    public void testStringRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("lname") instanceof BString);
        Assert.assertEquals(person.get("lname").stringValue(), "Bar");

        Assert.assertTrue(person.get("address") instanceof BString);
        Assert.assertEquals(person.get("address").stringValue(), "Colombo");

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, lname:\"Bar\", address:\"Colombo\"}");
    }

    @Test(description = "Test non-existent String rest field RHS index-based access")
    public void testStringRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "");
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test int constrained rest field")
    public void testIntRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("year") instanceof BInteger);
        Assert.assertEquals(((BInteger) person.get("year")).intValue(), 3L);

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, year:3}");
    }

    @Test(description = "Test non-existent int rest field RHS index-based access")
    public void testIntRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0L);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test float constrained rest field")
    public void testFloatRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("height") instanceof BFloat);
        Assert.assertEquals(((BFloat) person.get("height")).floatValue(), 5.9D);

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, height:5.9}");
    }

    @Test(description = "Test non-existent float rest field RHS index-based access")
    public void testFloatRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 61.5);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test boolean constrained rest field")
    public void testBooleanRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("isEmployed") instanceof BBoolean);
        Assert.assertTrue(((BBoolean) person.get("isEmployed")).booleanValue());

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, isEmployed:true}");
    }

    @Test(description = "Test non-existent boolean rest field RHS access")
    public void testBooleanRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test map constrained rest field")
    public void testMapRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("misc") instanceof BMap);
        Assert.assertEquals(person.get("misc").stringValue(),
                            "{\"lname\":\"Bar\", \"height\":5.9, \"isEmployed\":true}");

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, misc:{\"lname\":\"Bar\", \"height\":5.9, " +
                "\"isEmployed\":true}}");
    }

    @Test(description = "Test non-existent map rest field RHS index-based access")
    public void testMapRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BMap) returns[0]).size(), 0);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test union constrained rest field")
    public void testUnionRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnionRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("lname") instanceof BString);
        Assert.assertTrue(person.get("height") instanceof BFloat);
        Assert.assertTrue(person.get("isEmployed") instanceof BBoolean);

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, lname:\"Bar\", height:5.9, " +
                "isEmployed:true}");
    }

    @Test(description = "Test non-existent union rest field RHS access")
    public void testUnionRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnionRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Foo");
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test nil constrained rest field")
    public void testNilRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNilRestField");

        BMap person = (BMap) returns[0];
        Assert.assertNull(person.get("lname"));

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, lname:()}");
    }

    @Test(description = "Test record constrained rest field")
    public void testRecordRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRecordRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("dpt") instanceof BMap);
        Assert.assertEquals(person.get("dpt").stringValue(), "{dptName:\"Engineering\", employees:[]}");

        Assert.assertEquals(person.stringValue(),
                            "{name:\"Foo\", age:25, dpt:{dptName:\"Engineering\", employees:[]}}");
    }

    @Test(description = "Test non-existent record rest field RHS access")
    public void testRecordRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRecordRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test object constrained rest field")
    public void testObjectRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectRestField");

        BMap person = (BMap) returns[0];
        Assert.assertTrue(person.get("pet") instanceof BMap);
        Assert.assertEquals(person.get("pet").stringValue(), "{kind:\"Cat\", name:\"Miaw\"}");

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, pet:{kind:\"Cat\", name:\"Miaw\"}}");
    }

    @Test(description = "Test non-existent object rest field RHS index-based access")
    public void testObjectRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].getType().getName(), "Animal");
        Assert.assertEquals(((BMap) returns[0]).get("kind").stringValue(), "Dog");
        Assert.assertEquals(((BMap) returns[0]).get("name").stringValue(), "Rocky");
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test tuple constrained rest field")
    public void testTupleRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleRestField");

        BMap person = (BMap) returns[0];
        BValueArray miscInfo = (BValueArray) person.get("misc");
        Assert.assertTrue(person.get("misc") instanceof BValueArray);

        Assert.assertTrue(miscInfo.getRefValue(0) instanceof BFloat);
        Assert.assertTrue(miscInfo.getRefValue(1) instanceof BString);
        Assert.assertTrue(miscInfo.getRefValue(2) instanceof BMap);

        Assert.assertEquals(person.stringValue(),
                "{name:\"Foo\", age:25, misc:[5.9, \"Bar\", {kind:\"Cat\", name:\"Miaw\"}]}");
    }

    @Test(description = "Test non-existent tuple rest field RHS index-based access")
    public void testTupleRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);

        BValueArray tup = (BValueArray) returns[0];
        Assert.assertEquals(((BFloat) tup.getRefValue(0)).floatValue(), 4.5);
        Assert.assertEquals(tup.getRefValue(1).stringValue(), "foo");
        Assert.assertEquals(tup.getRefValue(2).getType().getName(), "Animal");

        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test any rest field")
    public void testAnyRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyRestField");

        BMap person = (BMap) returns[0];
        BValueArray pets = (BValueArray) person.get("pets");
        Assert.assertEquals(pets.getType().toString(), "Animal|null[]");
        Assert.assertEquals(person.stringValue(),
                "{name:\"Foo\", age:25, pets:[{kind:\"Cat\", name:\"Miaw\"}, {kind:\"Dog\", name:\"Woof\"}]}");
    }

    @Test(description = "Test non-existent any rest field RHS index-based access")
    public void testAnyRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyRestFieldRHSIndexAccess");
        BValueArray tup = (BValueArray) returns[0];

        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(tup.getType().toString(), "Animal|null[]");
        Assert.assertEquals(tup.stringValue(), "[{kind:\"Cat\", name:\"Miaw\"}, {kind:\"Dog\", name:\"Woof\"}]");

        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test function pointer as a record field")
    public void testFuncPtrAsAField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFuncPtrAsRecordField");
        Assert.assertEquals(returns[0].stringValue(), "Doe, John");
    }

    @Test(description = "Test case for default value initializing in type referenced fields")
    public void testDefaultValueInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultValueInit");
        BMap manager = (BMap) returns[0];
        Assert.assertEquals(manager.get("name").stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) manager.get("age")).intValue(), 25);
        Assert.assertEquals(manager.get("adr").stringValue(), "{city:\"Colombo\", country:\"Sri Lanka\"}");
        Assert.assertEquals(manager.get("company").stringValue(), "WSO2");
        Assert.assertEquals(manager.get("dept").stringValue(), "");
    }

    @Test
    public void testRecordInitWithFuncCalls() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRecordInitWithFuncCalls");
        Assert.assertEquals(returns[0].stringValue(), "{b:56, a:777}");
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
                "expected a string literal or an expression", 27, 26);
        BAssertUtil.validateError(result, 1, "invalid key 'i2': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 27, 39);
        BAssertUtil.validateError(result, 2, "invalid key 's2': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 28, 26);
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
}
