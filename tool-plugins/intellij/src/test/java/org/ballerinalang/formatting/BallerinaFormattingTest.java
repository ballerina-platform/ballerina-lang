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

package org.ballerinalang.formatting;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.Nullable;

/**
 * Formatting tests.
 */
public class BallerinaFormattingTest extends BallerinaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("formatting");
    }

    //    public void testEchoService() {
    //        doTest();
    //    }

    public void testHelloWorld() {
        doTest();
    }

    //    public void testHelloWorldService() {
    //        doTest();
    //    }

    //    public void testTweetMediumFeed() {
    //        doTest();
    //    }
    //
    //    public void testTweetOpenPR() {
    //        doTest();
    //    }
    //
    //    public void testTwitterConnector() {
    //        doTest();
    //    }
    //
    //    public void testAnnotationDefinition() {
    //        doTest();
    //    }


    public void testConstant() {
        doTest();
    }

    public void testExpressions() {
        doTest();
    }

    public void testForkJoin() {
        doTest();
    }

    public void testFunctionDefinition() {
        doTest();
    }

    public void testGlobalVariable() {
        doTest();
    }

    public void testLambdaFunction() {
        doTest();
    }

    public void testNative() {
        doTest();
    }

    public void testServiceDefinition() {
        doTest();
    }

    public void testStatement() {
        doTest();
    }

    public void testStruct() {
        doTest();
    }

    public void testTransaction() {
        doTest();
    }

    public void testTypeName() {
        doTest();
    }

    public void testWorker() {
        doTest();
    }

    private void doTest() {
        doTest(null);
    }

    private void doTestEnter() {
        doTest('\n');
    }

    private void doTest(@Nullable Character c) {
        String testName = getTestName(false);
        myFixture.configureByFile(testName + ".bal");
        String after = doTest(c, testName);

        myFixture.checkResultByFile(after);
        // To debug - Comment the above line and uncomment these lines and add expected result.
        // String result = "";
        // myFixture.checkResult(result);
    }

    private String doTest(@Nullable Character c, String testName) {
        if (c == null) {
            WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> {
                CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
            });
        } else {
            myFixture.type(c);
        }
        return String.format("%s-after.bal", testName);
    }
}
