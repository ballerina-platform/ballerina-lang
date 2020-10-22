/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.signature;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for Expressions Signature help.
 *
 * @since 2.0.0
 */
public class ExpressionsTest extends AbstractSignatureHelpTest {
    @Test(dataProvider = "signature-help-data-provider", description = "Test Signature Help for Expressions")
    public void test(String config, String source)
            throws WorkspaceDocumentException, InterruptedException, IOException {
        super.test(config, source);
    }

    @DataProvider(name = "signature-help-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public List<String> skipList() {
        return Arrays.asList("exprTableConstructor.json",
                "exprXMLAttributes.json",
                "exprFunctionConstructorDirect.json",
                "exprNewImplicit.json",
                "exprFunctionConstructorIndirect.json",
                "exprNewExplicit.json");
    }

    @Override
    public String getTestResourceDir() {
        return "expressions";
    }
}
