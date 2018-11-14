/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.completion;

/**
 * Hidden template completion test.
 */
public class BallerinaHiddenTemplateCompletionTest extends BallerinaCompletionTestBase {

    /**
     * Hidden templates.
     */

    public void testImportKeyword() {
        doCheckResult("test.bal", "import<caret>", "import ", null);
    }

    public void testIfKeyword() {
        doCheckResult("test.bal", "function test(){ if<caret> }", "function test(){ if () {\n    \n} }", null);
    }

    public void testElseKeyword() {
        doCheckResult("test.bal", "function test(){ else<caret> }", "function test(){ else }", null);
    }

    public void testForkKeyword() {
        doCheckResult("test.bal", "function test(){\n fork<caret> }",
                "function test(){\n fork {\n     \n } join () () {\n     \n } }", null);
    }

    public void testJoinKeyword() {
        doCheckResult("test.bal", "function test(){ join<caret> }", "function test(){ join () () {\n    \n}" +
                " }", null);
    }

    public void testTimeoutKeyword() {
        doCheckResult("test.bal", "function test(){ timeout<caret> }", "function test(){ timeout () () " +
                "{\n    \n} }", null);
    }

    public void testWorkerKeyword() {
        doCheckResult("test.bal", "function test(){ worker<caret> }", "function test(){ worker  {\n    \n} }",
                null);
    }

    public void testTransactionKeyword() {
        doCheckResult("test.bal", "function test(){ transaction<caret> }",
                "function test(){ transaction with retries = , oncommit = , onabort =  {\n    \n} }", null);
    }

    public void testTryKeyword() {
        doCheckResult("test.bal", "function test(){ try<caret> }",
                "function test(){ try {\n    \n} catch () {\n    \n} finally {\n    \n} }", null);
    }

    public void testCatchKeyword() {
        doCheckResult("test.bal", "function test(){ catch<caret> }", "function test(){ catch () {\n    \n} }",
                null);
    }

    public void testFinallyKeyword() {
        doCheckResult("test.bal", "function test(){ finally<caret> }", "function test(){ finally {\n    \n} }", null);
    }

    public void testForEachKeyword() {
        doCheckResult("test.bal", "function test(){ foreach<caret> }", "function test(){ foreach  in  {\n    \n} }",
                null);
    }

    public void testWhileKeyword() {
        doCheckResult("test.bal", "function test(){ while<caret> }", "function test(){ while () {\n    \n} }",
                null);
    }

    public void testContinueKeyword() {
        doCheckResult("test.bal", "function test(){ continue<caret> }", "function test(){ continue }", null);
    }

    public void testBreakKeyword() {
        doCheckResult("test.bal", "function test(){ break<caret> }", "function test(){ break }", null);
    }

    public void testThrowKeyword() {
        doCheckResult("test.bal", "function test(){ throw<caret> }", "function test(){ throw ; }", null);
    }
}
