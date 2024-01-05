// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function executeTest(TestFunction testFunction) {
    if !conMgr.isEnabled(testFunction.name) {
        conMgr.setExecutionDone(testFunction.name);
        conMgr.releaseWorker();
        return;
    }

    error? diagnoseError = testFunction.diagnostics;
    if diagnoseError is error {
        reportData.onFailed(name = testFunction.name, message = diagnoseError.message(), testType = getTestType(testFunction));
        println("\n" + testFunction.name + " has failed.\n");
        enableExit();
        conMgr.setExecutionDone(testFunction.name);
        conMgr.releaseWorker();
        return;
    }

    executeBeforeGroupFunctions(testFunction);
    executeBeforeEachFunctions();

    boolean shouldSkipDependents = false;
    if !conMgr.isSkip(testFunction.name) && !getShouldSkip() {
        if (isDataDrivenTest(testFunction)) {
            executeDataDrivenTestSet(testFunction);
        } else {
            shouldSkipDependents = executeNonDataDrivenTest(testFunction);
        }
    } else {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        shouldSkipDependents = true;
    }

    testFunction.groups.forEach('group => groupStatusRegistry.incrementExecutedTest('group));
    executeAfterEachFunctions();
    executeAfterGroupFunctions(testFunction);

    if shouldSkipDependents {
        conMgr.getDependents(testFunction.name).forEach(function(TestFunction dependent) {
            conMgr.setSkip(dependent.name);
        });
    }
    conMgr.setExecutionDone(testFunction.name);
    conMgr.releaseWorker();
}

function executeBeforeGroupFunctions(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted('group) {
            ExecutionError? err = executeFunctions(beforeGroupFunctions, getShouldSkip());
            if err is ExecutionError {
                conMgr.setSkip(testFunction.name);
                groupStatusRegistry.setSkipAfterGroup('group);
                enableExit();
                printExecutionError(err, "before test group function for the test");
            }
        }
    }
}

function executeBeforeEachFunctions() {
    ExecutionError? err = executeFunctions(beforeEachRegistry.getFunctions(), getShouldSkip());
    if err is ExecutionError {
        enableShouldSkip();
        enableExit();
        printExecutionError(err, "before each test function for the test");
    }
}

function executeDataDrivenTestSet(TestFunction testFunction) {
    DataProviderReturnType? params = testFunction.params;
    string[] keys = [];
    AnyOrError[][] values = [];
    TestType testType = DATA_DRIVEN_MAP_OF_TUPLE;
    if params is map<AnyOrError[]> {
        foreach [string, AnyOrError[]] entry in params.entries() {
            keys.push(entry[0]);
            values.push(entry[1]);
        }
    } else if params is AnyOrError[][] {
        testType = DATA_DRIVEN_TUPLE_OF_TUPLE;
        int i = 0;
        foreach AnyOrError[] entry in params {
            keys.push(i.toString());
            values.push(entry);
            i += 1;
        }
    }

    while keys.length() != 0 {
        string key = keys.remove(0);
        AnyOrError[] value = values.remove(0);
        prepareDataDrivenTest(testFunction, key, value, testType);
    }

}

function executeNonDataDrivenTest(TestFunction testFunction) returns boolean {
    boolean failed = false;
    boolean beforeFailed = executeBeforeFunction(testFunction);
    if (beforeFailed) {
        conMgr.setSkip(testFunction.name);
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        return true;
    }
    ExecutionError|boolean output = executeTestFunction(testFunction, "", GENERAL_TEST);
    if output is ExecutionError {
        failed = true;
        reportData.onFailed(name = testFunction.name, message = output.message(), testType = GENERAL_TEST);
        println("\n" + testFunction.name + " has failed.\n");
    }

    else if output {
        failed = true;
    }
    boolean afterFailed = executeAfterFunction(testFunction);
    if (afterFailed) {
        return true;
    }
    return failed;
}

function executeAfterEachFunctions() {
    ExecutionError? err = executeFunctions(afterEachRegistry.getFunctions(), getShouldSkip());
    if err is ExecutionError {
        enableShouldSkip();
        enableExit();
        printExecutionError(err, "after each test function for the test");
    }
}

function executeAfterGroupFunctions(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? afterGroupFunctions = afterGroupsRegistry.getFunctions('group);
        if afterGroupFunctions != () && groupStatusRegistry.lastExecuted('group) {
            ExecutionError? err = executeFunctions(afterGroupFunctions,
                    getShouldSkip() || groupStatusRegistry.getSkipAfterGroup('group));
            if err is ExecutionError {
                enableExit();
                printExecutionError(err, "after test group function for the test");
            }
        }
    }
}

function executeFunctions(TestFunction[] testFunctions, boolean skip = false) returns ExecutionError? {
    foreach TestFunction testFunction in testFunctions {
        if !skip || testFunction.alwaysRun {
            check executeFunction(testFunction);
        }
    }
}

function prepareDataDrivenTest(TestFunction testFunction, string key, AnyOrError[] value, TestType testType) {
    boolean beforeFailed = executeBeforeFunction(testFunction);
    if (beforeFailed) {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
    } else {
        executeDataDrivenTest(testFunction, key, testType, value);
        var _ = executeAfterFunction(testFunction);
    }
}

function executeDataDrivenTest(TestFunction testFunction, string suffix, TestType testType, AnyOrError[] params) {
    if (skipDataDrivenTest(testFunction, suffix, testType)) {
        return;
    }

    ExecutionError|boolean err = executeTestFunction(testFunction, suffix, testType, params);
    if err is ExecutionError {
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = "[fail data provider for the function " + testFunction.name
                + "]\n" + getErrorMessage(err), testType = testType);
        println("\n" + testFunction.name + ":" + suffix + " has failed.\n");
        enableExit();
    }
}

function executeBeforeFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if testFunction.before is function && !getShouldSkip() && !conMgr.isSkip(testFunction.name) {
        ExecutionError? err = executeFunction(<function>testFunction.before);
        if err is ExecutionError {
            enableExit();
            printExecutionError(err, "before test function for the test");
            failed = true;
        }
    }
    return failed;
}

function executeTestFunction(TestFunction testFunction, string suffix, TestType testType, AnyOrError[]? params = ()) returns ExecutionError|boolean {
    any|error output = params == () ? trap function:call(testFunction.executableFunction)
        : trap function:call(testFunction.executableFunction, ...params);
    if output is TestError {
        enableExit();
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = getErrorMessage(output), testType = testType);
        println("\n" + testFunction.name + ":" + suffix + " has failed\n");
        return true;
    } else if output is any {
        reportData.onPassed(name = testFunction.name, suffix = suffix, testType = testType);
        return false;
    } else {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction.name);
    }
}

function executeAfterFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if testFunction.after is function && !getShouldSkip() && !conMgr.isSkip(testFunction.name) {
        ExecutionError? err = executeFunction(<function>testFunction.after);
        if err is ExecutionError {
            enableExit();
            printExecutionError(err, "after test function for the test");
            failed = true;
        }
    }
    return failed;
}

function skipDataDrivenTest(TestFunction testFunction, string suffix, TestType testType) returns boolean {
    string functionName = testFunction.name;
    if (!testOptions.getHasFilteredTests()) {
        return false;
    }
    TestFunction[] dependents = conMgr.getDependents(functionName);

    // if a dependent in a below level is enabled, this test should run
    if (dependents.length() > 0 && nestedEnabledDependentsAvailable(dependents)) {
        return false;
    }
    string functionKey = functionName;

    // check if prefix matches directly
    boolean prefixMatch = testOptions.isFilterSubTestsContains(functionName);

    // if prefix matches to a wildcard
    if (!prefixMatch && hasTest(functionName)) {

        // get the matching wildcard
        prefixMatch = true;
        foreach string filter in testOptions.getFilterTests() {
            if (filter.includes(WILDCARD)) {
                boolean|error wildCardMatch = matchWildcard(functionKey, filter);
                if (wildCardMatch is boolean && wildCardMatch && matchModuleName(filter)) {
                    functionKey = filter;
                    break;
                }
            }
        }
    }

    // check if no filterSubTests found for a given prefix
    boolean suffixMatch = !testOptions.isFilterSubTestsContains(functionKey);

    // if a subtest is found specified
    if (!suffixMatch) {
        string[] subTests = testOptions.getFilterSubTest(functionKey);
        foreach string subFilter in subTests {

            string updatedSubFilter = subFilter;
            if (testType == DATA_DRIVEN_MAP_OF_TUPLE) {
                if (subFilter.startsWith(SINGLE_QUOTE) && subFilter.endsWith(SINGLE_QUOTE)) {
                    updatedSubFilter = subFilter.substring(1, subFilter.length() - 1);
                } else {
                    continue;
                }
            }
            string|error decodedSubFilter = escapeSpecialCharacters(updatedSubFilter);
            updatedSubFilter = decodedSubFilter is string ? decodedSubFilter : updatedSubFilter;
            string|error decodedSuffix = escapeSpecialCharacters(suffix);
            string updatedSuffix = decodedSuffix is string ? decodedSuffix : suffix;

            boolean wildCardMatchBoolean = false;
            if (updatedSubFilter.includes(WILDCARD)) {
                boolean|error wildCardMatch = matchWildcard(updatedSuffix, updatedSubFilter);
                wildCardMatchBoolean = wildCardMatch is boolean && wildCardMatch;
            }
            if ((updatedSubFilter == updatedSuffix) || wildCardMatchBoolean) {
                suffixMatch = true;
                break;
            }
        }
    }

    // do not skip iff both matches
    return !(prefixMatch && suffixMatch);
}

function executeFunction(TestFunction|function testFunction) returns ExecutionError? {
    any|error output = trap function:call(testFunction is function ? testFunction : testFunction.executableFunction);
    if output is error {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction is function ? "" : testFunction.name);
    }
}
