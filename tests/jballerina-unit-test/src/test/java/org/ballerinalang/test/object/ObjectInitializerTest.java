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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for object initializer feature.
 */
public class ObjectInitializerTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src/object/ObjectProject", "init");
    }

    @Test(description = "Test object initializers that are in the same package")
    public void testStructInitializerInSamePackage1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitializerInSamePackage1");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "Peter");
    }

    @Test(description = "Test object initializers that are in different packages")
    public void testStructInitializerInAnotherPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitializerInAnotherPackage");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "Peter");
    }

    @Test(description = "Test object initializer order, 1) default values, 2) initializer, 3) literal ")
    public void testStructInitializerOrder() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitializerOrder");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
        Assert.assertEquals(returns[1].stringValue(), "AB");
    }

    @Test(description = "Test negative object initializers scenarios")
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile(this, "test-src/object/ObjectProject", "init.negative");
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
        validateError(result, i,  "object 'init' method call is allowed only within the type descriptor",
                106, 5);
    }

    @Test(description = "Test error returning object initializer invocation")
    public void testErrorReturningInitializer() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorReturningInitializer");

        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[0]).getReason(), "failed to create Person object");
    }

    @Test(description = "Test initializer with rest args")
    public void testInitializerWithRestArgs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitializerWithRestArgs");
        BMap person = (BMap) returns[0];

        Assert.assertEquals(person.get("name").stringValue(), "Pubudu");
        Assert.assertEquals(((BInteger) person.get("age")).intValue(), 27);
        Assert.assertEquals(person.get("profession").stringValue(), "Software Engineer");
        Assert.assertEquals(person.get("misc").stringValue(), "[{\"city\":\"Colombo\", \"country\":\"Sri Lanka\"}]");
    }

    @Test(description = "Test returning a custom error from initializer", groups = { "brokenOnNewParser" })
    public void testCustomErrorReturn() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCustomErrorReturn");

        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(returns[0].getType().getName(), "Err");
        Assert.assertEquals(((BError) returns[0]).getReason(), "Failed to create object");
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(), "{id:100}");

        Assert.assertEquals(returns[1].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(returns[1].getType().getName(), "Err");
        Assert.assertEquals(((BError) returns[1]).getReason(), "Failed to create object");
        Assert.assertEquals(((BError) returns[1]).getDetails().stringValue(), "{id:100}");
    }

    @Test(description = "Test value returned from initializer with a type guard")
    public void testReturnedValWithTypeGuard() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnedValWithTypeGuard");
        Assert.assertEquals(returns[0].stringValue(), "error");
    }

    @Test(description = "Test returning multiple errors from initializer")
    public void testMultipleErrorReturn() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMultipleErrorReturn");

        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[0]).getReason(), "Foo Error");
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(), "{f:\"foo\"}");

        Assert.assertEquals(returns[1].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[1]).getReason(), "Bar Error");
        Assert.assertEquals(((BError) returns[1]).getDetails().stringValue(), "{b:\"bar\"}");
    }

    @Test(description = "Test assigning to a variable declared with var")
    public void testAssigningToVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAssigningToVar");

        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[0]).getReason(), "failed to create Person object");
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(), "{\"f\":\"foo\"}");
    }

    @Test(description = "Test checkpanic expression in object init expr's argument")
    public void testCheckPanicInObjectInitArg() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCheckPanicInObjectInitArg");

        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[0]).getReason(), "Panicked");
    }

    @Test(description = "Test checkpanic expression in object init expr's argument")
    public void testCheckPanicObjectInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCheckPanicObjectInit", new BValue[]{new BBoolean(true)});
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[0]).stringValue(), "Foo Error {f:\"foo\"}");

        returns = BRunUtil.invoke(compileResult, "testCheckPanicObjectInit", new BValue[]{new BBoolean(false)});
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertEquals(((BError) returns[0]).stringValue(), "Bar Error {b:\"bar\"}");
    }

    @Test(description = "Test panic in object init function")
    public void testObjectInitPanic() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitPanic");
        Assert.assertEquals(((BError) returns[0]).stringValue(), "init panicked {}");
    }

    @Test(description = "Test invoking 'init' function in a function inside object descriptor")
    public void testInitActionInsideObjectDescriptor() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitActionInsideObjectDescriptor");

        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.STRING);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test(description = "Test invoking 'init' function in a function inside object descriptor.")
    public void testInitInvocationInsideObject() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationInsideObject");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with args.")
    public void testInitInvocationInsideObjectWithArgs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationInsideObjectWithArgs");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "return.")
    public void testObjInitWithCheck1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjInitWithCheck1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "nil.")
    public void testObjInitWithCheck2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjInitWithCheck2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with rest " +
            "args.")
    public void testInitInvocationWithRestArgs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithRestArgs");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "return and rest params.")
    public void testInitInvocationWithCheckAndRestParams1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithCheckAndRestParams1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args in a function inside object descriptor with error " +
            "nil and rest param.")
    public void testInitInvocationWithCheckAndRestParams2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithCheckAndRestParams2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values.")
    public void testInitInvocationWithDefaultParams1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithDefaultParams1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values.")
    public void testInitInvocationWithDefaultParams2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithDefaultParams2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with finite type.")
    public void testInitInvocationWithFiniteType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithFiniteType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with error as a default value.")
    public void testInitInvocationWithDefaultError() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithDefaultError");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values and some of them are " +
            "referenced by params.")
    public void testInitInvocationWithReferenceToDefaultValue1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithReferenceToDefaultValue1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values and some of them are " +
            "referenced by params.")
    public void testInitInvocationWithReferenceToDefaultValue2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitInvocationWithReferenceToDefaultValue2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with default values and returns error.")
    public void testErrorReturnWithInitialization() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorReturnWithInitialization");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with args with constant references as default values.")
    public void testConstRefsAsDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstRefsAsDefaultValue");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test invoking 'init' function with function pointer args")
    public void testFunctionPointerAsDefaultableParam1() {
        BRunUtil.invoke(compileResult, "testFunctionPointerAsDefaultableParam1");
    }

    @Test(description = "Test invoking 'init' function with lambda function args")
    public void testFunctionPointerAsDefaultableParam2() {
        BRunUtil.invoke(compileResult, "testFunctionPointerAsDefaultableParam2");
    }
}
