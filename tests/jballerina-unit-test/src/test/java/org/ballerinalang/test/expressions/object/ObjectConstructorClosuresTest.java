/*
 * Copyright (c) (2022), WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
public class ObjectConstructorClosuresTest {

    private CompileResult closures, annotations, multiLevelClosures, edgeCases;
    private CompileResult closures_set1;
    private CompileResult closures_set2;
    private CompileResult closures_set3;
    private CompileResult closures_set4;

    @BeforeClass
    public void setup() {
//        closures = BCompileUtil.compile("test-src/expressions/object/object_closures.bal");
        closures_set1 = BCompileUtil.compile("test-src/expressions/object/object_closures_set1.bal");
        closures_set2 = BCompileUtil.compile("test-src/expressions/object/object_closures_set2.bal");
        closures_set3 = BCompileUtil.compile("test-src/expressions/object/object_closures_set3.bal");
        closures_set4 = BCompileUtil.compile("test-src/expressions/object/object_closures_set4.bal");
//        multiLevelClosures = BCompileUtil.compile("test-src/expressions/object/object_multilevel_closures.bal");
//        annotations = BCompileUtil.compile("test-src/expressions/object/object_closures_annotations.bal");
//        edgeCases = BCompileUtil.compile("test-src/expressions/object/edge_cases.bal");
    }

    @AfterClass
    public void tearDown() {
        closures = null;
        annotations = null;
        multiLevelClosures = null;
        edgeCases = null;
        closures_set1 = null;
        closures_set2 = null;
        closures_set3 = null;
        closures_set4 = null;
    }

    @Test
    public void testClosuresInsideLambdaFunctionInObjectCtorExpressionsAttachedFunction() {
        BRunUtil.invoke(closures_set1, "testAttachedFunction");
        BRunUtil.invoke(closures_set1, "testAttachedFunctionWithMultipleArgs");
        BRunUtil.invoke(closures_set1, "testAttachedFunctionReturningClosureWithArgs");
    }

    @Test
    public void testClosuresInsideArrowFunctionInObjectCtorExpressionsAttachedFunction() {
        BRunUtil.invoke(closures_set2, "testAttachedFunction");
        BRunUtil.invoke(closures_set2, "testAttachedFunctionWithArgs");
        BRunUtil.invoke(closures_set2, "testAttachedFunctionWithMultipleArgs");
        BRunUtil.invoke(closures_set2, "testAttachedFunctionReturningClosureWithArgs");
    }

    @Test
    public void testClosuresAsObjectCtorExpressionFields() {
        BRunUtil.invoke(closures_set3, "testFields");
        BRunUtil.invoke(closures_set3, "testMultipleFields");
        BRunUtil.invoke(closures_set3, "testMultipleFieldsAndArg");
    }

    @Test
    public void testClosuresAsObjectCtorExpressionFieldsAndArgs() {
        BRunUtil.invoke(closures_set4, "testFieldsAndArgs");
        BRunUtil.invoke(closures_set4, "testMultipleFields");
//        BRunUtil.invoke(closures_set4, "testMultipleExpressionFieldsAndArg");
    }

//
//    @DataProvider(name = "ClosureTestFunctionList")
//    public Object[][] closureTestFunctionList() {
//        return new Object[][]{
//                {"testClosureVariableAsFieldValue"},
//                {"testClosureVariableAsFieldValueUsedInAttachedFunctions"},
//                {"testClosureVariableAsFieldValueWithExpression"},
//                {"testClosureVariableUsedInsideAttachedMethodBodyAndField"},
//                {"testClosureVariableUsedInsideAttachedMethodBodyOnly"},
//                {"testClosureVariableUsedInsideWithDifferentType"},
//                {"testClosureButAsArgument"},
//                {"testAttachedMethodClosuresMapFromFunctionBlock"},
//                {"testFunctionPointerAsFieldValue"},
//                {"testClosuresWithObjectConstrExpr"},
//                {"testClosuresWithObjectConstrExprAsFunctionDefaultParam"},
//                {"testClosuresWithObjectConstrExprInAnonFunc"},
//                {"testClosuresWithObjectConstrExprInObjectFunc"},
//                {"testClosuresWithObjectConstrExprInVarAssignment"},
//                {"testClosuresWithObjectConstrExprInReturnStmt"},
//                {"testClosuresWithClientObjectConstrExpr"},
//                {"testClosuresWithObjectConstrExprInClientObjectConstrExpr"},
//                {"testClosuresWithServiceObjectConstrExpr"},
//                {"testClosuresWithObjectConstrExprsInObjectConstrExpr"},
//                {"testClosuresWithObjectConstrExprAsArrayMember"},
//                {"testClosuresWithObjectConstrExprInEqaulityExpr"}
//        };
//    }
//

//    @Test
//    public void testArrowExprInsideOCE() {
//        BRunUtil.invoke(edgeCases, "testArrowExprInsideOCE");
//    }
//
//    @Test(dataProvider = "ClosureTestFunctionList")
//    public void testClosureSupportForObjectCtor(String funcName) {
//        BRunUtil.invoke(closures, funcName);
//    }
//
//    @Test(dataProvider = "dataToTestClosuresWithObjectConstrExprWithAnnotations")
//    public void testClosureSupportForObjectCtorAnnotations(String funcName) {
//        BRunUtil.invoke(annotations, funcName);
//    }
//
//    @DataProvider
//    public Object[] dataToTestClosuresWithObjectConstrExprWithAnnotations() {
//        return new Object[]{
//                "testAnnotations",
//                "testObjectConstructorAnnotationAttachment",
//                "testAnnotsOfServiceObjectConstructorInReturnStmt",
//                "testClosuresWithObjectConstrExprWithAnnots",
//                "testClosuresWithObjectConstrExprWithAnnotsAsFunctionDefaultParam",
//                "testClosuresWithObjectConstrExprWithAnnotsInAnonFunc",
//                "testClosuresWithObjectConstrExprWithAnnotsInObjectFunc",
//                "testClosuresWithObjectConstrExprWithAnnotsInVarAssignment",
//                "testClosuresWithObjectConstrExprWithAnnotsInReturnStmt",
//                "testClosuresWithClientObjectConstrExprWithAnnots",
//                "testClosuresWithObjectConstrExprWithAnnotsInClientObjectConstrExpr",
//                "testClosuresWithServiceObjectConstrExprWithAnnots",
//                "testClosuresWithObjectConstrExprWithAnnotsInObjectConstrExprWithAnnots",
//                "testClosuresWithObjectConstrExprWithAnnotsAsArrayMember"
//        };
//    }
//
//    @Test
//    public void testUnusedVariableWarnings() {
//        Assert.assertEquals(closures.getWarnCount(), 0);
//        Assert.assertEquals(annotations.getWarnCount(), 0);
//        Assert.assertEquals(multiLevelClosures.getWarnCount(), 0);
//    }
//
//    @Test
//    public void testMultilevelUnsupportedClosureVarScenarios() {
//        CompileResult negativeResult = BCompileUtil.compile(
//                "test-src/expressions/object/object_constructor_closure_unsupported_negative.bal");
//        int index = 0;
//        validateError(negativeResult, index++, "closure variable 'i' : closures not yet supported for object " +
//                        "constructors which are fields", 18, 29);
//        validateError(negativeResult, index++, "closure variable 'i' : closures not yet supported for object " +
//                "constructors which are fields", 49, 25);
//        validateError(negativeResult, index++, "closure variable 'a1' : closures not yet supported for object " +
//                "constructors which are fields", 72, 22);
//        Assert.assertEquals(negativeResult.getErrorCount(), index);
//    }
//
//
//    @DataProvider(name = "MultiLevelClosureTestFunctionList")
//    public Object[][] multiLevelClosureTestFunctionList() {
//        return new Object[][]{
//                {"closureArrayPush"},
//        };
//    }
//
//    @Test(dataProvider = "MultiLevelClosureTestFunctionList")
//    public void testMultiLevelClosures(String funcName) {
//        BRunUtil.invoke(multiLevelClosures, funcName);
//    }

}
