/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.ballerinalang.test.statements.variabledef;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test forward variable definitions not allowed.
 *
 * @since 0.990.xxx what is this
 */
public class ForwardReferenceVariableDefinitionTest {
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        resultNegative = BCompileUtil.
                compile("test-src/statements/variabledef/forward-reference-in-global-vardef-negative.bal");
    }

    @Test(description = "-----------------")
    public void simpleDefinition() {
        Diagnostic[] diagnostics = resultNegative.getDiagnostics();
//        Assert.assertTrue(diagnostics.length > 0);

        // this should not go into the commit, just for testing the issue.
        BValue[] returns = BRunUtil.invoke(resultNegative, "getEmployee");
        int i = 0;

    }
}
