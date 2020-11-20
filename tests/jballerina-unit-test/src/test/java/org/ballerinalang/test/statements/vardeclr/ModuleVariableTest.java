/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.vardeclr;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * Class to test module level variable declaration for all binding patterns.
 *
 * @since 2.0
 */
public class ModuleVariableTest {

    private CompileResult compileResult, compileResultNegative, recordVarCompileResult;

    @BeforeClass
    public void setup() {
//        compileResult = BCompileUtil.compile("test-src/statements/vardeclr/module_tuple_var_decl.bal");
//        compileResultNegative = BCompileUtil.compile("test-src/statements/vardeclr/module_tuple_var_decl_negetive.bal");
        recordVarCompileResult = BCompileUtil.compile("test-src/statements/vardeclr/module_record_var_decl.bal");
    }

    @Test
    public void testBasicModuleLevelTupleVarDecl() {
        BRunUtil.invoke(compileResult, "testBasic");
    }
    
    @Test(dataProvider = "complexModuleLevelTupleVarDeclData")
    public void testComplexModuleLevelTupleVarDecl(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] complexModuleLevelTupleVarDeclData() {
        return new Object[]{
                "testTupleBindingWithRecordsAndObjects",
                "testTupleBindingPatternWithRestBindingPattern",
                "testDeclaredWithVar",
                "testTupleVarWithAnnotations",
                "testVariableForwardReferencing",
                "testVariableDeclaredInTupleAsAnnotationValue"
        };
    }

    @Test
    public void testModuleLevelTupleVarDeclNegetive() {
        int index = 0;
        validateError(compileResultNegative, index++, "redeclared symbol 'a'", 19, 23);
        validateError(compileResultNegative, index++, "redeclared symbol 'b'", 20, 7);
        validateError(compileResultNegative, index++, "undefined symbol 'd'", 23, 12);
        validateError(compileResultNegative, index++, "undefined symbol 'd'", 24, 9);
        validateError(compileResultNegative, index++, "only a simple variable can be marked as 'isolated'", 31, 1);
        validateError(compileResultNegative, index++, "annotation 'annot' is not allowed on var", 35, 1);
        validateError(compileResultNegative, index++, "incompatible types: expected 'int', found 'other'", 38, 25);
        validateError(compileResultNegative, index++, "redeclared symbol 'n'", 40, 9);
        assertEquals(compileResultNegative.getErrorCount(), index);
    }

    @Test
    public void testTaintAnalysisWithModuleLevelTupleVar() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/statements/vardeclr/module_tuple_var_decl_taint_analysis_negetive.bal");
        int index = 0;
        validateError(compileResult, index++, "tainted value passed to global variable 'p'", 21, 5);
        assertEquals(compileResult.getErrorCount(), index);
    }

    @Test
    public void testModuleLevelTupleVarAnnotationNegetive() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/statements/vardeclr/module_tuple_var_decl_annotation_negetive.bal");
        int index = 0;
        validateError(compileResult, index++,
                "annotation 'ballerina/lang.annotations:1.0.0:deprecated' is not allowed on var", 20, 1);
        assertEquals(compileResult.getErrorCount(), index);
    }

    @Test
    public void testUninitializedModuleLevelTupleVar() {
        // TODO: disallow uninitialized tuple variables from parser and update this test
        CompileResult compileResult =
                BCompileUtil.compile("test-src/statements/vardeclr/uninitialized_module_tuple_var_decl.bal");
        int index = 0;
        validateError(compileResult, index++, "uninitialized variable 'a'", 17, 13);
        validateError(compileResult, index++, "uninitialized variable 'b'", 17, 16);
        validateError(compileResult, index++, "variable 'a' is not initialized", 20, 13);
        assertEquals(compileResult.getErrorCount(), index);
    }

    @Test
    public void testModuleLevelRecordVarDecl() {

        BValue[] returns = BRunUtil.invoke(recordVarCompileResult, "testBasic");
        int index = 0;
        assertEquals(returns[index++].stringValue(), "Jhone");
        assertTrue(((BBoolean) returns[index++]).booleanValue());
        assertEquals(returns.length, index++);

        returns = BRunUtil.invoke(recordVarCompileResult, "recordVarInRecordVar");
        index = 0;
        assertEquals(returns[index++].stringValue(), "Peter");
        assertEquals(((BInteger) returns[index++]).intValue(), 29);
        assertEquals(returns[index++].stringValue(), "Y");
        assertTrue(((BBoolean) returns[index++]).booleanValue());
        assertEquals(returns.length, index++);

        returns = BRunUtil.invoke(recordVarCompileResult, "tupleVarInRecordVar");
        index = 0;
        assertEquals(returns[index++].stringValue(), "Mac");
        assertEquals(((BInteger) returns[index++]).intValue(), 21);
        assertEquals(returns[index++].stringValue(), "Y");
        assertFalse(((BBoolean) returns[index++]).booleanValue());
        assertEquals(returns.length, index++);
    }
}
