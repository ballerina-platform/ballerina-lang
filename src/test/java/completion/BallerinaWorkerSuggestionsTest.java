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

package completion;

public class BallerinaWorkerSuggestionsTest extends BallerinaCompletionTestBase {

    // In functions.
    public void testWorkerSuggestionsInWorkerInvocationStatementInFunction() {
        doTest("function testWorker () { message msg = {}; msg -> <caret> worker worker1 { } " +
                "worker worker2 { } }", "worker1", "worker2");
    }

    public void testWorkerSuggestionsInWorkerInvocationStatementInFunctionAutoCompletion() {
        doCheckResult("test.bal", "function testWorker () { message msg = {}; msg -> w<caret> worker worker1 { }",
                "function testWorker () { message msg = {}; msg -> worker1 worker worker1 { }", null);
    }

    public void testWorkerSuggestionsInWorkerReplyStatementInFunction() {
        doTest("function testWorker () { message msg = {}; msg <- <caret> worker worker1 { } " +
                "worker worker2 { } }", "worker1", "worker2");
    }

    public void testWorkerSuggestionsInWorkerReplyStatementInFunctionAutoCompletion() {
        doCheckResult("test.bal", "function testWorker () { message msg = {}; msg <- w<caret> worker worker1 { }",
                "function testWorker () { message msg = {}; msg <- worker1 worker worker1 { }", null);
    }

    public void testWorkerSuggestionsInSomeJoinConditionInFunction() {
        doTest("function test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join (some 1 <caret>)" +
                " (map responses) { } }", "ABC_Airline", "XYZ_Airline");
    }

    public void testWorkerSuggestionsInSomeJoinConditionInFunctionAutoCompletion() {
        doCheckResult("test.bal", "function test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } " +
                "join (some 1 AB<caret>) (map responses) { } }", "function test ()  { fork { worker ABC_Airline { } " +
                "worker XYZ_Airline { } } join (some 1 ABC_Airline) (map responses) { } }", null);
    }

    public void testWorkerSuggestionsInSomeJoinConditionAfterCommaInFunction() {
        doTest("function test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join (some 1 ABC_Airline," +
                " <caret>) (map responses) { } }", "ABC_Airline", "XYZ_Airline");
    }

    public void testWorkerSuggestionsInSomeJoinConditionAfterCommaInFunctionAutoCompletion() {
        doCheckResult("test.bal", "function test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } " +
                "join (some 1 ABC_Airline, X<caret>) (map responses) { } }", "function test ()  { fork { " +
                "worker ABC_Airline { } worker XYZ_Airline { } } join (some 1 ABC_Airline, XYZ_Airline) (map " +
                "responses) { } }", null);
    }

    // In services.
    public void testWorkerSuggestionsInWorkerInvocationStatementInService() {
        doTest("service test { resource testWorker () { message msg = {}; msg -> <caret> worker worker1 { } " +
                "worker worker2 { } } }", "worker1", "worker2");
    }

    public void testWorkerSuggestionsInWorkerInvocationStatementInServiceAutoCompletion() {
        doCheckResult("test.bal", "service test { resource testWorker () { message msg = {}; msg -> w<caret> worker" +
                " worker1 { } } }", "service test { resource testWorker () { message msg = {}; msg -> worker1" +
                " worker worker1 { } } }", null);
    }

    public void testWorkerSuggestionsInWorkerReplyStatementInService() {
        doTest("service test { resource testWorker () { message msg = {}; msg <- <caret> worker worker1 { } " +
                "worker worker2 { } } }", "worker1", "worker2");
    }

    public void testWorkerSuggestionsInWorkerReplyStatementInServiceAutoCompletion() {
        doCheckResult("test.bal", "service test { resource testWorker () { message msg = {}; msg <- w<caret> worker" +
                " worker1 { } } }", "service test { resource testWorker () { message msg = {}; msg <- worker1" +
                " worker worker1 { } } }", null);
    }

    public void testWorkerSuggestionsInSomeJoinConditionInService() {
        doTest("service test { resource test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join (some " +
                "1 <caret>) (map responses) { } } }", "ABC_Airline", "XYZ_Airline");
    }

    public void testWorkerSuggestionsInSomeJoinConditionInServiceAutoCompletion() {
        doCheckResult("test.bal", "service test { resource test ()  { fork { worker ABC_Airline { } worker " +
                        "XYZ_Airline { } } join (some 1 AB<caret>) (map responses) { } } }",
                "service test { resource test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join (some" +
                        " 1 ABC_Airline) (map responses) { } } }", null);
    }

    public void testWorkerSuggestionsInSomeJoinConditionAfterCommaInService() {
        doTest("service test { resource test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join (some " +
                "1 ABC_Airline, <caret>) (map responses) { } } }", "ABC_Airline", "XYZ_Airline");
    }

    public void testWorkerSuggestionsInSomeJoinConditionAfterCommaInServiceAutoCompletion() {
        doCheckResult("test.bal", "service test { resource test ()  { fork { worker ABC_Airline { } worker " +
                        "XYZ_Airline { } } join (some 1 ABC_Airline, X<caret>) (map responses) { } } }",
                "service test { resource test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join (some" +
                        " 1 ABC_Airline, XYZ_Airline) (map responses) { } } }", null);
    }

    // In actions.
    public void testWorkerSuggestionsInWorkerInvocationStatementInConnector() {
        doTest("connector test () { action testWorker () { message msg = {}; msg -> <caret> worker worker1 { } " +
                "worker worker2 { } } }", "worker1", "worker2");
    }

    public void testWorkerSuggestionsInWorkerInvocationStatementInConnectorAutoCompletion() {
        doCheckResult("test.bal", "connector test () { action testWorker () { message msg = {}; msg -> w<caret> " +
                "worker worker1 { } } }", "connector test () { action testWorker () { message msg = {}; msg -> " +
                "worker1 worker worker1 { } } }", null);
    }

    public void testWorkerSuggestionsInWorkerReplyStatementInConnector() {
        doTest("connector test () { action testWorker () { message msg = {}; msg <- <caret> worker worker1 { } " +
                "worker worker2 { } } }", "worker1", "worker2");
    }

    public void testWorkerSuggestionsInWorkerReplyStatementInConnectorAutoCompletion() {
        doCheckResult("test.bal", "connector test () { action testWorker () { message msg = {}; msg <- w<caret> " +
                "worker worker1 { } } }", "connector test () { action testWorker () { message msg = {}; msg <- " +
                "worker1 worker worker1 { } } }", null);
    }

    public void testWorkerSuggestionsInSomeJoinConditionInConnector() {
        doTest("connector test () { action test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join " +
                "(some 1 <caret>) (map responses) { } } }", "ABC_Airline", "XYZ_Airline");
    }

    public void testWorkerSuggestionsInSomeJoinConditionInConnectorAutoCompletion() {
        doCheckResult("test.bal", "connector test () { action test ()  { fork { worker ABC_Airline { } worker " +
                        "XYZ_Airline { } } join (some 1 AB<caret>) (map responses) { } } }",
                "connector test () { action test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join " +
                        "(some 1 ABC_Airline) (map responses) { } } }", null);
    }

    public void testWorkerSuggestionsInSomeJoinConditionAfterCommaInConnector() {
        doTest("connector test () { action test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join " +
                "(some 1 ABC_Airline, <caret>) (map responses) { } } }", "ABC_Airline", "XYZ_Airline");
    }

    public void testWorkerSuggestionsInSomeJoinConditionAfterCommaInConnectorAutoCompletion() {
        doCheckResult("test.bal", "connector test () { action test ()  { fork { worker ABC_Airline { } worker " +
                        "XYZ_Airline { } } join (some 1 ABC_Airline, X<caret>) (map responses) { } } }",
                "connector test () { action test ()  { fork { worker ABC_Airline { } worker XYZ_Airline { } } join " +
                        "(some 1 ABC_Airline, XYZ_Airline) (map responses) { } } }", null);
    }
}
