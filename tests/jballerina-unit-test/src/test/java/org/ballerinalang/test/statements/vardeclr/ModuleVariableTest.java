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

    private CompileResult compileResult, compileResultNegative, recordVarCompileResult, recordVarCompileResultNegetive;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/vardeclr/module_tuple_var_decl.bal");
        compileResultNegative = BCompileUtil.compile("test-src/statements/vardeclr/module_tuple_var_decl_negetive.bal");
        recordVarCompileResult = BCompileUtil.compile("test-src/statements/vardeclr/module_record_var_decl.bal");
        recordVarCompileResultNegetive =
                BCompileUtil.compile("test-src/statements/vardeclr/module_record_var_decl_negetive.bal");
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
        assertEquals(compileResultNegative.getErrorCount(), index);
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
        BRunUtil.invoke(recordVarCompileResult, "testBasic");
        BRunUtil.invoke(recordVarCompileResult, "recordVarInRecordVar");
        BRunUtil.invoke(recordVarCompileResult, "tupleVarInRecordVar");
        BRunUtil.invoke(recordVarCompileResult, "testRecordVarWithAnnotations");
        BRunUtil.invoke(recordVarCompileResult, "testVariableForwardReferencing");
//        BRunUtil.invoke(recordVarCompileResult, "testRecordVariableWithRestBP");
    }

    @Test
    public void testModuleLevelRecordVarDeclNegetive() {
        int index = 0;
        validateError(recordVarCompileResultNegetive, index++, "redeclared symbol 'Fname'", 23, 14);
        validateError(recordVarCompileResultNegetive, index++, "redeclared symbol 'Married'", 25, 9);
        validateError(recordVarCompileResultNegetive, index++, "invalid record binding pattern; unknown field 'age' in record type 'Person'", 31, 1);
        validateError(recordVarCompileResultNegetive, index++, "only a simple variable can be marked as 'isolated'", 34, 1);
        validateError(recordVarCompileResultNegetive, index++, "only simple variables are allowed to be configurable", 37, 1);
        assertEquals(recordVarCompileResultNegetive.getErrorCount(), index);
    }

    @Test
    public void testUninitializedModuleLevelRecordVar() {
        // TODO: disallow uninitialized record variables from parser and update this test. Add similar test case for
        // error variable as well.
        CompileResult compileResult =
                BCompileUtil.compile("test-src/statements/vardeclr/uninitialized_module_record_var_decl.bal");
        int index = 0;
        validateError(compileResult, index++, "uninitialized variable 'carId'", 22, 9);
        validateError(compileResult, index++, "uninitialized variable 'carColor'", 22, 22);
        validateError(compileResult, index++, "variable 'carColor' is not initialized", 25, 24);
        assertEquals(compileResult.getErrorCount(), index);
    }
}
