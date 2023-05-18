/*
*  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.object;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for object-constructor-expr types in ballerina.
 */
@Test
public class ObjectConstructorTest {

    private CompileResult compiledConstructedObjects, closures, annotations, multiLevelClosures;
    private static String path = "test-src/expressions/object/";

    @BeforeClass
    public void setup() {
        compiledConstructedObjects = BCompileUtil.compile(path + "object_constructor_expression.bal");
        closures = BCompileUtil.compile(path + "object_closures.bal");
        multiLevelClosures = BCompileUtil.compile(path + "object_multilevel_closures.bal");
        annotations = BCompileUtil.compile(path + "object_closures_annotations.bal");
    }

    @DataProvider(name = "ObjectCtorTestFunctionList")
    public Object[][] objectCtorTestFunctionList() {
        return new Object[][]{
                {"testObjectCreationViaObjectConstructor"},
                {"testObjectConstructorAnnotationAttachment"},
                {"testObjectConstructorObjectFunctionInvocation"},
                {"testObjectConstructorIncludedMethod"},
                {"testObjectConstructorWithDistinctExpectedType"},
                {"testObjectConstructorWithDistinctTypeReference"},
                {"testObjectConstructorWithDistinctTypeReferenceVar"},
                {"testObjectConstructorWithDefiniteTypeAndWithoutReference"},
                {"testObjectConstructorExprWithReadOnlyCET"},
                {"testMultipleVarAssignments"},
                {"testLocalVariablesAsFieldDefaults"},
                {"testModuleLevelObjectCtrWithModuleLevelVariableAsFieldDefaults"}
        };
    }

    @DataProvider(name = "ClosureTestFunctionList")
    public Object[][] closureTestFunctionList() {
        return new Object[][]{
                {"testClosureVariableAsFieldValue"},
                {"testClosureVariableAsFieldValueUsedInAttachedFunctions"},
                {"testClosureVariableAsFieldValueWithExpression"},
                {"testClosureVariableUsedInsideAttachedMethodBodyAndField"},
                {"testClosureVariableUsedInsideAttachedMethodBodyOnly"},
                {"testClosureVariableUsedInsideWithDifferentType"},
                {"testClosureButAsArgument"},
                {"testAttachedMethodClosuresMapFromFunctionBlock"},
                {"testFunctionPointerAsFieldValue"},
                {"testClosuresWithObjectConstrExpr"},
                {"testClosuresWithObjectConstrExprAsFunctionDefaultParam"},
                {"testClosuresWithObjectConstrExprInAnonFunc"},
                {"testClosuresWithObjectConstrExprInObjectFunc"},
                {"testClosuresWithObjectConstrExprInVarAssignment"},
                {"testClosuresWithObjectConstrExprInReturnStmt"},
                {"testClosuresWithClientObjectConstrExpr"},
                {"testClosuresWithObjectConstrExprInClientObjectConstrExpr"},
                {"testClosuresWithServiceObjectConstrExpr"},
                {"testClosuresWithObjectConstrExprsInObjectConstrExpr"},
                {"testClosuresWithObjectConstrExprAsArrayMember"},
                {"testClosuresWithObjectConstrExprInEqaulityExpr"}
        };
    }

    @Test(dataProvider = "ObjectCtorTestFunctionList")
    public void testCompiledConstructedObjects(String funcName) {
        BRunUtil.invoke(compiledConstructedObjects, funcName);
    }

    @Test(dataProvider = "ClosureTestFunctionList")
    public void testClosureSupportForObjectCtor(String funcName) {
        BRunUtil.invoke(closures, funcName);
    }

    @Test(dataProvider = "dataToTestClosuresWithObjectConstrExprWithAnnots")
    public void testClosureSupportForObjectCtorAnnotations(String funcName) {
        BRunUtil.invoke(annotations, funcName);
    }

    @DataProvider
    public Object[] dataToTestClosuresWithObjectConstrExprWithAnnots() {
        return new Object[]{
                "testAnnotations",
                "testObjectConstructorAnnotationAttachment",
                "testAnnotsOfServiceObjectConstructorInReturnStmt",
                "testClosuresWithObjectConstrExprWithAnnots",
                "testClosuresWithObjectConstrExprWithAnnotsAsFunctionDefaultParam",
                "testClosuresWithObjectConstrExprWithAnnotsInAnonFunc",
                "testClosuresWithObjectConstrExprWithAnnotsInObjectFunc",
                "testClosuresWithObjectConstrExprWithAnnotsInVarAssignment",
                "testClosuresWithObjectConstrExprWithAnnotsInReturnStmt",
                "testClosuresWithClientObjectConstrExprWithAnnots",
                "testClosuresWithObjectConstrExprWithAnnotsInClientObjectConstrExpr",
                "testClosuresWithServiceObjectConstrExprWithAnnots",
                "testClosuresWithObjectConstrExprWithAnnotsInObjectConstrExprWithAnnots",
                "testClosuresWithObjectConstrExprWithAnnotsAsArrayMember"
        };
    }

    @Test
    public void testUnusedVariableWarnings() {
        Assert.assertEquals(compiledConstructedObjects.getWarnCount(), 0);
        Assert.assertEquals(closures.getWarnCount(), 0);
        Assert.assertEquals(annotations.getWarnCount(), 0);
        Assert.assertEquals(multiLevelClosures.getWarnCount(), 0);
    }

    @Test
    public void testObjectConstructorWithReferredIntersectionType() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectConstructorWithReferredIntersectionType");
    }

    @Test
    public void testObjectConstructorNegative() {

        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_expression_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: 'SampleRec' is not an object", 19, 39);
        validateError(negativeResult, index++, "remote method has a visibility qualifier", 22, 5);
        validateError(negativeResult, index++,
                "remote qualifier only allowed in client and service objects", 22, 13);
        validateError(negativeResult, index++, "object constructor 'init' method cannot have parameters",
                26, 5);
        validateError(negativeResult, index++, "object initializer function can not be declared as " +
                "private", 30, 5);
        validateError(negativeResult, index++, "invalid token 'public'", 34, 22);
        validateError(negativeResult, index++, "type inclusions are not allowed in object constructor",
                39, 5);
        validateError(negativeResult, index++, "invalid usage of 'object constructor expression' with " +
                        "type 'any'", 42, 9);
        validateError(negativeResult, index++, "invalid usage of 'object constructor expression' with " +
                "type '(DistinctFooA|DistinctFoo)'", 53, 47);
        validateError(negativeResult, index++, "incompatible types: expected 'string[] & readonly', found 'string[]'",
                      84, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'ReadOnlyClass', " +
                              "found 'isolated object { final int a; final (string[] & readonly) s; } & readonly'",
                      87, 24);
        validateError(negativeResult, index++, "incompatible types: expected 'string[] & readonly', found 'string[]'",
                      89, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'string[] & readonly', found 'string[]'",
                      94, 22);
        validateError(negativeResult, index++, "incompatible types: expected '()', found 'stream<string>'",
                      95, 29);
        validateError(negativeResult, index++, "incompatible types: expected 'string[] & readonly', found 'string[]'",
                      104, 22);
        validateError(negativeResult, index++, "incompatible types: expected '()', found 'stream<string>'",
                      105, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'string[] & readonly', found 'string[]'",
                      113, 22);
        validateError(negativeResult, index++, "incompatible types: expected '()', found 'stream<string>'",
                      117, 22);
        validateError(negativeResult, index++,
                "no implementation found for the method 'onMessage' of object constructor " +
                        "'object { function onMessage () returns (); }'", 127, 14);
        validateError(negativeResult, index++, "incompatible types: expected 'any & readonly', found 'stream<int>'",
                      140, 17);
        validateError(negativeResult, index++, "annotation not attached to a construct", 153, 14);
        validateError(negativeResult, index++, "missing object constructor expression", 153, 14);
        validateError(negativeResult, index++, "missing semicolon token", 154, 1);
        validateError(negativeResult, index++, "annotation not attached to a construct", 154, 14);
        validateError(negativeResult, index++, "missing object constructor expression", 154, 14);
        validateError(negativeResult, index++, "annotation not attached to a construct", 154, 18);
        validateError(negativeResult, index++, "missing semicolon token", 155, 1);
        validateError(negativeResult, index++, "incompatible types: expected 'object { resource " +
                "function get name() returns (); }', found 'isolated object { }'", 159, 7);
        validateError(negativeResult, index++, "incompatible types: expected 'object { resource " +
                "function get name() returns (); }', found 'isolated object { }'", 163, 7);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @Test
    public void testMultilevelUnsupportedClosureVarScenarios() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_closure_unsupported_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "closure variable 'i' : closures not yet supported for object " +
                        "constructors which are fields", 18, 29);
        validateError(negativeResult, index++, "closure variable 'i' : closures not yet supported for object " +
                "constructors which are fields", 49, 25);
        validateError(negativeResult, index++, "closure variable 'a1' : closures not yet supported for object " +
                "constructors which are fields", 72, 22);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @Test
    public void testRedeclaredSymbolsScenarios() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_redeclared_symbols_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "redeclared symbol 'age'", 7, 36);
        validateError(negativeResult, index++, "redeclared symbol 'age'", 11, 46);
        validateError(negativeResult, index++, "redeclared symbol 'age'", 12, 17);
        validateError(negativeResult, index++, "redeclared symbol 'age'", 17, 17);
        validateError(negativeResult, index++, "incompatible types: expected 'object { public function getSum " +
             "(int) returns (int); }', found 'isolated object { public function getSum (other) returns (int); public " +
             "function getAge (int,other) returns (int); public function getAgeOf () returns (int); }'", 23, 12);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @DataProvider(name = "MultiLevelClosureTestFunctionList")
    public Object[][] multiLevelClosureTestFunctionList() {
        return new Object[][]{
                {"closureArrayPush"},
        };
    }

    @Test(dataProvider = "MultiLevelClosureTestFunctionList")
    public void testMultiLevelClosures(String funcName) {
        BRunUtil.invoke(multiLevelClosures, funcName);
    }

    @Test
    public void testInvalidFieldsInObjectCtr() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/object/object_constructor_fields_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined symbol 'x'", 22, 17);
        BAssertUtil.validateError(result, i++, "undefined symbol 'x'", 33, 13);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compiledConstructedObjects = null;
        closures = null;
        annotations = null;
        multiLevelClosures = null;
    }
}
