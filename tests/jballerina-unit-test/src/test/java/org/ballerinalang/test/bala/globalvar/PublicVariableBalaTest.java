/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bala.globalvar;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Public variable in BALA test.
 * 
 * @since 2.0
 */
public class PublicVariableBalaTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_public_var");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/globalvar/test_public_variable.bal");
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
    public void testModulePublicVariableAccessNegative() {
        CompileResult compileResultNegetive =
                BCompileUtil.compile("test-src/bala/test_bala/globalvar/test_public_variable_negative.bal");
        int index = 0;
        validateError(compileResultNegetive, index++, "attempt to refer to non-accessible symbol 'number'", 20, 20);
        validateError(compileResultNegetive, index++, "undefined symbol 'number'", 20, 20);
        assertEquals(compileResultNegetive.getErrorCount(), index);
    }
}
