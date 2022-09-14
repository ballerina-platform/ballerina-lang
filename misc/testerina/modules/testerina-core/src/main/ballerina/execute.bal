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
    error? err = orderTests();
    if err is error { //TODO: break the execution and display the error in a better way
        println(err.message());
    } else {
        executeBeforeSuiteFunctions();
        executeTests();
        executeAfterSuiteFunctions();
        reportGenerators.forEach(reportGen => reportGen(reportData));
    }

}

function executeTests() {
    foreach TestFunction testFunction in testRegistry.getFunctions() {
        executeTest(testFunction);
    }
}

function executeTest(TestFunction testFunction) {
    if !testFunction.enabled {
        return;
    }
    error? diagnoseError = testFunction.diagnostics;
    if diagnoseError is error {
        reportData.onFailed(name = testFunction.name, message = diagnoseError.message());
        return;
    }
    if testFunction.dependsOnCount > 1 {
        testFunction.dependsOnCount -= 1;
        return;
    }

    executeBeforeGroupFunctions(testFunction);
    executeBeforeEachFunctions();
    executeBeforeFunction(testFunction);

    if !testFunction.skip && !shouldSkip {
        DataProviderReturnType? params = testFunction.params;
        if params is map<AnyOrError[]> {
            foreach [string, AnyOrError[]] entry in params.entries() {
                if executeDataDrivenTest(testFunction, entry[0], entry[1]) {
                    break;
                }
            }
        } else if params is AnyOrError[][] {
            int i = 0;
            foreach AnyOrError[] entry in params {
                if executeDataDrivenTest(testFunction, i.toString(), entry) {
                    break;
                }
                i += 1;
            }
        } else {
            ExecutionError? err = executeTestFunction(testFunction, "");
            if err is ExecutionError {
                reportData.onFailed(name = testFunction.name, message = err.message());
            }
        }
    } else {
        reportData.onSkipped(name = testFunction.name);
    }

    testFunction.groups.forEach(group => groupStatusRegistry.incrementExecutedTest(group));
    executeAfterFunction(testFunction);
    executeAfterEachFunctions();
    executeAfterGroupFunctions(testFunction);

    testFunction.dependents.forEach(dependent => executeTest(dependent));
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

function executeDataDrivenTest(TestFunction testFunction, string suffix, AnyOrError[] params) returns boolean {
    if hasFilteredTests {
        int? testIndex = filterSubTests.indexOf(testFunction.name + DATA_PROVIDER_SEPARATOR + suffix);
        if testIndex is int {
            _ = filterSubTests.remove(testIndex);
        } else {
            return false;
        }
    }
    ExecutionError? err = executeTestFunction(testFunction, suffix, params);
    if err is ExecutionError {
        reportData.onFailed(name = testFunction.name, message = "[fail data provider for the function " + testFunction.name
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

function executeTestFunction(TestFunction testFunction, string suffix, AnyOrError[]? params = ()) returns ExecutionError? {
    any|error output = params == () ? trap function:call(testFunction.executableFunction)
        : trap function:call(testFunction.executableFunction, ...params);
    if output is TestError {
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = getErrorMessage(output));
    } else if output is any {
        reportData.onPassed(name = testFunction.name, suffix = suffix);
    } else {
        return error(getErrorMessage(output), functionName = testFunction.name);
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

function orderTests() returns error? {
    string[] descendants = [];

    foreach TestFunction testFunction in testRegistry.getDependentFunctions() {
        if !testFunction.visited && testFunction.enabled {
            check restructureTest(testFunction, descendants);
        }
    }
}

function restructureTest(TestFunction testFunction, string[] descendants) returns error? {
    descendants.push(testFunction.name);

    // TODO: Change the type to function after https://github.com/ballerina-platform/ballerina-lang/issues/37379 fixed
    foreach string dependsOnFunction in testFunction.dependsOnString {
        TestFunction dependsOnTestFunction = check testRegistry.getTestFunction(dependsOnFunction);
        dependsOnTestFunction.dependents.push(testFunction);

        // Contains cyclic dependencies 
        int? startIndex = descendants.indexOf(dependsOnTestFunction.name);
        if startIndex is int {
            string[] newCycle = descendants.slice(startIndex);
            newCycle.push(dependsOnTestFunction.name);
            return error("Cyclic test dependencies detected: " + string:'join(" -> ", ...newCycle));
        } else if !dependsOnTestFunction.visited {
            check restructureTest(dependsOnTestFunction, descendants);
        }

    }

    testFunction.enabled = true;
    testFunction.visited = true;
    _ = descendants.pop();
}
