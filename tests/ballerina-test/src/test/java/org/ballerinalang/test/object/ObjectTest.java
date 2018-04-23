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
package org.ballerinalang.test.object;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for user defined object types in ballerina.
 */
public class ObjectTest {

    @Test(description = "Test Basic object as struct")
    public void testBasicStructAsObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-simple-struct.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleObjectAsStruct");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-field-defaultable.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectFieldDefaultable");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-simple-struct.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleObjectAsStructWithNew");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSimpleInit");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-defaultable-field.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSimpleInit");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSimpleInitWithDiffValues");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithoutRHSType");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-attach-funcs.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithAttachedFunc1");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSelfKeyword");

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

    @Test(description = "Test object with calling attached functions")
    public void testObjectCallAttachedFunctions() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-call-attached-functions.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectCallAttachedFunctions");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword-pass-values.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInsideObject");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "changed value");
    }

    @Test(description = "Test object self as a value")
    public void testObjectPassSelfAsValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword-pass-values.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetValueFromPassedSelf");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-interface.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithInterface");

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

    @Test(description = "Test object with attached function implementation")
    public void testObjectWithAttachedFunctionImpl() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-interface-and-impl.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithInterface");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 17);
        Assert.assertEquals(returns[1].stringValue(), "february");
    }

    @Test(description = "Test object with default initializer")
    public void testObjectWithWithDefaultInitialize() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObject");

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

    @Test(description = "Test passing value to a defaultable object field")
    public void testPassingValueForDefaultableObjectField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_values_for_defaultable_field.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "passValueForDefaultableObjectField");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(returns[1].stringValue(), "passed in name value");
    }

    @Test(description = "Test shadowing object field")
    public void testShadowingObjectField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_shadow_field.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testShadowingObjectField");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(returns[1].stringValue(), "passed in name value");
    }

    @Test(description = "Test initializing object in return statement with same type")
    public void testNewAsReturnWithSameType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_new_in_return.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateObjectInReturnSameType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test initializing object in return statement with different type")
    public void testNewAsReturnWithDifferentType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_new_in_return.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateObjectInReturnDifferentType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

//
//    @Test(description = "Test object with default initializer with default values") //TODO fix
//    public void testObjectWithWithDefaultInitializeWithDefaultValues() {
//        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_with_default_test.bal");
//        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectFields");
//
//        Assert.assertEquals(returns.length, 2);
//        Assert.assertSame(returns[0].getClass(), BInteger.class);
//        Assert.assertSame(returns[1].getClass(), BString.class);
//
//        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15);
//        Assert.assertEquals(returns[1].stringValue(), "hello world");
//    }

    @Test(description = "Test object with default initialize global variable")
    public void testObjectWithDefaultInitializeGlobalVar() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectGlobalVar");

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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_cyclic_" +
                "self_reference_with_default.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testCyclicReferenceWithDefaultable");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 89);
    }

//    @Test(description = "Test object with default initialize global variable") //TODO fix
//    public void testObjectWithDefaultInitializeGlobalVar1() {
//        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test1.bal");
//        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectGlobalVar");
//
//        Assert.assertEquals(returns.length, 2);
//        Assert.assertSame(returns[0].getClass(), BInteger.class);
//        Assert.assertSame(returns[1].getClass(), BString.class);
//
//        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
//        Assert.assertEquals(returns[1].stringValue(), "");
//    }

//    @Test(description = "Test object with default initialize global variable") //TODO fix
//    public void abc() {
//        CompileResult compileResult = BCompileUtil.compile("test-src/object/abc1.bal");
//        BValue[] returns = BRunUtil.invoke(compileResult, "test");
//
//        Assert.assertEquals(returns.length, 2);
//        Assert.assertSame(returns[0].getClass(), BInteger.class);
//        Assert.assertSame(returns[1].getClass(), BString.class);
//
//        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
//        Assert.assertEquals(returns[1].stringValue(), "sample value");
//    }

    @Test (description = "Negative test to test multiple attach functions for same function interface and " +
            "attached function without function interface")
    public void testObjectNegativeTestForAttachFunctions() {
        CompileResult result = BCompileUtil.compile("test-src/object/object-with-interface-and-impl-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 5);

        BAssertUtil.validateError(result, 0, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.attachInterface'", 3, 16);
        // test accessing object fields without "self" keyword in attached functions.
        BAssertUtil.validateError(result, 1, "undefined symbol 'age'", 20, 17);
        // test duplicate matching attach function implementations
        BAssertUtil.validateError(result, 2, "implementation already exist for the given " +
                "function 'attachInterface' in same package", 24, 1);

        // test object without matching function signature within the object
        BAssertUtil.validateError(result, 3, "cannot find function signature for" +
                " function 'attachInterfaceFunc' in object 'Employee'", 38, 1);

        // test accessing object fields without "self" keyword in attached functions.
        BAssertUtil.validateError(result, 4, "undefined symbol 'age'", 39, 17);
    }

    @Test (description = "Negative test to test uninitialized object variables")
    public void testObjectNegativeTestForNonInitializable() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_with_non_defaultable_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        BAssertUtil.validateError(result, 0, "variable 'pp' is not initialized", 2, 1);
        BAssertUtil.validateError(result, 1, "variable 'ee' is not initialized", 3, 1);
        BAssertUtil.validateError(result, 2, "variable 'p' is not initialized", 6, 5);
        BAssertUtil.validateError(result, 3, "variable 'e' is not initialized", 7, 5);
        BAssertUtil.validateError(result, 4, "undefined function 'attachInterface' in struct 'Person'", 8, 13);
        BAssertUtil.validateError(result, 5, "object un-initializable field 'Person p' is " +
                "not present as a constructor parameter", 25, 1);
    }

    @Test (description = "Negative test to test returning different type without type name")
    public void testObjectNegativeTestForReturnDifferentType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_new_in_return_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        BAssertUtil.validateError(result, 0, "too many arguments in call to 'new()'", 23, 12);
        BAssertUtil.validateError(result, 1, "cannot infer type of the object from 'Person?'", 27, 12);
        BAssertUtil.validateError(result, 2, "cannot infer type of the object from 'Person?'", 31, 26);
        BAssertUtil.validateError(result, 3, "invalid variable definition; can not infer the assignment type.",
                32, 19);
        BAssertUtil.validateError(result, 4, "cannot infer type of the object from 'other'", 32, 19);
        BAssertUtil.validateError(result, 5, "invalid usage of 'new' with type 'error'", 33, 21);
    }

    @Test (description = "Negative test to test returning different type without type name")
    public void testUnInitializableObjFieldAsParam() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_un_initializable_field.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "object un-initializable field 'Foo foo' is not " +
                "present as a constructor parameter", 18, 1);
    }

    @Test (description = "Negative test to test self reference types")
    public void testSelfReferenceType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_cyclic_self_reference.bal");
        Assert.assertEquals(result.getErrorCount(), 5);
        BAssertUtil.validateError(result, 0, "object un-initializable field " +
                "'Employee emp' is not present as a constructor parameter", 7, 1);
        BAssertUtil.validateError(result, 1, "object un-initializable field " +
                "'Foo foo' is not present as a constructor parameter", 14, 1);
        BAssertUtil.validateError(result, 2, "object un-initializable field " +
                "'Bar bar' is not present as a constructor parameter", 14, 1);
        BAssertUtil.validateError(result, 3, "object un-initializable field " +
                "'Bar bar1' is not present as a constructor parameter", 22, 1);
        BAssertUtil.validateError(result, 4, "cyclic type reference in " +
                "'[Person, Employee, Foo, Bar]'", 32, 5);
    }

    @Test (description = "Negative test to test self reference types")
    public void testNonMatchingAttachedFunction() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_invalid_attached_func_def.bal");
        Assert.assertEquals(result.getErrorCount(), 21);
        BAssertUtil.validateError(result, 0, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test0'", 3, 16);
        BAssertUtil.validateError(result, 1, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test1'", 3, 16);
        BAssertUtil.validateError(result, 2, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test2'", 3, 16);
        BAssertUtil.validateError(result, 3, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test3'", 3, 16);
        BAssertUtil.validateError(result, 4, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test5'", 3, 16);
        BAssertUtil.validateError(result, 5, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test6'", 3, 16);
        BAssertUtil.validateError(result, 6, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test7'", 3, 16);
        BAssertUtil.validateError(result, 7, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test9'", 3, 16);
        BAssertUtil.validateError(result, 8, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test12'", 3, 16);
        BAssertUtil.validateError(result, 9, "cannot initialize object 'Person', no " +
                "implementation for the interface 'Person.test13'", 3, 16);
        BAssertUtil.validateError(result, 10, "cannot find matching interface " +
                "function 'test0' in the object 'Person'", 42, 1);
        BAssertUtil.validateError(result, 11, "cannot find matching interface " +
                "function 'test1' in the object 'Person'", 46, 1);
        BAssertUtil.validateError(result, 12, "cannot find matching interface " +
                "function 'test2' in the object 'Person'", 50, 1);
        BAssertUtil.validateError(result, 13, "cannot find matching interface " +
                "function 'test3' in the object 'Person'", 54, 1);
        BAssertUtil.validateError(result, 14, "incompatible types: expected " +
                "'string', found 'int'", 54, 44);
        BAssertUtil.validateError(result, 15, "cannot find matching interface " +
                "function 'test5' in the object 'Person'", 62, 1);
        BAssertUtil.validateError(result, 16, "cannot find matching interface " +
                "function 'test6' in the object 'Person'", 66, 1);
        BAssertUtil.validateError(result, 17, "cannot find matching interface " +
                "function 'test7' in the object 'Person'", 70, 1);
        BAssertUtil.validateError(result, 18, "cannot find matching interface " +
                "function 'test9' in the object 'Person'", 78, 1);
        BAssertUtil.validateError(result, 19, "cannot find matching interface " +
                "function 'test12' in the object 'Person'", 90, 1);
        BAssertUtil.validateError(result, 20, "cannot find matching interface " +
                "function 'test13' in the object 'Person'", 94, 1);
    }

    @Test (description = "Negative test to test initializing objects with only interface functions")
    public void testInitializingInterfaceObject() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_initialize_interface_object.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot initialize object 'Person', " +
                "no implementation for the interface 'Person.test'", 3, 16);
    }

}
