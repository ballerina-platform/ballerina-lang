/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.balo.object;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined object types in ballerina.
 */
public class ObjectInBaloTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "utils");
        result = BCompileUtil.compile("test-src/balo/test_balo/object/test_objects.bal");
    }

    @Test(description = "Test Basic object as struct")
    public void testBasicStructAsObject() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleObjectAsStruct");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test Object field defaultable")
    public void testObjectFieldDefaultable() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectFieldDefaultable");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test Basic object as struct with just new")
    public void testBasicStructAsObjectWithJustNew() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleObjectAsStructWithNew");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test object with init function")
    public void testObjectWithSimpleInit() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithSimpleInit");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 17);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 99);
        Assert.assertEquals(returns[3].stringValue(), "default value");
    }

    @Test(description = "Test object with defaultable field in init function")
    public void testObjectWithDefaultableField() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithDefaultableValues");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 109);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "default value");
    }

    @Test(description = "Test object with init with different values")
    public void testObjectWithSimpleInitWithDiffValues() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithSimpleInitWithDiffValues");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 37);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 675);
        Assert.assertEquals(returns[3].stringValue(), "adding value in invocation");
    }

    @Test(description = "Test object without RHS type")
    public void testObjectWithoutRHSType() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithoutRHSType");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 37);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 675);
        Assert.assertEquals(returns[3].stringValue(), "adding value in invocation");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithAttachedFunc1");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 361);
        Assert.assertEquals(returns[1].stringValue(), "added values february");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 99);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test object with self keyword")
    public void testObjectWithSelfKeyword() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithSelfKeyword");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(returns[2].stringValue(), "sample name");
        Assert.assertEquals(returns[3].stringValue(), "sample name");
    }

    @Test(description = "Test object with byte type fields")
    public void testObjectWithByteTypeFields() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithByteTypeFields");

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        Assert.assertSame(returns[1].getClass(), BValueArray.class);
        Assert.assertSame(returns[2].getClass(), BValueArray.class);

        byte[] bytes1 = new byte[]{3, 4, 5, 8};
        byte[] bytes2 = ByteArrayUtils.decodeBase64("aGVsbG8gYmFsbGVyaW5hICEhIQ==");
        byte[] bytes3 = ByteArrayUtils.hexStringToByteArray("aaabcfccadafcd341a4bdfabcd8912df");
        BValueArray blobArray1 = (BValueArray) returns[0];
        BValueArray blobArray2 = (BValueArray) returns[1];
        BValueArray blobArray3 = (BValueArray) returns[2];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blobArray1);
        ByteArrayUtils.assertJBytesWithBBytes(bytes2, blobArray2);
        ByteArrayUtils.assertJBytesWithBBytes(bytes3, blobArray3);
    }

    @Test(description = "Test object with calling attached functions")
    public void testObjectCallAttachedFunctions() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectCallAttachedFunctions");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(returns[2].stringValue(), "sample name");
        Assert.assertEquals(returns[3].stringValue(), "sample name");
    }

    @Test(description = "Test object inside object with different values")
    public void testObjectInsideObject() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectInsideObject");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "changed value");
    }

    @Test(description = "Test object self as a value")
    public void testObjectPassSelfAsValue() {
        BValue[] returns = BRunUtil.invoke(result, "testGetValueFromPassedSelf");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction1() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithInterface");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 80);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 100);
        Assert.assertEquals(returns[3].stringValue(), "adding value in invocation uuuu");
    }

    @Test(description = "Test object with default initializer")
    public void testObjectWithWithDefaultInitialize() {
        BValue[] returns = BRunUtil.invoke(result, "testGetDefaultValuesInObject");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(returns[1].stringValue(), "sample value");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
        Assert.assertEquals(returns[3].stringValue(), "");
    }

//    @Test(description = "Test passing value to a defaultable object field")
//    public void testPassingValueForDefaultableObjectField() {
//        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_values_for_defaultable_field.bal");
//        BValue[] returns = BRunUtil.invoke(compileResult, "passValueForDefaultableObjectField");
//
//        Assert.assertEquals(returns.length, 2);
//        Assert.assertSame(returns[0].getClass(), BInteger.class);
//        Assert.assertSame(returns[1].getClass(), BString.class);
//
//        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
//        Assert.assertEquals(returns[1].stringValue(), "passed in name value");
//    }

    @Test(description = "Test shadowing object field")
    public void testShadowingObjectField() {
        BValue[] returns = BRunUtil.invoke(result, "testShadowingObjectField");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(returns[1].stringValue(), "passed in name value");
    }

    @Test(description = "Test initializing object in return statement with same type")
    public void testNewAsReturnWithSameType() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateObjectInReturnSameType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test initializing object in return statement with different type")
    public void testNewAsReturnWithDifferentType() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateObjectInReturnDifferentType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

    @Test(description = "Test object with default initialize global variable")
    public void testObjectWithDefaultInitializeGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "testGetDefaultValuesInObjectGlobalVar");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(returns[1].stringValue(), "sample value");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
        Assert.assertEquals(returns[3].stringValue(), "");
    }

    @Test(description = "Test object self reference with defaultable")
    public void testObjectSelfreferenceWithDefaultable() {
        BValue[] returns = BRunUtil.invoke(result, "testCyclicReferenceWithDefaultable");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 89);
    }

    @Test(description = "Test object recursive reference with nillable")
    public void testRecursiveObjectRefWithNillable() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveObjectWithNill");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 90);
    }

    @Test(description = "Test object field with expr as defaultable")
    public void testFieldWithExpr() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldWithExpr");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 88);
        Assert.assertEquals(returns[1].stringValue(), "sanjiva");
    }

    @Test (description = "Negative test to test multiple attach functions for same function interface and " +
            "attached function without function interface")
    public void testObjectNegativeTestForAttachFunctions() {
        CompileResult result = BCompileUtil.compile("test-src/balo/test_balo/object" +
                "/object_with_interface_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot initialize abstract object 'testorg/foo:1.0.0:Country'", 4, 21);
    }

    @Test (description = "Negative test to test undefined functions in object variables")
    public void testObjectNegativeSemanticTestForNonInitializable() {
        CompileResult result = BCompileUtil.compile("test-src/balo/test_balo/object" +
                "/object_with_non_defaultable_semantic_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined method 'attachInterface' in object " +
                "'testorg/foo:1.0.0:Architect'", 7, 15);
    }

    @Test (description = "Negative test to test uninitialized object variables")
    public void testObjectNegativeTestForNonInitializable() {
        CompileResult result = BCompileUtil.compile("test-src/balo/test_balo/object" +
                "/object_with_non_defaultable_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "variable 'p' is not initialized", 7, 13);
        BAssertUtil.validateError(result, 1, "variable 'p' is not initialized", 7, 19);
    }

    @Test(description = "Negative test to test returning different type without type name")
    public void testObjectNegativeTestForReturnDifferentType() {
        CompileResult result = BCompileUtil.compile("test-src/balo/test_balo/object" +
                "/object_new_in_return_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "missing required parameter 'addVal' in call to 'new'()", 8, 12);
        BAssertUtil.validateError(result, i++, "missing required parameter 'addVal' in call to 'new'()", 12, 33);
        BAssertUtil.validateError(result, i++, "cannot infer type of the object from 'other'", 13, 19);
        BAssertUtil.validateError(result, i++, "invalid variable definition; can not infer the assignment type.",
                13, 19);
        BAssertUtil.validateError(result, i++, "cannot infer type of the object from 'error'", 14, 21);
        Assert.assertEquals(result.getErrorCount(), i);
    }

//    @Test (description = "Negative test to test returning different type without type name")
//    public void testUnInitializableObjFieldAsParam() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_un_initializable_field.bal");
//        Assert.assertEquals(result.getErrorCount(), 1);
//        BAssertUtil.validateError(result, 0, "object un-initializable field 'Foo foo' is not " +
//                "present as a constructor parameter", 18, 1);
//    }
//
//    @Test (description = "Negative test to test self reference types")
//    public void testSelfReferenceType() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_cyclic_self_reference.bal");
//        Assert.assertEquals(result.getErrorCount(), 5);
//        BAssertUtil.validateError(result, 0, "object un-initializable field " +
//                "'Employee emp' is not present as a constructor parameter", 7, 1);
//        BAssertUtil.validateError(result, 1, "object un-initializable field " +
//                "'Foo foo' is not present as a constructor parameter", 14, 1);
//        BAssertUtil.validateError(result, 2, "object un-initializable field " +
//                "'Bar bar' is not present as a constructor parameter", 14, 1);
//        BAssertUtil.validateError(result, 3, "object un-initializable field " +
//                "'Bar bar1' is not present as a constructor parameter", 22, 1);
//        BAssertUtil.validateError(result, 4, "cyclic type reference in " +
//                "'[Person, Employee, Foo, Bar]'", 32, 5);
//    }
//
//    @Test (description = "Negative test to test self reference types")
//    public void testNonMatchingAttachedFunction() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_invalid_attached_func_def.bal");
//        Assert.assertEquals(result.getErrorCount(), 21);
//        BAssertUtil.validateError(result, 0, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test0'", 3, 16);
//        BAssertUtil.validateError(result, 1, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test1'", 3, 16);
//        BAssertUtil.validateError(result, 2, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test2'", 3, 16);
//        BAssertUtil.validateError(result, 3, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test3'", 3, 16);
//        BAssertUtil.validateError(result, 4, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test5'", 3, 16);
//        BAssertUtil.validateError(result, 5, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test6'", 3, 16);
//        BAssertUtil.validateError(result, 6, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test7'", 3, 16);
//        BAssertUtil.validateError(result, 7, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test9'", 3, 16);
//        BAssertUtil.validateError(result, 8, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test12'", 3, 16);
//        BAssertUtil.validateError(result, 9, "cannot initialize object 'Person', no " +
//                "implementation for the interface 'Person.test13'", 3, 16);
//        BAssertUtil.validateError(result, 10, "cannot find matching interface " +
//                "function 'test0' in the object 'Person'", 42, 1);
//        BAssertUtil.validateError(result, 11, "cannot find matching interface " +
//                "function 'test1' in the object 'Person'", 46, 1);
//        BAssertUtil.validateError(result, 12, "cannot find matching interface " +
//                "function 'test2' in the object 'Person'", 50, 1);
//        BAssertUtil.validateError(result, 13, "cannot find matching interface " +
//                "function 'test3' in the object 'Person'", 54, 1);
//        BAssertUtil.validateError(result, 14, "incompatible types: expected " +
//                "'string', found 'int'", 54, 44);
//        BAssertUtil.validateError(result, 15, "cannot find matching interface " +
//                "function 'test5' in the object 'Person'", 62, 1);
//        BAssertUtil.validateError(result, 16, "cannot find matching interface " +
//                "function 'test6' in the object 'Person'", 66, 1);
//        BAssertUtil.validateError(result, 17, "cannot find matching interface " +
//                "function 'test7' in the object 'Person'", 70, 1);
//        BAssertUtil.validateError(result, 18, "cannot find matching interface " +
//                "function 'test9' in the object 'Person'", 78, 1);
//        BAssertUtil.validateError(result, 19, "cannot find matching interface " +
//                "function 'test12' in the object 'Person'", 90, 1);
//        BAssertUtil.validateError(result, 20, "cannot find matching interface " +
//                "function 'test13' in the object 'Person'", 94, 1);
//    }
//
//    @Test (description = "Negative test to test initializing objects with only interface functions")
//    public void testInitializingInterfaceObject() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_initialize_interface_object.bal");
//        Assert.assertEquals(result.getErrorCount(), 1);
//        BAssertUtil.validateError(result, 0, "cannot initialize object 'Person', " +
//                "no implementation for the interface 'Person.test'", 3, 16);
//    }
//
//    @Test (description = "Negative test to test initializing object with struct literal")
//    public void testInitializingObjectWithStructLiteral() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_init_with_struct_literal.bal");
//        Assert.assertEquals(result.getErrorCount(), 2);
//        BAssertUtil.validateError(result, 0, "invalid usage of record literal with type 'Person'", 1, 13);
//        BAssertUtil.validateError(result, 1, "invalid usage of record literal with type 'Person'", 4, 16);
//    }
//
//    @Test (description = "Negative test to test referring undefined field in constructor")
//    public void testReferUndefinedFieldBal() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_access_undefined_field.bal");
//        Assert.assertEquals(result.getErrorCount(), 3);
//        BAssertUtil.validateError(result, 0, "undefined field 'agea' in object 'Person'", 6, 10);
//        BAssertUtil.validateError(result, 1, "undefined symbol '><'", 6, 10);
//        BAssertUtil.validateError(result, 2, "undefined symbol 'abc'", 7, 9);
//    }
//
//    @Test (description = "Negative test to test nillable initialization")
//    public void testNillableInitialization() {
//        CompileResult result = BCompileUtil.compile("test-src/object/object_nillable_init.bal");
//        Assert.assertEquals(result.getErrorCount(), 10);
//        BAssertUtil.validateError(result, 0, "cannot infer type of the object from 'Person?'", 1, 14);
//        BAssertUtil.validateError(result, 1, "cannot infer type of the object from 'Person?'", 2, 14);
//        BAssertUtil.validateError(result, 2, "cannot infer type of the object from 'Person?'", 5, 18);
//        BAssertUtil.validateError(result, 3, "cannot infer type of the object from 'Person?'", 6, 18);
//        BAssertUtil.validateError(result, 4, "cannot infer type of the object from 'Person?'", 8, 10);
//        BAssertUtil.validateError(result, 5, "cannot infer type of the object from 'Person?'", 10, 10);
//        BAssertUtil.validateError(result, 6, "cannot infer type of the object from 'Person?'", 22, 22);
//        BAssertUtil.validateError(result, 7, "cannot infer type of the object from 'Person?'", 23, 22);
//        BAssertUtil.validateError(result, 8, "cannot infer type of the object from 'Person?'", 28, 14);
//        BAssertUtil.validateError(result, 9, "cannot infer type of the object from 'Person?'", 29, 14);
//    }

    @Test
    public void testObjectReferingTypeFromBalo_1() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectReferingTypeFromBalo_1");
        Assert.assertEquals(returns.length, 2);

        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BFloat.class);

        Assert.assertEquals(returns[0].stringValue(), "Hello John");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 800.0);
    }

    @Test
    public void testObjectReferingTypeFromBalo_2() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectReferingTypeFromBalo_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Hello Jane");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1800.0);
    }

    @Test
    public void testObjectReferingTypeFromBalo_3() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectReferingTypeFromBalo_3");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Good morning Jane");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1800.0);
    }

    @Test
    public void testObjectReferingNonAbstractObjFromBalo() {
        BRunUtil.invoke(result, "testObjectReferingNonAbstractObjFromBalo");
    }

    @Test
    public void testObjectReferingNonAbstractObjLoadedFromBalo() {
        BRunUtil.invoke(result, "testObjectReferingNonAbstractObjLoadedFromBalo");
    }

    @Test
    public void testObjectReferingTypeFromBaloNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/balo/test_balo/object/test_objects_type_reference_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 5);
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined field 'name' in object 'Manager1'", 25, 13);
        BAssertUtil.validateError(result, i++, "undefined field 'age' in object 'Manager1'", 26, 13);
        BAssertUtil.validateError(result, i++,
                                  "no implementation found for the function 'getBonus' of non-abstract object " +
                                          "'Manager2'",
                                  36, 5);
        BAssertUtil.validateError(result, i++,
                                  "no implementation found for the function 'getName' of non-abstract object " +
                                          "'Manager2'",
                                  36, 5);
        BAssertUtil.validateError(result, i,
                                  "incompatible type reference 'foo:NormalPerson': a referenced object cannot have " +
                                          "non-public fields or methods",
                                  42, 6);
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "utils");
    }
}
