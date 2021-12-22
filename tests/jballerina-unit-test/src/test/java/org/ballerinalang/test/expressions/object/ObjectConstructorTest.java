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
//                {"testFunctionPointerAsFieldValue"},
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

    @Test
    public void testClosureSupportForObjectCtorAnnotations() {
        BRunUtil.invoke(annotations, "testAnnotations");
        BRunUtil.invoke(annotations, "testObjectConstructorAnnotationAttachment");
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
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @Test
    public void testRedeclaredSymbolsScenarios() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_redeclared_symbols_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "redeclared symbol 'age'", 7, 32);
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

    @AfterClass
    public void tearDown() {
        compiledConstructedObjects = null;
        closures = null;
        annotations = null;
        multiLevelClosures = null;
    }
}
