/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completion.definitions;

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Completion item tests for Top Level Resolving.
 */
public class TopLevelNodeCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(TopLevelNodeCompletionTest.class);

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws IOException, WorkspaceDocumentException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for Top Level Scope");
        return new Object[][] {
                {"allTopLevelItemSkip.json", "toplevel"},
                {"topLevelNonEmptyFirstLine.json", "toplevel"},
                {"topLevelEmptyFirstLine.json", "toplevel"},
                {"topLevelFirstLineWithPublicKeyword.json", "toplevel"},
                {"topLevelFirstLineWithPublicDefStart.json", "toplevel"},
                {"mapTest1.json", "toplevel"},
                {"recordTest1.json", "toplevel"},
                {"recordTest2.json", "toplevel"},
                {"globalVarDef1.json", "toplevel"},
                {"globalVarDef2.json", "toplevel"},
                {"globalVarDef3.json", "toplevel"},
                {"globalVarDefPackageContent.json", "toplevel"},
                {"topLevelCompletionAfterDocumentation.json", "toplevel"},
                {"importStatement1.json", "toplevel"},
                {"importStatement2.json", "toplevel"},
                {"importStatement3.json", "toplevel"},
                {"importStatement4.json", "toplevel"},
                {"namespaceDeclarationStatement1.json", "toplevel"},
                {"topLevelCompletionAfterListenerKeyword1.json", "toplevel"},
                {"topLevelCompletionAfterListenerKeyword2.json", "toplevel"},
                {"topLevelCompletionAfterListenerKeyword3.json", "toplevel"},
                {"topLevelCompletionAfterFinalKeyword1.json", "toplevel"},
                {"topLevelCompletionAfterFinalKeyword2.json", "toplevel"},
//                {"topLevelPackageContentAccess.json", "toplevel"},
                {"constantDefinition1.json", "toplevel"},
                {"statementWithMissingSemiColon1.json", "toplevel"},
                {"statementWithMissingSemiColon2.json", "toplevel"},
                {"statementWithMissingSemiColon3.json", "toplevel"},
                {"statementWithMissingSemiColon4.json", "toplevel"},
        };
    }
}
