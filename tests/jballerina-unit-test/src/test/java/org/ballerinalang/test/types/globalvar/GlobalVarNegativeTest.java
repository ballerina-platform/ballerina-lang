/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.globalvar;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Global variable error scenarios.
 */
public class GlobalVarNegativeTest {

    private static final String INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX =
            "cannot call a function or method in the same module before all module-level variables are initialized: ";

    @Test
    public void testGlobalVarNegatives() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/statements/variabledef/global_variable_negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 7);
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "missing non-defaultable required record field 'x'", 22, 12);
        BAssertUtil.validateError(resultNegative, i++, "invalid character ':' in field access expression", 23, 15);
        BAssertUtil.validateError(resultNegative, i++, "missing equal token", 27, 46);
        BAssertUtil.validateError(resultNegative, i++, "missing identifier", 27, 46);
        BAssertUtil.validateError(resultNegative, i++, "missing equal token", 29, 59);
        BAssertUtil.validateError(resultNegative, i++, "missing identifier", 29, 59);
        BAssertUtil.validateError(resultNegative, i++, "invalid cyclic type reference in '[Listener, Listener]'", 31,
                1);
    }

    @Test
    void testGlobalVariableInitNegative() {
        CompileResult result = BCompileUtil.compile("test-src/statements/variabledef/global_variable_init_negative" +
                ".bal");

        int i = 0;
        BAssertUtil.validateError(result, i++, "uninitialized variable 'i'", 17, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 's'", 18, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'a'", 19, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'b'", 20, 1);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 25, 5);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 31, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 39, 13);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 40, 16);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'func1'", 43, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'func2'", 45, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'func3'", 47, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'func4'", 49, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'func5'", 51, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'func6'", 53, 1);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    void testGlobalVariableInitWithInvocationNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/variabledef/global_variable_init_with_invocation_negative.bal");

        int i = 0;
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'i, s, t, u' not initialized", 24, 5);
        BAssertUtil.validateError(result, i++, "variable 'lf' is not initialized", 24, 5);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'i, s, u' not initialized", 26, 5);
        BAssertUtil.validateError(result, i++, "variable 't' is not initialized", 26, 5);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'i, s, t, u' not initialized", 28, 9);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 's, t' not initialized", 30, 5);
        BAssertUtil.validateError(result, i++, "variable 'u' is not initialized", 30, 5);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 's, t, u' not initialized", 32, 9);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'u' not initialized", 34, 5);
        BAssertUtil.validateError(result, i++, "variable 't' is not initialized", 34, 5);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'u' not initialized", 38, 5);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'u' not initialized", 38, 5);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testConfigurableModuleVarDeclNegative() {
        CompileResult result = BCompileUtil.compile
                ("test-src/statements/variabledef/configurable_global_var_decl_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "configurable variable must be initialized or be marked as required",
                18, 19);
        BAssertUtil.validateError(result, i++, "configurable variable cannot be declared with var", 20, 1);
        BAssertUtil.validateError(result, i++, "invalid type for configurable variable: expected a subtype" +
                " of 'anydata'", 22, 14);
        BAssertUtil.validateError(result, i++, "missing close brace token", 27, 1);
        BAssertUtil.validateError(result, i++, "invalid token '}'", 29, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '()'", 31, 1);
        BAssertUtil.validateError(result, i++, "only simple variables are allowed to be configurable", 34, 1);
        BAssertUtil.validateError(result, i++, "'final' qualifier not allowed: configurable variables are " +
                "implicitly final", 37, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(Person1 & " +
                "readonly)'\n\t" +
                "record field type '()' of field 'person1.nilField' is not supported", 71, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(Person2 & " +
                "readonly)'\n\t" +
                "union member type '()' is not supported", 72, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(Person3 & " +
                "readonly)'\n\t" +
                "array element type '()' is not supported", 73, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(Person4 & " +
                "readonly)'\n\t" +
                "record field type '()' of field 'person4.person.nilField' is not supported", 74, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(Person5 & " +
                "readonly)'\n\t" +
                "record field type '()' of field 'person5.field1' is not supported\n\t" +
                "record field type '()' of field 'person5.field2' is not supported", 75, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(table<map<()>> " +
                "& readonly)'\n\t" +
                "map constraint type '()' is not supported", 78, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(table<Person1> & " +
                "readonly)'\n\t" +
                "record field type '()' of field 'tableVar2.nilField' is not supported", 79, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(table<()> & " +
                "readonly)'\n\t" +
                "table constraint type '()' is not supported", 80, 1);
        BAssertUtil.validateError(result, i++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found '()'", 80, 20);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(()[] & " +
                "readonly)'\n\t" +
                "array element type '()' is not supported", 83, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(map<()> & " +
                "readonly)'\n\t" +
                "map constraint type '()' is not supported", 86, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '(string? & " +
                "readonly)'\n\t" +
                "union member type '()' is not supported", 89, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '()" +
                "'", 90, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '([int,string,()]" +
                " & readonly)'\n\t" +
                "tuple element type '()' is not supported", 93, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for '()'", 101, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for " +
                "'(()[] & readonly)'\n\t" +
                "array element type '()' is not supported", 102, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for " +
                "'(string? & readonly)'\n\t" +
                "union member type '()' is not supported", 103, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for " +
                "'(map<()> & readonly)'\n\t" +
                "map constraint type '()' is not supported", 104, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for" +
                " '(Person6 & readonly)'\n\t" +
                "record field type '()' of field 'nilRecord1.field1' is not supported", 105, 1);
        BAssertUtil.validateError(result, i++, "configurable variable currently not supported for " +
                "'(table<map<()>> & readonly)'\n\t" +
                "map constraint type '()' is not supported", 106, 1);

        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testConfigurableImplicitFinal() {
        CompileResult result = BCompileUtil.compile
                ("test-src/statements/variabledef/configurable_global_var_decl_negative_02.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "cannot assign a value to final 'discountRate'",
                21, 5);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testConfigurableImplicitReadOnly() {
        CompileResult result = BCompileUtil.compile
                ("test-src/statements/variabledef/configurable_global_var_decl_negative_03.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'int[] & readonly', found 'int[]'",
                20, 27);
        BAssertUtil.validateError(result, i++, "incompatible types: expected '(Foo & readonly)', found 'Foo'",
                28, 22);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testGlobalVariableInitWithOnFailNegative() {
        CompileResult result = BCompileUtil.compile
                ("test-src/statements/variabledef/global_variable_init_with_onfail_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "uninitialized variable 'j'", 18, 1);
        BAssertUtil.validateError(result, i++, "uninitialized variable 'k'", 19, 1);
        BAssertUtil.validateError(result, i++, INVALID_FUNC_OR_METHOD_CALL_WITH_UNINITIALIZED_VARS_PREFIX +
                "variable(s) 'j, k' not initialized", 31, 5);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
