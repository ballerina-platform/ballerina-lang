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

import com.intellij.codeInsight.hint.ParameterInfoComponent;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoUIContextEx;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockUpdateParameterInfoContext;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;
import org.ballerinalang.BallerinaSDKAware;
import org.ballerinalang.plugins.idea.editor.BallerinaParameterInfoHandler;
import org.ballerinalang.plugins.idea.project.BallerinaApplicationLibrariesService;
import org.jetbrains.annotations.NotNull;

/**
 * Parameter info handler tests.
 */
@BallerinaSDKAware
public class BallerinaParameterInfoHandlerTest extends BallerinaCodeInsightFixtureTestCase {

    private static final String NO_PARAMETER = "<html>&lt;no parameters&gt;</html>";
    private BallerinaParameterInfoHandler myParameterInfoHandler;

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("editor/parameterInfo");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BallerinaApplicationLibrariesService.getInstance().setLibraryRootUrls("temp:///");
        if (isSdkAware()) {
            setUpProjectSdk();
        }
        myParameterInfoHandler = new BallerinaParameterInfoHandler();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            BallerinaApplicationLibrariesService.getInstance().setLibraryRootUrls();
        } finally {
            //noinspection ThrowFromFinallyBlock
            super.tearDown();
        }
    }

//    public void testLocalActionWithNoParam() {
//        doTest(NO_PARAMETER);
//    }

//    public void testLocalActionWithSingleParam() {
//        doTest("<html><b>string a</b></html>");
//    }
//
//    public void testLocalActionWithMultipleParam() {
//        doTest("<html><b>string a</b>, string b</html>");
//    }
//
//    public void testLocalActionWithMultipleParamSecondElement() {
//        doTest(1, "<html>string a, <b>string b</b></html>");
//    }
//
//    public void testLocalConnectorWithNoParam() {
//        doTest(NO_PARAMETER);
//    }
//
//    public void testLocalConnectorWithSingleParam() {
//        doTest("<html><b>string a</b></html>");
//    }
//
//    public void testLocalConnectorWithMultipleParam() {
//        doTest("<html><b>string a</b>, string b</html>");
//    }
//
//    public void testLocalConnectorWithMultipleParamSecondElement() {
//        doTest(1, "<html>string a, <b>string b</b></html>");
//    }

    public void testLocalFunctionWithNoParam() {
        doTest(NO_PARAMETER);
    }

//    public void testLocalFunctionWithSingleParam() {
//        doTest("<html><b>string a</b></html>");
//    }

//    public void testLocalFunctionWithMultipleParam() {
//        doTest("<html><b>string a</b>, string b</html>");
//    }
//
//    public void testLocalFunctionWithMultipleParamSecondElement() {
//        doTest(1, "<html>string a, <b>string b</b></html>");
//    }

    public void testSDKFunctionWithNoParam() {
        doTest(NO_PARAMETER);
    }

//    public void testSDKActionWithMultipleParam() {
//        doTest("<html><b>string serviceUri</b>, Options connectorOptions</html>");
//    }
//
//    public void testSDKActionWithMultipleParamSecondElement() {
//        doTest(1, "<html>string serviceUri, <b>Options connectorOptions</b></html>");
//    }
//
//    public void testSDKFunctionWithSingleParam() {
//        doTest("<html><b>string msg</b></html>");
//    }
//
//    public void testSDKFunctionWithMultipleParam() {
//        doTest("<html><b>string name</b>, string value</html>");
//    }
//
//    public void testSDKFunctionWithMultipleParamSecondElement() {
//        doTest(1, "<html>string name, <b>string value</b></html>");
//    }

    private void doTest(@NotNull String expectedPresentation) {
        doTest(0, expectedPresentation);
    }

    private void doTest(int expectedParamIdx, @NotNull String expectedPresentation) {
        myFixture.configureByFile(getTestName(false) + ".bal");
        Object[] itemsToShow = getItemsToShow();
        int paramIdx = getHighlightedItem();
        String presentation = getPresentation(itemsToShow, paramIdx);
        assertEquals(1, itemsToShow.length);
        assertEquals(expectedParamIdx, paramIdx);
        assertEquals(expectedPresentation, presentation);
    }

    private Object[] getItemsToShow() {
        CreateParameterInfoContext createCtx = new MockCreateParameterInfoContext(myFixture.getEditor(),
                myFixture.getFile());
        PsiElement psiElement = (PsiElement) myParameterInfoHandler.findElementForParameterInfo(createCtx);
        assertNotNull(psiElement);
        myParameterInfoHandler.showParameterInfo(psiElement, createCtx);
        return createCtx.getItemsToShow();
    }

    private int getHighlightedItem() {
        MockUpdateParameterInfoContext updateCtx = new MockUpdateParameterInfoContext(myFixture.getEditor(),
                myFixture.getFile());
        PsiElement psiElement = (PsiElement) myParameterInfoHandler.findElementForUpdatingParameterInfo(updateCtx);
        assertNotNull(psiElement);
        myParameterInfoHandler.updateParameterInfo(psiElement, updateCtx);
        return updateCtx.getCurrentParameter();
    }

    private String getPresentation(Object[] itemsToShow, int paramIdx) {
        ParameterInfoUIContextEx uiCtx = ParameterInfoComponent.createContext(itemsToShow, myFixture.getEditor(),
                myParameterInfoHandler, paramIdx);
        return BallerinaParameterInfoHandler.updatePresentation(itemsToShow[0], uiCtx);
    }
}
