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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for object initializer feature.
 */
public class ObjectInitializerTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/object_init_project");
    }

    @Test(description = "Test object initializers that are in the same package")
    public void testStructInitializerInSamePackage1() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectInitializerInSamePackage1");

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "Peter");
    }

    @Test(description = "Test object initializers that are in different packages")
    public void testStructInitializerInAnotherPackage() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectInitializerInAnotherPackage");

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "Peter");
    }

    @Test(description = "Test object initializer order, 1) default values, 2) initializer, 3) literal ")
    public void testStructInitializerOrder() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectInitializerOrder");

        Assert.assertEquals(returns.get(0), 40L);
        Assert.assertEquals(returns.get(1).toString(), "AB");
    }

    @Test(description = "Test negative object initializers scenarios")
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_init_negative_project");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "attempt to refer to non-accessible symbol 'student.init'", 5, 21);

    }

    @Test(description = "Test negative object initializers scenarios")
    public void testObjectInitializerNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_initializer_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 8);
        int i = 0;
        validateError(result, i++, "redeclared symbol 'Foo.init'", 23, 14);
        validateError(result, i++,
                "object initializer function can not be declared as private", 27, 4);
        validateError(result, i++, "incompatible types: expected 'Person', found '(Person|error)'", 47, 17);
        validateError(result, i++, "incompatible types: expected 'Person', found '(Person|error)'", 48, 17);
        validateError(result, i++, "invalid object constructor return type 'string?', " +
                "expected a subtype of 'error?' containing '()'", 54, 29);
        validateError(result, i++,
                "invalid object constructor return type 'error', expected a subtype of 'error?' containing '()'",
                63, 29);
        validateError(result, i++,
                "invalid object constructor return type '(FooErr|BarErr)', expected a subtype of 'error?' " +
                        "containing '()'", 89, 29);
        validateError(result, i, "object 'init' method call is allowed only within the type descriptor",
                106, 5);
    }

    @Test(description = "Test error returning object initializer invocation")
    public void testErrorReturningInitializer() {
        BRunUtil.invoke(compileResult, "testErrorReturningInitializer");
    }

    @Test(description = "Test initializer with rest args")
    public void testInitializerWithRestArgs() {
        Object returns = BRunUtil.invoke(compileResult, "testInitializerWithRestArgs");
        BObject person = (BObject) returns;

        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "Pubudu");
        Assert.assertEquals(person.get(StringUtils.fromString("age")), 27L);
        Assert.assertEquals(person.get(StringUtils.fromString("profession")).toString(), "Software Engineer");
        Assert.assertEquals(person.get(StringUtils.fromString("misc")).toString(),
                "[{\"city\":\"Colombo\",\"country\":\"Sri Lanka\"}]");
    }

    @Test(description = "Test returning a custom error from initializer")
    public void testCustomErrorReturn() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testCustomErrorReturn");

        Assert.assertEquals(TypeUtils.getReferredType(getType(returns.get(0))).getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(getType(returns.get(0)).getName(), "Err");
        Assert.assertEquals(((BError) returns.get(0)).getMessage(), "Failed to create object");
        Assert.assertEquals(((BError) returns.get(0)).getDetails().toString(), "{\"id\":100}");

        Assert.assertEquals(TypeUtils.getReferredType(getType(returns.get(1))).getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(getType(returns.get(1)).getName(), "Err");
        Assert.assertEquals(((BError) returns.get(1)).getMessage(), "Failed to create object");
        Assert.assertEquals(((BError) returns.get(1)).getDetails().toString(), "{\"id\":100}");
    }

    @Test(description = "Test value returned from initializer with a type guard")
    public void testReturnedValWithTypeGuard() {
        Object returns = BRunUtil.invoke(compileResult, "testReturnedValWithTypeGuard");
        Assert.assertEquals(returns.toString(), "error");
    }

    @Test(description = "Test returning multiple errors from initializer")
    public void testMultipleErrorReturn() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testMultipleErrorReturn");

        Assert.assertEquals(TypeUtils.getReferredType(getType(returns.get(0))).getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(((BError) returns.get(0)).getMessage(), "Foo Error");
        Assert.assertEquals(((BError) returns.get(0)).getDetails().toString(), "{\"f\":\"foo\"}");

        Assert.assertEquals(TypeUtils.getReferredType(getType(returns.get(1))).getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(((BError) returns.get(1)).getMessage(), "Bar Error");
        Assert.assertEquals(((BError) returns.get(1)).getDetails().toString(), "{\"b\":\"bar\"}");
    }

    @Test(description = "Test assigning to a variable declared with var")
    public void testAssigningToVar() {
        BRunUtil.invoke(compileResult, "testAssigningToVar");
    }

    @Test(description = "Test checkpanic expression in object init expr's argument")
    public void testCheckPanicInObjectInitArg() {
        BRunUtil.invoke(compileResult, "testCheckPanicInObjectInitArg");
    }

    @Test(description = "Test checkpanic expression in object init expr's argument")
    public void testCheckPanicObjectInit() {
        Object returns = BRunUtil.invoke(compileResult, "testCheckPanicObjectInit", new Object[]{(true)});
        Assert.assertEquals(TypeUtils.getReferredType(getType(returns)).getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(returns.toString(), "error FooErr (\"Foo Error\",f=\"foo\")");

        returns = BRunUtil.invoke(compileResult, "testCheckPanicObjectInit", new Object[]{(false)});
        Assert.assertEquals(TypeUtils.getReferredType(getType(returns)).getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(returns.toString(), "error BarErr (\"Bar Error\",b=\"bar\")");
    }

    @Test(description = "Test panic in object init function")
    public void testObjectInitPanic() {
        BRunUtil.invoke(compileResult, "testObjectInitPanic");
    }

    @Test(description = "Test invoking 'init' function in a function inside object descriptor")
    public void testInitActionInsideObjectDescriptor() {
        Object returns = BRunUtil.invoke(compileResult, "testInitActionInsideObjectDescriptor");

        Assert.assertEquals(getType(returns).getTag(), TypeTags.STRING_TAG);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test(description = "Test invoking 'init' function in a function inside object descriptor.")
    public void testInitInvocationInsideObject() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationInsideObject");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with args.")
    public void testInitInvocationInsideObjectWithArgs() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationInsideObjectWithArgs");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "return.")
    public void testObjInitWithCheck1() {
        Object returns = BRunUtil.invoke(compileResult, "testObjInitWithCheck1");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "nil.")
    public void testObjInitWithCheck2() {
        Object returns = BRunUtil.invoke(compileResult, "testObjInitWithCheck2");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with rest " +
            "args.")
    public void testInitInvocationWithRestArgs() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithRestArgs");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "return and rest params.")
    public void testInitInvocationWithCheckAndRestParams1() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithCheckAndRestParams1");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "nil and rest param.")
    public void testInitInvocationWithCheckAndRestParams2() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithCheckAndRestParams2");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values.")
    public void testInitInvocationWithDefaultParams1() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithDefaultParams1");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values.")
    public void testInitInvocationWithDefaultParams2() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithDefaultParams2");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with finite type.")
    public void testInitInvocationWithFiniteType() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithFiniteType");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with error as a default value.")
    public void testInitInvocationWithDefaultError() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithDefaultError");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values and some of them are " +
            "referenced by params.")
    public void testInitInvocationWithReferenceToDefaultValue1() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithReferenceToDefaultValue1");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values and some of them are " +
            "referenced by params.")
    public void testInitInvocationWithReferenceToDefaultValue2() {
        Object returns = BRunUtil.invoke(compileResult, "testInitInvocationWithReferenceToDefaultValue2");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values and returns error.")
    public void testErrorReturnWithInitialization() {
        Object returns = BRunUtil.invoke(compileResult, "testErrorReturnWithInitialization");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with constant references as default values.")
    public void testConstRefsAsDefaultValue() {
        Object returns = BRunUtil.invoke(compileResult, "testConstRefsAsDefaultValue");
        Assert.assertTrue((Boolean) returns, "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with function pointer args")
    public void testFunctionPointerAsDefaultableParam1() {
        BRunUtil.invoke(compileResult, "testFunctionPointerAsDefaultableParam1");
    }

    @Test(description = "Test invoking 'init' function with lambda function args")
    public void testFunctionPointerAsDefaultableParam2() {
        BRunUtil.invoke(compileResult, "testFunctionPointerAsDefaultableParam2");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
