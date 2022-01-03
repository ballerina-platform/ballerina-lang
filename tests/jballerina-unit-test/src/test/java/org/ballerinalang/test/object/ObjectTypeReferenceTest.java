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
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for object reference type in ballerina.
 */
public class ObjectTypeReferenceTest {

    private static final String ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_NON_READ_ONLY_CLASS =
            "object type inclusion cannot be used with a 'readonly class' in a 'class' that is not 'readonly'";
    private static final String ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_OBJECT_TYPEDESC =
            "object type inclusion cannot be used with a 'readonly class' in an object type descriptor";
    private static final String INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS =
            "invalid object type inclusion with an object that has private fields or methods";

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
                                  "invalid cyclic type reference in '[Foo, Foo]'", 52, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[A, B, C, D, A]'", 57, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[C, E, C]'", 57, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[B, C, D, A, B]'", 61, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                "invalid cyclic type reference in '[C, E, C]'", 61, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[C, D, A, B, C]'", 65, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[C, E, C]'", 65, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[C, E, C]'", 70, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[D, A, B, C, D]'", 70, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[C, D, A, B, C]'", 74, 1);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid cyclic type reference in '[E, C, E]'", 74, 1);
        BAssertUtil.validateError(negativeResult, i++,
                "no implementation found for the method 'getName' of class 'Manager2'", 95, 1);
        BAssertUtil.validateError(negativeResult, i++,
                "no implementation found for the method 'getSalary' of class 'Manager2'", 95, 1);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'Q' is not an object", 101, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared type reference 'Person1'", 111, 6);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'Baz'", 119, 6);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'Tar'", 123, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'xyz'", 144, 6);
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
    public void testOutOfOrderObjectTypeReferenceNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object" +
                "/object_out_of_order_inclusion_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "included field 'body' of type 'anydata' cannot " +
                "be overridden by a field of type 'Baz2': expected a subtype of 'anydata'", 24, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testCyclicDependencyReferencesObjectTypeReferenceNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-cyclic-dependency" +
                                                                    "-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[Foo, Foo]'", 18, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[A, B, C, D, A]'", 23, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, E, C]'", 23, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[B, C, D, A, B]'", 27, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, E, C]'", 27, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, D, A, B, C]'", 31, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, E, C]'", 31, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, E, C]'", 36, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[D, A, B, C, D]'", 36, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, D, A, B, C]'", 40, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[E, C, E]'", 40, 1);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testSimpleObjectTypeReferenceSemanticsNegative_2() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-2-semantics" +
                "-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "only type references are allowed as type inclusions",
                18, 6);
        BAssertUtil.validateError(negativeResult, i++, "only type references are allowed as type inclusions", 20, 6);
        BAssertUtil.validateError(negativeResult, i, "unknown type 'YYY'", 29, 6);
    }

    @Test
    public void testInclusionWithQualifiers() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_inclusion_with_qualifiers.bal");
        Assert.assertEquals(result.getErrorCount(), 0);

        BRunUtil.invoke(result, "testObjectConstructorWithReadOnlyReference");
        BRunUtil.invoke(result, "testReadOnlyAndObjectIntersectionInclusion");
    }

    @Test
    public void testInvalidInclusionWithQualifiers() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/object/object_inclusion_with_qualifiers_negative.bal");

        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_NON_READ_ONLY_CLASS,
                                  26, 6);
        BAssertUtil.validateError(negativeResult, i++, ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_NON_READ_ONLY_CLASS,
                                  27, 6);
        BAssertUtil.validateError(negativeResult, i++, ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_OBJECT_TYPEDESC,
                                  33, 6);
        BAssertUtil.validateError(negativeResult, i++, ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_OBJECT_TYPEDESC,
                                  34, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'isolated' qualifier" +
                "(s) in the referencing object", 58, 6);
        BAssertUtil.validateError(negativeResult, i++, ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_NON_READ_ONLY_CLASS,
                                  58, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'client' qualifier(s) " +
                "in the referencing object", 59, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'service' qualifier(s)" +
                " in the referencing object", 60, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'isolated client' " +
                "qualifier(s) in the referencing object", 61, 6);
        BAssertUtil.validateError(negativeResult, i++, "remote qualifier only allowed in client and service objects",
                                  68, 5);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'isolated' qualifier" +
                "(s) in the referencing object", 74, 6);
        BAssertUtil.validateError(negativeResult, i++, ERROR_INVALID_READ_ONLY_CLASS_INCLUSION_IN_OBJECT_TYPEDESC,
                                  74, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'client' qualifier(s) " +
                "in the referencing object", 75, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'service' qualifier(s)" +
                " in the referencing object", 76, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid object type inclusion: missing 'isolated client' " +
                "qualifier(s) in the referencing object", 77, 6);
        BAssertUtil.validateError(negativeResult, i++, "invalid type reference: missing 'isolated' qualifier(s) in " +
                "the referencing object constructor expression", 80, 18);
        BAssertUtil.validateError(negativeResult, i++, "invalid type reference: missing 'client' qualifier(s) in the " +
                "referencing object constructor expression", 84, 24);
        BAssertUtil.validateError(negativeResult, i++, "remote qualifier only allowed in client and service objects",
                                  87, 5);
        BAssertUtil.validateError(negativeResult, i++, "invalid type reference: missing 'isolated client' " +
                "qualifier(s) in the referencing object constructor expression", 93, 21);
        BAssertUtil.validateError(negativeResult, i++, "invalid field in an object constructor expression with a " +
                "'readonly' reference: 'stream<int>' can never be 'readonly'", 106, 5);
        BAssertUtil.validateError(negativeResult, i++, "invalid field in an object constructor expression with a " +
                "'readonly' reference: 'stream<int>' can never be 'readonly'", 119, 9);
        BAssertUtil.validateError(negativeResult, i++, "invalid field in an object constructor expression with a " +
                "'readonly' reference: 'future' can never be 'readonly'", 120, 9);
        BAssertUtil.validateError(negativeResult, i++, "object type inclusion cannot be used with a 'readonly' type " +
                "descriptor in a 'class' that is not 'readonly'", 135, 6);
        BAssertUtil.validateError(negativeResult, i++, "object type inclusion cannot be used with a 'readonly' type " +
                "descriptor in an 'object' type descriptor", 141, 6);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'vals' with " +
                "object type inclusion", 156, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'name' with " +
                "object type inclusion", 157, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'age' with " +
                "object type inclusion", 158, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched function signatures: expected 'function " +
                "foo(float ratio, int months) returns float', found 'public function " +
                "foo(float ratio, int months) returns float'", 162, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'a' with " +
                "object type inclusion", 175, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'b' with " +
                "object type inclusion", 176, 5);
        BAssertUtil.validateError(negativeResult, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 180, 6);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'x' with " +
                "object type inclusion", 181, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'y' with " +
                "object type inclusion", 182, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'l' with " +
                "object type inclusion", 195, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'm' with " +
                "object type inclusion", 196, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'n' with " +
                "object type inclusion", 197, 5);
        BAssertUtil.validateError(negativeResult, i++, "mismatched function signatures: expected 'public function " +
                "foo(float ratio, int months) returns float', found 'private function foo(float ratio, int months) " +
                "returns float'", 199, 5);
        BAssertUtil.validateError(negativeResult, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 209, 6);
        BAssertUtil.validateError(negativeResult, i++, "mismatched visibility qualifiers for field 'q' with " +
                "object type inclusion", 210, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testInvalidInclusionOfObjectWithPrivateMembers() {
        CompileResult result = BCompileUtil.compile(
                "test-src/object/object_inclusion_with_private_members_negative.bal");

        int i = 0;
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 25, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 35, 34);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 45, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 59, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 73, 34);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 86, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 106, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 124, 34);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 142, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 143, 6);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 166, 22);
        BAssertUtil.validateError(result, i++, INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS, 177, 29);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testSimpleObjectTypeReferenceNegative_2() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-2-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'x'", 18, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'y'", 18, 6);
        BAssertUtil.validateError(negativeResult, i++, "uninitialized field 'value'", 33, 6);
        BAssertUtil.validateError(negativeResult, i++, "variable 'value' is not initialized", 35, 56);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
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
                "mismatched function signatures: expected 'function test5(Status... status) returns Bar',"
                        + " found 'function test5(Status... stat) returns Bar'", 71, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test6([string,Status]... tup)'," +
                                          " found 'function test6([string,Status]... tupl)'", 76, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test7() returns Status', found " +
                                          "'function test7(int x) returns Status'", 79, 5);
        BAssertUtil.validateError(result, index++,
                                  "mismatched function signatures: expected 'function test8(int:Signed16 anInt, Bar.." +
                                          ". bars) returns int:Signed16', found 'function test8(int:Signed16 anInt, " +
                                          "Bar... bars) returns int'",
                                  84, 5);
        BAssertUtil.validateError(result, index++, "no implementation found for the method 'toString' of class " +
                "'InvalidReadOnlyClassWithMissingImpl'", 93, 1);
        BAssertUtil.validateError(result, index++, "no implementation found for the method 'toFloat' of class " +
                "'InvalidReadOnlyClassWithMissingImpls'", 101, 1);
        BAssertUtil.validateError(result, index++, "no implementation found for the method 'toString' of class " +
                "'InvalidReadOnlyClassWithMissingImpls'", 101, 1);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testInvalidTypeReferenceAcrossModules() {
        CompileResult result = BCompileUtil.compile("test-src/object/ObjectRefNegativeProject");
        int index = 0;

        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, index++,
                                  "incompatible type reference 'abc:Foo': a referenced type across modules " +
                                          "cannot have non-public fields or methods", 20, 6);
        BAssertUtil.validateError(result, index++,
                                  "incompatible type reference 'abc:Bar': a referenced type across modules " +
                                          "cannot have non-public fields or methods", 21, 6);
        BAssertUtil.validateError(result, index,
                                  "incompatible type reference 'abc:Baz': a referenced type across modules " +
                                          "cannot have non-public fields or methods", 22, 6);
    }

    @Test
    public void testCreatingObjectWithOverriddenMethods() {
        BRunUtil.invoke(compileResult, "testCreatingObjectWithOverriddenMethods");
    }
}
