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

/**
 * Class to test module level error variable declaration.
 *
 * @since 2.0
 */
public class ModuleErrorVariableTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/vardeclr/module_error_var_decl.bal");
    }

    @Test(dataProvider = "testModuleLevelErrorVarDeclData")
    public void testModuleLevelErrorVarDecl(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] testModuleLevelErrorVarDeclData() {
        return new Object[]{
                "testBasic",
                "testWithErrorCause",
                "testTupleVarInsideErrorVar",
                "testRecordVarInsideErrorVar",
                "testErrorVarInsideErrorVar",
                "testErrorVarWithAnnotations",
                "testVariableForwardReferencing",
                "testErrorVarWithRestVariable",
        };
    }

//    @Test
//    public void testModuleLevelRecordVarDeclNegetive() {
//        CompileResult compileResultNegetive =
//                BCompileUtil.compile("test-src/statements/vardeclr/module_record_var_decl_negetive.bal");
//        int index = 0;
//        validateError(compileResultNegetive, index++, "redeclared symbol 'Fname'", 23, 14);
//        validateError(compileResultNegetive, index++, "redeclared symbol 'Married'", 25, 9);
//        validateError(compileResultNegetive, index++,
//                "invalid record binding pattern; unknown field 'age' in record type 'Person'", 31, 1);
//        validateError(compileResultNegetive, index++,
//                "only a simple variable can be marked as 'isolated'", 34, 1);
//        validateError(compileResultNegetive, index++,
//                "only simple variables are allowed to be configurable", 37, 1);
//        validateError(compileResultNegetive, index++,
//                "annotation 'annot' is not allowed on var", 41, 1);
//        validateError(compileResultNegetive, index++,
//                "missing non-defaultable required record field 'married'", 42, 23);
//        validateError(compileResultNegetive, index++,
//                "incompatible types: expected 'string', found 'other'", 44, 47);
//        assertEquals(compileResultNegetive.getErrorCount(), index);
//    }
//
//    @Test
//    public void testTaintAnalysisWithModuleLevelTupleVar() {
//        CompileResult compileResult = BCompileUtil.compile(
//                "test-src/statements/vardeclr/module_record_var_decl_taint_analysis_negetive.bal");
//        int index = 0;
//        validateError(compileResult, index++, "tainted value passed to global variable 'myA'", 21, 5);
//        assertEquals(compileResult.getErrorCount(), index);
//    }
//
//    @Test
//    public void testModuleLevelTupleVarAnnotationNegetive() {
//        CompileResult compileResult = BCompileUtil.compile(
//                "test-src/statements/vardeclr/module_record_var_decl_annotation_negetive.bal");
//        int index = 0;
//        validateError(compileResult, index++,
//                "annotation 'ballerina/lang.annotations:1.0.0:deprecated' is not allowed on var", 20, 1);
//        assertEquals(compileResult.getErrorCount(), index);
//    }
//
//    @Test
//    public void testUninitializedModuleLevelRecordVar() {
//        // TODO: disallow uninitialized record variables from parser and update this test. Add similar test case for
//        // error variable as well.
//        CompileResult compileResult =
//                BCompileUtil.compile("test-src/statements/vardeclr/uninitialized_module_record_var_decl.bal");
//        int index = 0;
//        validateError(compileResult, index++, "uninitialized variable 'carId'", 22, 9);
//        validateError(compileResult, index++, "uninitialized variable 'carColor'", 22, 22);
//        validateError(compileResult, index++, "variable 'carColor' is not initialized", 25, 24);
//        assertEquals(compileResult.getErrorCount(), index);
//    }
}
