/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.isolation;

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
 * Test cases related to isolation analysis.
 *
 * @since 2.0.0
 */
public class IsolationAnalysisTest {

    private static final String INVALID_MUTABLE_STORAGE_ACCESS_ERROR =
            "invalid access of mutable storage in an 'isolated' function";
    private static final String INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR =
            "invalid invocation of a non-isolated function in an 'isolated' function";
    private static final String INVALID_NON_ISOLATED_INIT_EXPR_IN_ISOLATED_FUNC_ERROR =
            "invalid non-isolated initialization expression in an 'isolated' function";

    private static final String INVALID_MUTABLE_STORAGE_ACCESS_IN_RECORD_FIELD_DEFAULT =
            "invalid access of mutable storage in the default value of a record field";
    private static final String INVALID_NON_ISOLATED_FUNCTION_CALL_IN_RECORD_FIELD_DEFAULT =
            "invalid invocation of a non-isolated function in the default value of a record field";
    private static final String INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_RECORD_FIELD_DEFAULT =
            "invalid non-isolated initialization expression in the default value of a record field";

    private static final String INVALID_MUTABLE_STORAGE_ACCESS_IN_OBJECT_FIELD_DEFAULT =
            "invalid access of mutable storage in the initializer of an object field";
    private static final String INVALID_NON_ISOLATED_FUNCTION_CALL_IN_OBJECT_FIELD_DEFAULT =
            "invalid invocation of a non-isolated function in the initializer of an object field";
    private static final String INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_OBJECT_FIELD_DEFAULT =
            "invalid non-isolated initialization expression in the initializer of an object field";

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/isolation-analysis/isolation_analysis.bal");
    }

    @Test(dataProvider = "isolatedFunctionTests")
    public void testIsolatedFunctions(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "isolatedFunctionTests")
    public Object[] isolatedFunctionTests() {
        return new Object[]{
                "testIsolatedFunctionWithOnlyLocalVars",
                "testIsolatedFunctionWithLocalVarsAndParams",
                "testIsolatedFunctionAccessingImmutableGlobalStorage",
                "testIsolatedObjectMethods",
                "testNonIsolatedMethodAsIsolatedMethodRuntimeNegative",
                "testIsolatedFunctionPointerInvocation",
                "testIsolatedFunctionAsIsolatedFunctionRuntime",
                "testIsolatedFunctionAsIsolatedFunctionRuntimeNegative",
                "testIsolatedArrowFunctions",
                "testConstantRefsInIsolatedFunctions",
                "testIsolatedClosuresAsRecordDefaultValues",
                "testIsolatedObjectFieldInitializers",
                "testIsolationAnalysisWithRemoteMethods",
                "testIsolatedFunctionWithDefaultableParams",
                "testAccessingFinalIsolatedObjectInIsolatedFunction",
                "testIsolationOfBoundMethods",
                "testFinalReadOnlyServiceAccessInIsolatedFunction"
        };
    }

    @Test
    public void testAccessingImmutableModuleLevelDefinitionsInIsolatedContexts() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/immutable_module_def_access_in_isolated_contexts.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        Assert.assertEquals(result.getWarnCount(), 0);
    }

    @Test
    public void testAnonIsolatedFuncAccessingImplicitlyFinalVars() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/implicitly_final_var_access_in_anon_isolated_functions.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        Assert.assertEquals(result.getWarnCount(), 0);
    }

    @Test
    public void testIsolatedFunctionsSemanticNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/isolation-analysis/isolation_analysis_semantic_negative.bal");

        int i = 0;
        validateError(result, i++, "incompatible types: expected 'Qux', found 'object { int i; function qux () " +
                "returns (int); }'", 37, 13);
        validateError(result, i++, "incompatible types: expected 'IsolatedFunction', " +
                "found 'function () returns (int)'", 44, 24);
        validateError(result, i++, "incompatible types: expected 'isolated function (int) returns (int)', found " +
                "'function (int) returns (int)'", 66, 51);
        validateError(result, i++, "incompatible types: expected 'isolated function () returns (int)', found " +
                "'function () returns (int)'", 69, 48);
        validateError(result, i++, "incompatible types: expected 'isolated function () returns (int)', found " +
                "'function () returns (int)'", 72, 48);
        validateError(result, i++, "incompatible types: expected 'isolated function (int) returns (int)', found " +
                "'(function (int) returns (int)|isolated function (int) returns (int))'", 75, 57);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedFunctionsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/isolation-analysis/isolation_analysis_negative.bal");

        int i = 0;
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 30, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 31, 19);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 33, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 34, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 34, 19);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 36, 12);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 36, 14);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 40, 5);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 41, 5);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 47, 5);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 48, 5);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 50, 5);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 50, 7);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 55, 13);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 68, 13);
        validateError(result, i++, "worker declaration not allowed in an 'isolated' function", 74, 12);
        validateError(result, i++, "async invocation not allowed in an 'isolated' function", 80, 22);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 94, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 101, 22);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 105, 25);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 124, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 129, 28);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 133, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 139, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 144, 28);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 148, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 155, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 156, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 156, 23);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 156, 30);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 157, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 157, 27);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 158, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 158, 23);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 158, 29);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 159, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 159, 27);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 159, 30);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 159, 39);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 166, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 166, 29);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 167, 16);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 172, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 172, 33);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 173, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 180, 34);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 183, 34);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 186, 56);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 191, 5);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 192, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 192, 20);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 193, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 193, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 193, 23);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 193, 26);
        validateError(result, i++, "fork statement not allowed in an 'isolated' function", 208, 5);
        validateError(result, i++, "worker declaration not allowed in an 'isolated' function", 209, 16);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 218, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 223, 81);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 223, 94);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_ERROR, 227, 45);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 227, 73);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 253, 27);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 254, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 271, 14);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 272, 14);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 273, 14);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 277, 52);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 282, 10);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 282, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 283, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 287, 85);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 290, 7);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 291, 14);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 299, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 300, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 305, 51);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 308, 54);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 311, 70);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_ERROR, 313, 76);
        Assert.assertEquals(result.getErrorCount(), i);
        Assert.assertEquals(result.getWarnCount(), 0);
    }

    @Test
    public void testNonIsolatedRecordFieldDefaultsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolated_record_field_default_negative.bal");
        int i = 0;
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_RECORD_FIELD_DEFAULT, 22, 13);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_IN_RECORD_FIELD_DEFAULT, 23, 13);
        validateError(result, i++, INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_RECORD_FIELD_DEFAULT, 24, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_RECORD_FIELD_DEFAULT, 31, 17);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_IN_RECORD_FIELD_DEFAULT, 32, 20);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_IN_RECORD_FIELD_DEFAULT, 33, 20);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_RECORD_FIELD_DEFAULT, 33, 24);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_OBJECT_FIELD_DEFAULT, 39, 25);
        Assert.assertEquals(result.getErrorCount(), i);

    }

    @Test
    public void testNonIsolatedObjectFieldDefaultsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolated_object_field_default_negative.bal");
        int i = 0;
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_OBJECT_FIELD_DEFAULT, 20, 13);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_OBJECT_FIELD_DEFAULT, 24, 13);
        validateError(result, i++, INVALID_NON_ISOLATED_INIT_EXPR_IN_ISOLATED_FUNC_ERROR, 44, 14);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_OBJECT_FIELD_DEFAULT, 47, 17);
        validateError(result, i++, INVALID_MUTABLE_STORAGE_ACCESS_IN_OBJECT_FIELD_DEFAULT, 51, 17);
        validateError(result, i++, INVALID_NON_ISOLATED_INIT_EXPR_IN_ISOLATED_FUNC_ERROR, 58, 15);
        validateError(result, i++, INVALID_NON_ISOLATED_FUNCTION_CALL_IN_OBJECT_FIELD_DEFAULT, 68, 14);
        validateError(result, i++, INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_OBJECT_FIELD_DEFAULT, 74, 12);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
