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

package org.ballerinalang.editor;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;

import java.util.List;

/**
 * Exit point highlighter tests.
 */
public class BallerinaExitPointHighlightingTest extends BallerinaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("editor/exitPoints");
    }

    public void testSingleReturn() {
        doTest("return;");
    }

//    public void testMultipleReturns() {
//        doTest("return \"\";", "return args[0];");
//    }

    public void testSingleThrow() {
        doTest("throw err;");
    }

    public void testMultipleThrows() {
        doTest("throw err;", "throw err;");
    }

    private void doTest(String... usages) {
        myFixture.configureByFile(getTestName(false) + ".bal");
        @SuppressWarnings("unchecked")
        HighlightUsagesHandlerBase<PsiElement> handler =
                HighlightUsagesHandler.createCustomHandler(myFixture.getEditor(), myFixture.getFile());
        assertNotNull(handler);
        List<PsiElement> targets = handler.getTargets();
        assertEquals(1, targets.size());
        handler.computeUsages(targets);
        List<String> textUsages = ContainerUtil.map(handler.getReadUsages(),
                range -> range.substring(myFixture.getFile().getText()));
        assertSameElements(textUsages, usages);
    }
}
