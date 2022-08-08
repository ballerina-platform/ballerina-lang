// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

boolean shouldSkip = false;
boolean shouldAfterSuiteSkip = false;

public function startSuite() {
    executeBeforeSuiteFunctions();
    executeTests();
    executeAfterSuiteFunctions();
    report();
}

function executeTests() {
    foreach TestFunction testFunction in testRegistry.getFunctions() {
        executeBeforeGroupFunctions(testFunction);
        executeBeforeEachFunctions();
        executeBeforeFunction(testFunction);

        if !testFunction.skip && !shouldSkip {
            DataProviderReturnType? params = testFunction.params;
            if params is map<AnyOrError[]> {
                foreach [string, AnyOrError[]] entry in params.entries() {
                    if executeDataDrivenTest(testFunction, testFunction.name + "#" + entry[0], entry[1]) {
                        break;
                    }
                }
            } else if params is AnyOrError[][] {
                int i = 0;
                foreach AnyOrError[] entry in params {
                    if executeDataDrivenTest(testFunction, testFunction.name + "#" + i.toString(), entry) {
                        break;
                    }
                    i += 1;
                }
            } else {
                ExecutionError? err = executeTestFunction(testFunction, testFunction.name);
                if err is ExecutionError {
                    onFailed(testFunction.name, err.message());
                }
            }
        } else {
            onSkipped(testFunction.name);
        }

        executeAfterFunction(testFunction);
        executeAfterEachFunctions();
        executeAfterGroupFunctions(testFunction);
    }
}

function executeBeforeSuiteFunctions() {
    ExecutionError? err = executeFunctions(beforeSuiteRegistry.getFunctions());
    if err is ExecutionError {
        shouldSkip = true;
        shouldAfterSuiteSkip = true;
        printExecutionError(err, "before test suite function");
    }
}

function executeAfterSuiteFunctions() {
    ExecutionError? err = executeFunctions(afterSuiteRegistry.getFunctions(), shouldAfterSuiteSkip);
    if err is ExecutionError {
        printExecutionError(err, "after test suite function");
    }
}

function executeBeforeEachFunctions() {
    ExecutionError? err = executeFunctions(beforeEachRegistry.getFunctions(), shouldSkip);
    if err is ExecutionError {
        shouldSkip = true;
        printExecutionError(err, "before each test function for the test");
    }
}

function executeAfterEachFunctions() {
    ExecutionError? err = executeFunctions(afterEachRegistry.getFunctions(), shouldSkip);
    if err is ExecutionError {
        shouldSkip = true;
        printExecutionError(err, "after each test function for the test");
    }
}

function executeBeforeFunction(TestFunction testFunction) {
    if testFunction.before is function && !shouldSkip && !testFunction.skip {
        ExecutionError? err = executeFunction(<function>testFunction.before);
        if err is ExecutionError {
            testFunction.skip = true;
            printExecutionError(err, "before test function for the test");
        }
    }
}

function executeAfterFunction(TestFunction testFunction) {
    if testFunction.after is function && !shouldSkip && !testFunction.skip {
        ExecutionError? err = executeFunction(<function>testFunction.after);
        if err is ExecutionError {
            printExecutionError(err, "after test function for the test");
        }
    }
}

function executeBeforeGroupFunctions(TestFunction testFunction) {
    foreach string group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions(group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted(group) {
            ExecutionError? err = executeFunctions(beforeGroupFunctions, shouldSkip);
            if err is ExecutionError {
                testFunction.skip = true;
                groupStatusRegistry.setSkipAfterGroup(group);
                printExecutionError(err, "before test group function for the test");
            }
        }
    }
}

function executeAfterGroupFunctions(TestFunction testFunction) {
    foreach string group in testFunction.groups {
        TestFunction[]? afterGroupFunctions = afterGroupsRegistry.getFunctions(group);
        if afterGroupFunctions != () && groupStatusRegistry.lastExecuted(group) {
            ExecutionError? err = executeFunctions(afterGroupFunctions,
                shouldSkip || groupStatusRegistry.getSkipAfterGroup(group));
            if err is ExecutionError {
                printExecutionError(err, "after test group function for the test");
            }
        }
    }
}

function executeDataDrivenTest(TestFunction testFunction, string name, AnyOrError[] params) returns boolean {
    ExecutionError? err = executeTestFunction(testFunction, name, params);
    if err is ExecutionError {
        onFailed(testFunction.name, "[fail data provider for the function " + testFunction.name
            + "]\n" + getErrorMessage(err));
        return true;
    }
    return false;
}

function printExecutionError(ExecutionError err, string functionSuffix)
    => println("\t[fail] " + err.detail().functionName + "[" + functionSuffix + "]" + ":\n\t    " + err.message());

function executeFunctions(TestFunction[] testFunctions, boolean skip = false) returns ExecutionError? {
    foreach TestFunction testFunction in testFunctions {
        if !skip || testFunction.alwaysRun {
            check executeFunction(testFunction);
        }
    }
}

function executeTestFunction(TestFunction testFunction, string name, AnyOrError[]? params = ()) returns ExecutionError? {
    any|error output = params == () ? trap function:call(testFunction.executableFunction)
        : trap function:call(testFunction.executableFunction, ...params);
    if output is error {
        return error(getErrorMessage(output), functionName = testFunction.name);
    } else {
        onPassed(name);
        testFunction.groups.forEach(group => groupStatusRegistry.incrementExecutedTest(group));
    }
}

function executeFunction(TestFunction|function testFunction) returns ExecutionError? {
    any|error output = trap function:call(testFunction is function ? testFunction : testFunction.executableFunction);
    if output is error {
        return error(getErrorMessage(output), functionName = testFunction is function ? "" : testFunction.name);
    }
}

function getErrorMessage(error err) returns string {
    string|error message = err.detail()["message"].ensureType();
    return message is error ? err.message() : message;
}
