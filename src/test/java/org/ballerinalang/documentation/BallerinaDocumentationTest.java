/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.documentation;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;

public class BallerinaDocumentationTest extends BallerinaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("documentation");
    }

    public void testFunction1() {
        doTest();
    }

    public void testFunction2() {
        doTest();
    }

    public void testFunction3() {
        doTest();
    }

    public void testFunction4() {
        doTest();
    }

    public void testConnector1() {
        doTest();
    }

    public void testConnector2() {
        doTest();
    }

    public void testConnector3() {
        doTest();
    }

    public void testConnector4() {
        doTest();
    }

    public void testStruct1() {
        doTest();
    }

    public void testStruct2() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile(getTestName(false) + ".bal");
        Editor editor = myFixture.getEditor();
        PsiFile file = myFixture.getFile();
        PsiElement originalElement = file.findElementAt(editor.getCaretModel().getOffset());
        assertNotNull(originalElement);

        PsiElement docElement = DocumentationManager.getInstance(getProject()).findTargetElement(editor, file);
        DocumentationProvider documentationProvider = DocumentationManager.getProviderFromElement(originalElement);
        String actualDoc = StringUtil.notNullize(documentationProvider.generateDoc(docElement, originalElement));
        assertSameLinesWithFile(getTestDataPath() + "/" + getTestName(false) + ".txt", actualDoc);
    }
}
