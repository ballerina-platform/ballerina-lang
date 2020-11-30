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
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Class to test module level variable declaration for all binding patterns.
 *
 * @since 2.0
 */
public class ModuleVariableTest {

    private CompileResult compileResult, compileResultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/vardeclr/module_tuple_var_decl.bal");
        compileResultNegative = BCompileUtil.compile("test-src/statements/vardeclr/module_tuple_var_decl_negetive.bal");
    }

    @Test
    public void testBasicModuleLevelTupleVarDecl() {
        BRunUtil.invoke(compileResult, "testBasic");
    }
    
    @Test
    public void testComlexModuleLevelTupleVarDecl() {
        BRunUtil.invoke(compileResult, "testTupleBindingWithRecordsAndObjects");
        BRunUtil.invoke(compileResult, "testTupleBindingPatternWithRestBindingPattern");
        BRunUtil.invoke(compileResult, "testDeclaredWithVar");
        BRunUtil.invoke(compileResult, "testTupleVarWithAnnotations");
        BRunUtil.invoke(compileResult, "testVariableForwardReferencing");
    }

    @Test
    public void testModuleLevelTupleVarDeclNegetive() {
        int index = 0;
        validateError(compileResultNegative, index++, "redeclared symbol 'a'", 19, 23);
        validateError(compileResultNegative, index++, "redeclared symbol 'b'", 20, 7);
        validateError(compileResultNegative, index++, "undefined symbol 'd'", 23, 12);
        validateError(compileResultNegative, index++, "undefined symbol 'd'", 24, 9);
        validateError(compileResultNegative, index++, "only a simple variable can be marked as 'isolated'", 31, 1);
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
}
