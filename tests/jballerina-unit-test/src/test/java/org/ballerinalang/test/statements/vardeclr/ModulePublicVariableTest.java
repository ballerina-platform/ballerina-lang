/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Class to module level public variable declaration.
 *
 * @since 2.0
 */
public class ModulePublicVariableTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/vardeclr/access-project");
    }
    
    @Test(dataProvider = "modulePublicVariableAccessData")
    public void testModulePublicVariableAccess(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] modulePublicVariableAccessData() {
        return new Object[]{
                "testDefaultVisibility",
                "testPublicVisibility",
                "testPublicVisibilityInComplexVar",
                "testPublicWithIsolatedFuncType",
                "testPublicWithIsolatedObjectType",
        };
    }

    @Test
    public void testModulePublicVariableSingleFile() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/statements/vardeclr/module_public_var.bal");
        assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test
    public void testModulePublicVariableAccessNegative() {
        CompileResult compileResultNegative =
                BCompileUtil.compile("test-src/statements/vardeclr/access-project-negative");
        int index = 0;
        validateError(compileResultNegative, index++, "isolated variable cannot be public", 17, 1);
        validateError(compileResultNegative, index++, "variable declared with var cannot be public", 19, 1);
        validateError(compileResultNegative, index++, "attempt to refer to non-accessible symbol 'name'", 20, 21);
        validateError(compileResultNegative, index++, "undefined symbol 'name'", 20, 21);
        assertEquals(compileResultNegative.getErrorCount(), index);
    }
}
