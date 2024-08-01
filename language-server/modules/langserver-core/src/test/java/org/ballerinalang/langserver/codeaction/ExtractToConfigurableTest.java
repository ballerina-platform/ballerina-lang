/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases for {@link org.ballerinalang.langserver.codeaction.providers.ExtractToConfigurableCodeAction}.
 *
 * @since 2201.10.0
 */
public class ExtractToConfigurableTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_QUICKPICK_SUPPORT, true);
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
    }

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"extractToConfigurableInBracedExpression.json"},
                {"extractToConfigurableInErrorConstructor.json"},
                {"extractToConfigurableInExplicitNewExpression1.json"},
                {"extractToConfigurableInExplicitNewExpression2.json"},
                {"extractToConfigurableInExplicitNewExpression3.json"},
                {"extractToConfigurableInFunctionCallExpr1.json"},
                {"extractToConfigurableInFunctionCallExpr2.json"},
                {"extractToConfigurableInFunctionCallExpr3.json"},
                {"extractToConfigurableInImplicitNewExpr1.json"},
                {"extractToConfigurableInImplicitNewExpr2.json"},
                {"extractToConfigurableInImplicitNewExpr3.json"},
                {"extractToConfigurableInIndexedExpression.json"},
                {"extractToConfigurableInListConstructor.json"},
                {"extractToConfigurableInMappingConstructor1.json"},
                {"extractToConfigurableInMappingConstructor2.json"},
                {"extractToConfigurableInMatchClause.json"},
                {"extractToConfigurableInMethodCall1.json"},
                {"extractToConfigurableInMethodCall2.json"},
                {"extractToConfigurableInMethodCall3.json"},
                {"extractToConfigurableInObjectConstructor.json"},
                {"extractToConfigurableInTableConstructor.json"},
                {"extractToConfigurableInUnaryExpr.json"}
        };
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"extractToConfigurableInConstExprNegative.json"},
                {"extractToConfigurableInTableConstructorNegative.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-configurable";
    }
}
