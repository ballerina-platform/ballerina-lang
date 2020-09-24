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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
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
 * Test cases for object reference type in ballerina.
 */
public class ObjectTypeReferenceTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/object-type-reference.bal");
    }

    @Test
    public void testSimpleObjectTypeReferenceSemanticsNegative_1() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-1-semantics" +
                "-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'Employee1' is not an object", 32,
                6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'salary'", 48, 6);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[Foo, Foo]'", 52, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[A, B, C, D, A]'", 57, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[C, E, C]'", 57, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[B, C, D, A, B]'", 61, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                "cyclic type reference in '[C, E, C]'", 61, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[C, D, A, B, C]'", 65, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[C, E, C]'", 65, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[C, E, C]'", 70, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[D, A, B, C, D]'", 70, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[C, D, A, B, C]'", 74, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "cyclic type reference in '[E, C, E]'", 74, 1);
        // Disable as new class definition will replace objects that can have method implementations.
//        BAssertUtil.validateError(negativeResult, i++,
//                                  "no implementation found for the function 'getName' of non-abstract object " +
//                                          "'Manager2'", 96, 5);
        i++;
//        BAssertUtil.validateError(negativeResult, i++,
//                "no implementation found for the function 'getSalary' of non-abstract object 'Manager2'", 96, 5);
        i++;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'Q' is not an object", 101, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared type reference 'Person1'", 111, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'getName': trying to copy a duplicate " +
                "function through referenced type 'ObjectWithFunction'", 120, 5);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'getName': trying to copy a duplicate " +
                "function through referenced type 'ObjectWithRedeclaredFunction_1'", 125, 6);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'Baz'", 129, 6);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'Tar'", 133, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'xyz'", 154, 6);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testSimpleObjectTypeReferenceNegative_1() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-1-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'age'", 27, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'name'", 27, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'salary'", 28, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'age'", 42, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'name'", 42, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'salary'", 45, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'age'", 66, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'name'", 66, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'salary'", 66, 6);
        BAssertUtil.validateError(negativeResult, i++, "variable 'name' is not initialized", 69, 16);
        BAssertUtil.validateError(negativeResult, i++, "variable 'salary' is not initialized", 73, 16);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testCyclicDependencyReferencesObjectTypeReferenceNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-cyclic-dependency" +
                                                                    "-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[Foo, Foo]'", 18, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[A, B, C, D, A]'", 23, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[C, E, C]'", 23, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[B, C, D, A, B]'", 27, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[C, E, C]'", 27, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[C, D, A, B, C]'", 31, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[C, E, C]'", 31, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[C, E, C]'", 36, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[D, A, B, C, D]'", 36, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[C, D, A, B, C]'", 40, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[E, C, E]'", 40, 1);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testSimpleObjectTypeReferenceSemanticsNegative_2() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-2-semantics" +
                "-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 4);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "only type references are allowed as type inclusions",
                18, 6);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'Bar'", 19, 6);
        BAssertUtil.validateError(negativeResult, i++, "only type references are allowed as type inclusions", 20, 6);
        BAssertUtil.validateError(negativeResult, i, "unknown type 'YYY'", 29, 6);
    }

    @Test
    public void testSimpleObjectTypeReferenceNegative_2() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-2-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 2);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'x'", 18, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'y'", 18, 6);
    }

    @Test
    public void testSimpleObjectTypeReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleObjectTypeReference");
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 99);
        Assert.assertEquals(returns[1].stringValue(), "sample name 2 from inner function");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 8.0);
        Assert.assertEquals(returns[3].stringValue(), "HR");
    }

    @Test
    public void testInitTypeReferenceObjectWithNew() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitTypeReferenceObjectWithNew");
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
        Assert.assertEquals(returns[1].stringValue(), "John from inner function");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 1000.0);
        Assert.assertEquals(returns[3].stringValue(), "HR");
    }

    @Test
    public void testObjectWithChainedTypeReferences() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithChainedTypeReferences");
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
        Assert.assertEquals(returns[1].stringValue(), "John from outer function");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 2500.0);
        Assert.assertEquals(returns[3].stringValue(), "HR");
    }

    @Test
    public void testAbstractObjectFuncWithDefaultVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbstractObjectFuncWithDefaultVal");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Hello Jane");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1800.0);
    }

    @Test
    public void testNonAbstractObjectInclusion() {
        BRunUtil.invoke(compileResult, "testNonAbstractObjectInclusion");
    }

    @Test
    public void testCreatingObjectWithOverriddenFields() {
        BRunUtil.invoke(compileResult, "testCreatingObjectWithOverriddenFields");
    }

    @Test
    public void testTypeReferencedFunctionImplementation() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_func_reference_neg.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test1(string aString, int " +
                                          "anInt) returns (string|error)', found 'function test1(string str, int " +
                                          "anInt)" +
                                          " returns (string|error)'", 53, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'public function test2(string aString)', " +
                                          "found 'function test2(string aString)'", 58, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test3(int anInt, string " +
                                          "aString, string defaultable, int def2)', found 'function test3(string " +
                                          "aString, int anInt, string defaultable, int def2) returns string'", 61, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test4(string aString, " +
                                          "int:Signed16 anInt, Bar... bars) returns string', found 'function test4" +
                                          "(string aString, int anInt, AnotherBar... bars) returns string'",
                                  66, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test5(ON|OFF... status) returns" +
                                          " Bar', found 'function test5(ON|OFF... stat) returns Bar'", 71, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test6([string,ON|OFF]... tup)'," +
                                          " found 'function test6([string,ON|OFF]... tupl)'", 76, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test7() returns ON|OFF', found " +
                                          "'function test7(int x) returns ON|OFF'", 79, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test8(int:Signed16 anInt, Bar.." +
                                          ". bars) returns int:Signed16', found 'function test8(int:Signed16 anInt, " +
                                          "Bar... bars) returns int'",
                                  84, 5);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testInvalidTypeReferenceAcrossModules() {
        CompileResult result = BCompileUtil.compile("test-src/object/ObjectProject", "object_reference_negative");
        int index = 0;

        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, index++,
                                  "incompatible type reference 'abc:Foo': a referenced object cannot have non-public " +
                                          "fields or methods", 20, 6);
        BAssertUtil.validateError(result, index++,
                                  "incompatible type reference 'abc:Bar': a referenced object cannot have non-public " +
                                          "fields or methods", 21, 6);
        BAssertUtil.validateError(result, index,
                                  "incompatible type reference 'abc:Baz': a referenced object cannot have non-public " +
                                          "fields or methods", 22, 6);
    }

    @Test
    public void testCreatingObjectWithOverriddenMethods() {
        BRunUtil.invoke(compileResult, "testCreatingObjectWithOverriddenMethods");
    }
}
