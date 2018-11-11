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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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
                "adrs:{}, age:50, family:{spouse:\"\", noOfChildren:0, children:[]}, parent:null}}");
    }

    @Test
    public void testStructLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_literals.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns[0].stringValue(), "{dptName:\"\", employees:[], manager:" +
                "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:null}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns[0].stringValue(),
                            "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:null}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile("test-src/record/nested_record_inline_init.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns[0].stringValue(),
                            "{name:\"default first name\", fname:\"\", lname:\"Doe\", adrs:{}, age:999, " +
                                    "family:{spouse:\"Jane\", noOfChildren:0, children:[\"Alex\", \"Bob\"]}}");
    }

    @Test(description = "Negative test to test attaching functions to record literal")
    public void testStructLiteralAttachedFunc() {
        CompileResult result = BCompileUtil.compile("test-src/record/record_literal_with_attached_functions.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "cannot attach function 'getName' to record type 'Person'", 7, 1);
        BAssertUtil.validateError(result, 1, "undefined symbol 'self'", 8, 12);
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
                "noOfChildren:0, children:[]}, parent:null, mname:\"Bar\", height:5.9, firstName:\"John\"}");
    }

    @Test(description = "Test non-existent anydata rest field RHS access",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*cannot find key 'firstName'.*")
    public void testAnydataRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testAnydataRestFieldRHSAccess");
    }

    @Test(description = "Test non-existent anydata rest field RHS index-based access")
    public void testAnydataRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnydataRestFieldRHSIndexAccess");
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

    @Test(description = "Test non-existent String rest field RHS access", expectedExceptions =
            BLangRuntimeException.class)
    public void testStringRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testStringRestFieldRHSAccess");
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

    @Test(description = "Test non-existent int rest field RHS access", expectedExceptions =
            BLangRuntimeException.class)
    public void testIntRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testIntRestFieldRHSAccess");
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

    @Test(description = "Test non-existent float rest field RHS access", expectedExceptions =
            BLangRuntimeException.class)
    public void testFloatRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testFloatRestFieldRHSAccess");
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

    @Test(description = "Test non-existent boolean rest field RHS access", expectedExceptions =
            BLangRuntimeException.class)
    public void testBooleanRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testBooleanRestFieldRHSAccess");
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

    @Test(description = "Test non-existent map rest field RHS access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'misc'.*")
    public void testMapRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testMapRestFieldRHSAccess");
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

    @Test(description = "Test non-existent union rest field RHS access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'misc'.*")
    public void testUnionRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testUnionRestFieldRHSAccess");
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

        Assert.assertEquals(person.stringValue(), "{name:\"Foo\", age:25, lname:null}");
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

    @Test(description = "Test non-existent record rest field RHS access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'department'.*")
    public void testRecordRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testRecordRestFieldRHSAccess");
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

    @Test(description = "Test non-existent object rest field RHS access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'pet'.*")
    public void testObjectRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testObjectRestFieldRHSAccess");
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
        BRefValueArray miscInfo = (BRefValueArray) person.get("misc");
        Assert.assertTrue(person.get("misc") instanceof BRefValueArray);

        Assert.assertTrue(miscInfo.get(0) instanceof BFloat);
        Assert.assertTrue(miscInfo.get(1) instanceof BString);
        Assert.assertTrue(miscInfo.get(2) instanceof BMap);

        Assert.assertEquals(person.stringValue(),
                "{name:\"Foo\", age:25, misc:(5.9, \"Bar\", {kind:\"Cat\", name:\"Miaw\"})}");
    }

    @Test(description = "Test non-existent tuple rest field RHS access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'tupType'.*")
    public void testTupleRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testTupleRestFieldRHSAccess");
    }

    @Test(description = "Test non-existent tuple rest field RHS index-based access")
    public void testTupleRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleRestFieldRHSIndexAccess");
        Assert.assertNotNull(returns[0]);

        BRefValueArray tup = (BRefValueArray) returns[0];
        Assert.assertEquals(((BFloat) tup.get(0)).floatValue(), 4.5);
        Assert.assertEquals(tup.get(1).stringValue(), "foo");
        Assert.assertEquals(((BMap) tup.get(2)).getType().getName(), "Animal");

        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test any rest field")
    public void testAnyRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyRestField");

        BMap person = (BMap) returns[0];
        BRefValueArray pets = (BRefValueArray) person.get("pets");
        Assert.assertEquals(pets.getType().toString(), "Animal[]");
        Assert.assertEquals(person.stringValue(),
                "{name:\"Foo\", age:25, pets:[{kind:\"Cat\", name:\"Miaw\"}, {kind:\"Dog\", name:\"Woof\"}]}");
    }

    @Test(description = "Test non-existent any rest field RHS access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'anyField'.*")
    public void testAnyRestFieldRHSAccess() {
        BRunUtil.invoke(compileResult, "testAnyRestFieldRHSAccess");
    }

    @Test(description = "Test non-existent any rest field RHS index-based access")
    public void testAnyRestFieldRHSIndexAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyRestFieldRHSIndexAccess");
        BRefValueArray tup = (BRefValueArray) returns[0];

        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(tup.getType().toString(), "Animal[]");
        Assert.assertEquals(tup.stringValue(), "[{kind:\"Cat\", name:\"Miaw\"}, {kind:\"Dog\", name:\"Woof\"}]");

        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test function pointer as a record field")
    public void testFuncPtrAsAField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFuncPtrAsRecordField");
        Assert.assertEquals(returns[0].stringValue(), "Doe, John");
    }
}
