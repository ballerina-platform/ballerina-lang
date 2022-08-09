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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test cases for user defined object types in ballerina.
 */
public class ObjectTest {

    private static final String INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION =
            "cannot use 'check' in the default expression of a record field";
    private static final String INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT =
            "cannot use 'check' in an object field initializer of an object with no 'init' method";

    private CompileResult checkInInitializerResult;
    private CompileResult checkFunctionReferencesResult;

    @BeforeClass
    public void setUp() {
        checkInInitializerResult = BCompileUtil.compile("test-src/object/object_field_initializer_with_check.bal");
        checkFunctionReferencesResult = BCompileUtil.compile("test-src/object/object_function_pointer.bal");
    }

    @AfterClass
    public void tearDown() {
        checkInInitializerResult = null;
        checkFunctionReferencesResult = null;
    }

    @Test(description = "Test Basic object as struct")
    public void testBasicStructAsObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-simple-struct.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testSimpleObjectAsStruct");

        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "sample name");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test(description = "Test Object field defaultable")
    public void testObjectFieldDefaultable() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-field-defaultable.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectFieldDefaultable");

        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "sample name");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test(description = "Test Basic object as struct with just new")
    public void testBasicStructAsObjectWithJustNew() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-simple-struct.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testSimpleObjectAsStructWithNew");

        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "sample name");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test(description = "Test object with init function")
    public void testObjectWithSimpleInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithSimpleInit");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 17L);
        Assert.assertEquals(returns.get(1).toString(), "sample value1");
        Assert.assertEquals(returns.get(2), 99L);
        Assert.assertEquals(returns.get(3).toString(), "default value");
    }

    @Test(description = "Test object with defaultable field in init function")
    public void testObjectWithDefaultableField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-defaultable-field.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithSimpleInit");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 109L);
        Assert.assertEquals(returns.get(1).toString(), "sample value1");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "default value");
    }

    @Test(description = "Test object with init with different values")
    public void testObjectWithSimpleInitWithDiffValues() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithSimpleInitWithDiffValues");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 37L);
        Assert.assertEquals(returns.get(1).toString(), "sample value1");
        Assert.assertEquals(returns.get(2), 675L);
        Assert.assertEquals(returns.get(3).toString(), "adding value in invocation");
    }

    @Test(description = "Test object without RHS type")
    public void testObjectWithoutRHSType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithoutRHSType");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 37L);
        Assert.assertEquals(returns.get(1).toString(), "sample value1");
        Assert.assertEquals(returns.get(2), 675L);
        Assert.assertEquals(returns.get(3).toString(), "adding value in invocation");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-attach-funcs.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithAttachedFunc1");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 361L);
        Assert.assertEquals(returns.get(1).toString(), "added values february");
        Assert.assertEquals(returns.get(2), 99L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test(description = "Test object with self keyword")
    public void testObjectWithSelfKeyword() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword.bal");
        Object returns = BRunUtil.invoke(compileResult, "testObjectWithSelfKeyword");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "sample name");
    }

    @Test(description = "Test object with calling attached functions")
    public void testObjectCallAttachedFunctions() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-call-attached-functions.bal");
        Object returns = BRunUtil.invoke(compileResult, "testObjectCallAttachedFunctions");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "sample name");
    }

    @Test(description = "Test object inside object with different values")
    public void testObjectInsideObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword-pass-values.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectInsideObject");

        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0).toString(), "sample name");
        Assert.assertEquals(returns.get(1).toString(), "changed value");
    }

    @Test(description = "Test object self as a value")
    public void testObjectPassSelfAsValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword-pass-values.bal");
        Object returns = BRunUtil.invoke(compileResult, "testGetValueFromPassedSelf");

        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "sample name");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-interface.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithInterface");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 80L);
        Assert.assertEquals(returns.get(1).toString(), "sample value1");
        Assert.assertEquals(returns.get(2), 100L);
        Assert.assertEquals(returns.get(3).toString(), "adding value in invocation uuuu");
    }

    @Test(description = "Test object with attached function implementation")
    public void testObjectWithAttachedFunctionImpl() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-interface-and-impl.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithInterface");

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 17L);
        Assert.assertEquals(returns.get(1).toString(), "february");
    }

    @Test(description = "Test object with default initializer")
    public void testObjectWithWithDefaultInitialize() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testGetDefaultValuesInObject");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 0L);
        Assert.assertEquals(returns.get(1).toString(), "sample value");
        Assert.assertEquals(returns.get(2), 0L);
        Assert.assertEquals(returns.get(3).toString(), "");
    }

    @Test(description = "Test passing value to a defaultable object field")
    public void testPassingValueForDefaultableObjectField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_values_for_defaultable_field.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "passValueForDefaultableObjectField");

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 50L);
        Assert.assertEquals(returns.get(1).toString(), "passed in name value");
    }

    @Test(description = "Test shadowing object field")
    public void testShadowingObjectField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_shadow_field.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testShadowingObjectField");

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 50L);
        Assert.assertEquals(returns.get(1).toString(), "passed in name value");
    }

    @Test(description = "Test initializing object in return statement with same type")
    public void testNewAsReturnWithSameType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_new_in_return.bal");
        Object returns = BRunUtil.invoke(compileResult, "testCreateObjectInReturnSameType");

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 5L);
    }

    @Test(description = "Test initializing object in return statement with different type")
    public void testNewAsReturnWithDifferentType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_new_in_return.bal");
        Object returns = BRunUtil.invoke(compileResult, "testCreateObjectInReturnDifferentType");

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 12L);
    }

//
//    @Test(description = "Test object with default initializer with default values") //TODO fix
//    public void testObjectWithWithDefaultInitializeWithDefaultValues() {
//        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_with_default_test.bal");
//        BArray returns = (BArray) JvmRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectFields");
//
//        Assert.assertEquals(returns.size(), 2);
//        Assert.assertSame(returns.get(0).getClass(), Long.class);
//        Assert.assertTrue(returns.get(1) instanceof BString);
//
//        Assert.assertEquals(returns.get(0), 15L);
//        Assert.assertEquals(returns.get(1).toString(), "hello world");
//    }

    @Test(description = "Test object with default initialize global variable")
    public void testObjectWithDefaultInitializeGlobalVar() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectGlobalVar");

        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 0L);
        Assert.assertEquals(returns.get(1).toString(), "sample value");
        Assert.assertEquals(returns.get(2), 0L);
        Assert.assertEquals(returns.get(3).toString(), "");
    }

    @Test(description = "Test object self reference with defaultable")
    public void testObjectSelfreferenceWithDefaultable() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_cyclic_" +
                "self_reference_with_default.bal");
        Object returns = BRunUtil.invoke(compileResult, "testCyclicReferenceWithDefaultable");

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 89L);
    }

    @Test(description = "Test function references from an object", dataProvider = "functionReferencesFromObjectTests")
    public void testFunctionReferencesFromObjects(String functionName) {
        BRunUtil.invoke(checkFunctionReferencesResult, functionName);
    }

    @DataProvider
    private Object[] functionReferencesFromObjectTests() {
        return new String[]{
                "testObjectFunctionPointer",
                "testObjectFunctionPointerFieldAccess"
        };
    }

    @Test(description = "Test object any type field as a constructor parameter")
    public void testObjectAnyTypeFieldAsConstructorParam() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_field_any_type.bal");
        Object returns = BRunUtil.invoke(compileResult, "testObjectWithAnyTypeField");

        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "grainier");
    }

    @Test(description = "Test object recursive reference with nillable")
    public void testRecursiveObjectRefWithNillable() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_recurs_with_nill.bal");
        Object returns = BRunUtil.invoke(compileResult, "testRecursiveObjectWithNill");

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 90L);
    }

    @Test(description = "Test object field with expr as defaultable")
    public void testFieldWithExpr() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_field_with_expr.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testFieldWithExpr");

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 88L);
        Assert.assertEquals(returns.get(1).toString(), "sanjiva");
    }

    @Test(description = "Negative test to test uninitialized object variables")
    public void testObjectNonInitializableSemanticsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_with_non_defaultable_semantics_negative" +
                ".bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        Assert.assertEquals(result.getWarnCount(), 1);
        BAssertUtil.validateWarning(result, 0, "unused variable 'e'", 4, 5);
        BAssertUtil.validateError(result, 1, "variable 'p' is not initialized", 5, 13);
        BAssertUtil.validateError(result, 2, "variable 'p' is not initialized", 5, 20);
    }

    @Test(description = "Negative test to test uninitialized object variables")
    public void testObjectNegativeTestForNonInitializable() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_with_non_defaultable_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined method 'attachInterface' in object 'Person'", 5, 15);
    }

    @Test(description = "Negative test to test returning different type without type name")
    public void testObjectNegativeTestForReturnDifferentType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_new_in_return_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 5);
        BAssertUtil.validateError(result, 0, "too many arguments in call to 'new()'", 18, 12);
        BAssertUtil.validateError(result, 1, "cannot infer type of the object from 'other'", 27, 19);
        BAssertUtil.validateError(result, 2, "invalid variable definition; can not infer the assignment type.", 27, 19);
        BAssertUtil.validateError(result, 3, "too many arguments in call to 'new()'", 35, 24);
        BAssertUtil.validateError(result, 4, "too many arguments in call to 'new()'", 39, 23);
    }

    @Test(description = "Negative test to test returning different type without type name")
    public void testUnInitializableObjFieldAsParam() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_un_initializable_field.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "uninitialized field 'foo'", 18, 5);
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/14633
    @Test(description = "Negative test to test self reference types", enabled = false)
    public void testSelfReferenceType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_cyclic_self_reference.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "invalid cyclic type reference in " + "'[Person, Employee, Foo, Bar]'",
                32, 5);
    }

    @Test(description = "Negative test to test self reference types")
    public void testNonMatchingAttachedFunctionSemanticsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_attached_func_def_semantics_negative.bal");
        int index = 0;
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'int'", 13, 16);
        BAssertUtil.validateError(result, index, "incompatible types: expected 'int', found 'string'", 23, 17);
    }

    @Test(description = "Negative test to test self reference types")
    public void testNonMatchingAttachedFunction() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_attached_func_def_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "this function must return a result", 14, 5);
    }

    @Test(description = "Negative test to test initializing objects with only interface functions")
    public void testInitializingInterfaceObject() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_initialize_interface_object.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot initialize abstract object 'Person'", 3, 16);
    }

    @Test(description = "Negative test to test initializing object with struct literal")
    public void testInitializingObjectWithStructLiteral() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_init_with_struct_literal.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "invalid usage of record literal with type 'Person'", 1, 13);
        BAssertUtil.validateError(result, 1, "invalid usage of record literal with type 'Person'", 4, 16);
    }

    @Test(description = "Negative test to test referring undefined field in constructor")
    public void testReferUndefinedFieldBal() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_access_undefined_field.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined symbol 'abc'", 6, 9);
    }

    @Test(description = "Negative test to test invalid object init functions")
    public void testObjectInitFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_init_function_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "object 'init' method cannot have an 'external' implementation",
                19, 5);
        BAssertUtil.validateError(result, 1, "object 'init' method cannot have an 'external' implementation",
                23, 5);
    }

    @Test(description = "Test nillable initialization")
    @SuppressWarnings("unchecked")
    public void testNillableInitialization() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_nillable_init.bal");
        BArray personInstances = (BArray) BRunUtil.invoke(result, "getPersonInstances");
        Assert.assertEquals(getAgeField((BObject) personInstances.get(0)), 0);
        Assert.assertEquals(getAgeField((BObject) personInstances.get(1)), 0);
        Assert.assertEquals(getAgeField((BObject) personInstances.get(2)), 0);
        Assert.assertEquals(getAgeField((BObject) personInstances.get(3)), 0);
        Assert.assertEquals(getAgeField((BObject) personInstances.get(4)), 0);
        Assert.assertEquals(getAgeField((BObject) personInstances.get(5)), 0);

        Object results = BRunUtil.invoke(result, "getEmployeeInstance");
        BObject employee = (BObject) results;
        Assert.assertEquals(getAgeField((BObject) employee.get(StringUtils.fromString("p3"))), 0);
        Assert.assertEquals(getAgeField((BObject) employee.get(StringUtils.fromString("p4"))), 0);
        Assert.assertEquals(getAgeField((BObject) employee.get(StringUtils.fromString("p5"))), 0);
        Assert.assertEquals(getAgeField((BObject) employee.get(StringUtils.fromString("p6"))), 0);
    }

    private long getAgeField(BObject person) {
        return (long) person.get(StringUtils.fromString("age"));
    }

    @Test(description = "Negative test to test object visibility modifiers")
    public void testObjectVisibilityModifiers() {
        CompileResult result = BCompileUtil.compile("test-src/object/test_pkg2");
        Assert.assertEquals(result.getErrorCount(), 12);
        int index = 0;

        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'name'", 34, 17);
        BAssertUtil.validateError(result, index++,
                "undefined field 'name' in object 'testorg/test_pkg2:1.0.0:Employee'", 34, 22);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'Employee.getAge'",
                38, 14);
        BAssertUtil.validateError(result, index++,
                "undefined method 'getAge' in object 'testorg/test_pkg2:1.0.0:Employee'", 38, 19);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'name'", 45, 17);
        BAssertUtil.validateError(result, index++,
                "undefined field 'name' in object 'testorg/test_pkg2.pkg1:1.0.0:Employee'", 45, 22);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'email'", 46, 17);
        BAssertUtil.validateError(result, index++,
                "undefined field 'email' in object 'testorg/test_pkg2.pkg1:1.0.0:Employee'", 46, 22);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'Employee.getAge'",
                49, 14);
        BAssertUtil.validateError(result, index++, "undefined method 'getAge' " +
                        "in object 'testorg/test_pkg2.pkg1:1.0.0:Employee'",
                49, 19);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol " + "'Employee" +
                ".getEmail'", 50, 17);
        BAssertUtil.validateError(result, index, "undefined method 'getEmail' in object " +
                        "'testorg/test_pkg2.pkg1:1.0.0:Employee'",
                50, 22);
    }

    @Test(description = "Negative test to test unknown object field type")
    public void testUnknownObjectFieldType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_undefined_field_type_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unknown type 'Employee'", 3, 13);
    }

    @Test
    public void testAttachFunctionsWithIdenticalRestParams() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/attach_func_with_identical_rest_params" +
                ".bal");
        Object returns = BRunUtil.invoke(compileResult, "testAttachFunctionsWithIdenticalRestParams");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hello foo");
    }

    @Test
    public void testStructPrint() {
        PrintStream prevOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-print.bal");
        System.setOut(new PrintStream(out));
        Object returns = BRunUtil.invoke(compileResult, "testPrintingObject");
        System.setOut(prevOut);

        Assert.assertTrue(returns instanceof BObject);
        Assert.assertEquals(returns.toString(), "{age:20, name:John}");
        Assert.assertEquals(out.toString().trim(), "{\"age\":20, \"name\":\"John\"}");
    }

    @Test
    public void testObjectInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_constructor.bal");
        Object returns = BRunUtil.invoke(compileResult, "testObjectInit");

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 1L);
    }

    @Test
    public void testObjectPrivateMethods() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_private_method.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testPrivateMethodAccess");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0), 15000L);
        Assert.assertEquals(returns.get(1), 12500L);
    }

    @Test(description = "Negative test to test object private methods")
    public void testObjectPrivateMethodsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_private_method_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        int i = 0;
        BAssertUtil.validateError(result, i++,
                "attempt to refer to non-accessible symbol 'Person.incrementSalary'", 45, 5);
        BAssertUtil.validateError(result, i++,
                "undefined method 'incrementSalary' in object 'Person'", 45, 12);
        BAssertUtil.validateError(result, i++,
                "attempt to refer to non-accessible symbol 'Person.decrementAndUpdateSalary'", 46,
                13);
        BAssertUtil.validateError(result, i,
                "undefined method 'decrementAndUpdateSalary' in object 'Person'", 46, 20);
    }

    @Test
    public void testAbstractClientObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/abstract_client_object_method.bal");
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAbstractClientObject");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0), 15000L);
        Assert.assertEquals(returns.get(1), 12500L);
        Assert.assertEquals(returns.get(2), 15000L);
        Assert.assertEquals(returns.get(3), 12500L);
    }

    @Test
    public void testObjectWithFutureTypeFieldWithValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_with_future_type_field.bal");
        Object returns = BRunUtil.invoke(compileResult, "getIntFromFutureField");

        Assert.assertEquals(returns, 20L);
    }

    @Test(description = "Test initialization of object union")
    @SuppressWarnings("unchecked")
    public void testUnionTypeObjectInit() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        Object a = BRunUtil.invoke(objectTypeUnion, "getObj4");
        BObject aMap = (BObject) a;
        Assert.assertEquals((aMap.get(StringUtils.fromString("val"))), 4L);

        Object ab = BRunUtil.invoke(objectTypeUnion, "getObj0");
        BObject bMap = (BObject) ab;
        Assert.assertEquals((bMap.get(StringUtils.fromString("val"))), 0L);

        BArray tupple = (BArray) BRunUtil.invoke(objectTypeUnion, "getLocals");
        BObject localObj4 = (BObject) tupple.get(0);
        Assert.assertEquals((localObj4.get(StringUtils.fromString("val"))), 4L);

        BObject localObj0 = (BObject) tupple.get(1);
        Assert.assertEquals((localObj0.get(StringUtils.fromString("val"))), 0L);

        BObject localObj3 = (BObject) tupple.get(2);
        Assert.assertEquals((localObj3.get(StringUtils.fromString("val"))), 3L);
    }

    @Test(description = "Test initialization of object union with union members of mixed types")
    @SuppressWarnings("unchecked")
    public void testUnionTypeObjectInitMixedMember() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        Object a = BRunUtil.invoke(objectTypeUnion, "getMixedUnionMembers");
        BObject aMap = (BObject) a;
        Assert.assertEquals((aMap.get(StringUtils.fromString("val"))), 0L);
    }

    @Test(description = "Test return new() when return type is a union")
    @SuppressWarnings("unchecked")
    public void testUnionTypeOReturnType() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        Object a = BRunUtil.invoke(objectTypeUnion, "returnDifferentObectInit1");
        BObject aMap = (BObject) a;
        Assert.assertEquals((aMap.get(StringUtils.fromString("age"))), 5L);
    }

    @Test(description = "Test choosing union member differentiating on rest param type")
    @SuppressWarnings("unchecked")
    public void testUnionTypeSelectOnRestParamType() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        Object a = BRunUtil.invoke(objectTypeUnion, "selectOnRestParam");
        BObject aMap = (BObject) a;
        Assert.assertEquals((aMap.get(StringUtils.fromString("val"))), 5L);

        Object b = BRunUtil.invoke(objectTypeUnion, "selectOnRestParamInReturnType");
        BObject retChoose = (BObject) b;
        Assert.assertEquals((retChoose.get(StringUtils.fromString("val"))), 5L);
    }

    @Test(description = "Test invoking object inits with union params in another object's function")
    public void testUnionsAsAnInitParam() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");
        Object a = BRunUtil.invoke(objectTypeUnion, "testUnionsAsAnInitParam");
        BObject foo = (BObject) a;
        Assert.assertEquals(
                ((BObject) foo.get(StringUtils.fromString("bar"))).get(StringUtils.fromString("p")).toString(),
                "{\"name\":\"John Doe\"}");
    }

    @Test(description = "Test invoking object inits with union params in another object's function")
    public void testObjectInitFunctionWithDefaultableParams() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/test_pkg1");
        BArray result = (BArray) BRunUtil.invoke(compileResult, "testObjectInitFunctionWithDefaultableParams");
        Assert.assertEquals((result.get(0)), 900000L);
        Assert.assertEquals((result.get(1)), 10000L);
        Assert.assertEquals((result.get(2)), 20000L);
        Assert.assertEquals((result.get(3)), 30000L);
        Assert.assertEquals((result.get(4)), 40000L);
    }

    @Test(description = "Test invoking object inits with union params in another object's function")
    public void testObjectInitFunctionWithDefaultableParams2() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/test_pkg1");
        BArray result = (BArray) BRunUtil.invoke(compileResult, "testObjectInitFunctionWithDefaultableParams2");
        Assert.assertEquals((result.get(0)), 1.1);
        Assert.assertEquals((result.get(1)), 1L);
    }

    @Test(description = "Negative test for object union type inference")
    public void testNegativeUnionTypeInit() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/object/object_type_union_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "ambiguous type '(Obj|Obj2|Obj3|Obj4)'", 48, 25);
        BAssertUtil.validateError(resultNegative, i++, "ambiguous type '(Obj|Obj2|Obj3|Obj4)'", 49, 25);
        BAssertUtil.validateError(resultNegative, i++, "cannot infer type of the object from '(Obj|Obj2|Obj3|Obj4)'",
                50, 46);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(PersonRec|EmployeeRec)', found 'string'", 71, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type of the object from '(InitObjOne|InitObjTwo|float)'", 114, 38);
        BAssertUtil.validateError(resultNegative, i++,
                "positional argument not allowed after named arguments", 114, 53);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type of the object from '(InitObjOne|InitObjTwo|int)'", 119, 36);
        BAssertUtil.validateError(resultNegative, i++,
                "ambiguous type '(InitObjOne|InitObjTwo|float)'", 120, 38);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type of the object from '(InitObjOne|InitObjTwo|float)'", 121, 38);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found 'string'", 126, 51);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type of the object from '(InitObjOne|InitObjThree|boolean|string)'", 127, 50);
        BAssertUtil.validateError(resultNegative, i++,
                "positional argument not allowed after named arguments", 128, 59);
        BAssertUtil.validateError(resultNegative, i++,
                "positional argument not allowed after named arguments", 129, 70);
        BAssertUtil.validateError(resultNegative, i++,
                "positional argument not allowed after named arguments", 129, 75);
        BAssertUtil.validateError(resultNegative, i++,
                "positional argument not allowed after named arguments", 129, 80);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test(description = "Negative test for field name and method name being same")
    public void testFieldWithSameNameAsMethod() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/object/object_field_with_same_name_as_method_neg.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "redeclared symbol 'ObjectA.someInt'", 21, 14);
        BAssertUtil.validateError(resultNegative, i++, "redeclared symbol 'ObjectA.someFloat'", 26, 14);
        BAssertUtil.validateError(resultNegative, i++, "undefined method 'someFloat' in object 'ObjectA'", 36, 25);
        BAssertUtil.validateError(resultNegative, i++, "undefined method 'someInt' in object 'ObjectA'", 44, 17);
        BAssertUtil.validateError(resultNegative, i++, "undefined method 'someFloat' in object 'ObjectA'", 49, 19);

        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test(description = "Test object attach func returning tuple with non blocking call")
    public void testObjectAttachFuncReturningTuple() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_attach_func_ret_tuple.bal");
        BArray result = (BArray) BRunUtil.invoke(compileResult, "testReturningTuple");
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.get(0) instanceof BString);
        Assert.assertTrue(result.get(1) instanceof BString);
        Assert.assertEquals(result.get(0).toString(), "firstValue");
        Assert.assertEquals(result.get(1).toString(), "secondValue");
    }

    @Test(description = "Negative test to test duplicate fields")
    public void testDuplicateFields() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_field_negative.bal");
        BAssertUtil.validateError(result, 0, "redeclared symbol 'error'", 20, 18);
        Assert.assertEquals(result.getErrorCount(), 1);
    }

    @Test(description = "Test lang lib object type inclusion")
    public void testLangLibObjectInclusion() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_langlib_inclusion.bal");
        BArray result = (BArray) BRunUtil.invoke(compileResult, "testLangLibObjectInclusion");
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.get(0) instanceof BString);
        Assert.assertTrue(result.get(1) instanceof BString);
        Assert.assertEquals(result.get(0).toString(), "Name:David age:10");
        Assert.assertEquals(result.get(1).toString(), "Default string");
    }

    @Test(description = "Negative test to test calling lang lib functions for objects")
    public void testLangLibFunctionInvocation() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_langlib_function_invocation_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "undefined method 'toString' in object 'Person'",
                27, 25);
        BAssertUtil.validateError(result, 1, "no implementation found for the method 'returnString' " +
                        "of class 'FrameImpl'",
                34, 1);
        BAssertUtil.validateError(result, 2, "no implementation found for the method 'start' " +
                        "of class 'DynamicListenerImpl'",
                46, 1);
        BAssertUtil.validateError(result, 3, "no implementation found for the method 'toString' " +
                        "of class 'StackFrameImpl'",
                58, 1);
    }

    @Test
    public void testInvalidUsageOfCheckInObjectFieldInitializer() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_field_initializer_with_check_negative.bal");
        int i = 0;
        BAssertUtil.validateWarning(result, i++, "unused variable 'q'", 20, 9);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 23, 13);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 24, 21);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 25, 23);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 38, 29);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 42, 21);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 59, 19);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 64, 11);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 65, 16);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 77, 21);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 78, 21);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 78, 42);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 82, 23);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 89, 21);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 90, 21);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION, 90, 42);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 93, 27);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 99, 18);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 99, 30);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 109, 13);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 110, 21);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found '(error|MyError)'", 111, 23);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 126, 25);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected '(MyError|MyErrorTwo)?', found 'error'", 142, 19);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected '(MyError|MyErrorTwo)?', found '(error|MyError)" +
                "'", 148, 16);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyErrorTwo?', found 'MyError'", 157, 23);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 170, 13);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 175, 13);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 179, 13);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 184, 13);
        BAssertUtil.validateError(result, i++, "let expressions are not yet supported for object fields", 198, 13);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 198, 25);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 198, 43);
        BAssertUtil.validateError(result, i++, "let expressions are not yet supported for object fields", 202, 13);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 202, 25);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 202, 43);
        BAssertUtil.validateError(result, i++, "let expressions are not yet supported for object fields", 210, 13);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 210, 25);
        BAssertUtil.validateError(result, i++, "let expressions are not yet supported for object fields", 214, 13);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 214, 25);
        BAssertUtil.validateError(result, i++, "let expressions are not yet supported for object fields", 223, 17);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 223, 29);
        BAssertUtil.validateError(result, i++, INVALID_USAGE_OF_CHECK_IN_INITIALIZER_IN_OBJECT_WITH_NO_INIT, 223, 51);
        BAssertUtil.validateError(result, i++, "let expressions are not yet supported for object fields", 226, 17);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 226, 29);
        BAssertUtil.validateError(result, i++, "usage of 'check' in field initializer is allowed only when compatible" +
                " with the return type of the 'init' method: expected 'MyError?', found 'error'", 226, 51);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 233, 5);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 234, 5);
        Assert.assertEquals(result.getErrorCount(), i - 3);
        Assert.assertEquals(result.getWarnCount(), 3);
    }

    @Test(dataProvider = "checkInObjectFieldInitializerTests")
    public void testValidUsageOfCheckInObjectFieldInitializer(String funcName) {
        BRunUtil.invoke(checkInInitializerResult, funcName);
    }

    @DataProvider
    private Object[][] checkInObjectFieldInitializerTests() {
        return new Object[][]{
                {"testCheckInObjectFieldInitializer1"},
                {"testCheckInObjectFieldInitializer2"},
                {"testCheckInObjectFieldInitializer3"},
                {"testCheckInObjectFieldInitializer4"},
                {"testCheckInObjectFieldInitializer5"},
                {"testCheckInObjectFieldInitializer6"}
        };
    }
}
