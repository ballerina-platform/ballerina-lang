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
int exitCode = 0;

public function startSuite() {
    if listGroups {
        string[] groupsList = groupStatusRegistry.getGroupsList();
        if groupsList.length() == 0 {
            println("\tThere are no groups available!");
        } else {
            println("\tFollowing groups are available :");
            println("\t[" + string:'join(", ", ...groupsList) + "]");
        }
    } else {
        if testRegistry.getFunctions().length() == 0 && testRegistry.getDependentFunctions().length() == 0 {
            println("\tNo tests found");
            return;
        }

        error? err = orderTests();
        if err is error {
            exitCode = 1;
            println(err.message());
        } else {
            executeBeforeSuiteFunctions();
            executeTests();
            executeAfterSuiteFunctions();
            reportGenerators.forEach(reportGen => reportGen(reportData));
        }
    }
    if (exitCode > 0) {
        panic(error(""));
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
        reportData.onFailed(name = testFunction.name, message = diagnoseError.message(), testType = getTestType(testFunction));
        exitCode = 1;
        return;
    }
    if testFunction.dependsOnCount > 1 {
        testFunction.dependsOnCount -= 1;
        return;
    }

    executeBeforeGroupFunctions(testFunction);
    executeBeforeEachFunctions();

    boolean shouldSkipDependents = false;
    if !testFunction.skip && !shouldSkip {
        if (isDataDrivenTest(testFunction)) {
            executeDataDrivenTestSet(testFunction);
        } else {
            shouldSkipDependents = executeNonDataDrivenTest(testFunction);
        }
    } else {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        shouldSkipDependents = true;
    }

    testFunction.groups.forEach(group => groupStatusRegistry.incrementExecutedTest(group));
    executeAfterEachFunctions();
    executeAfterGroupFunctions(testFunction);

    if shouldSkipDependents {
        testFunction.dependents.forEach(function(TestFunction dependent) {
            dependent.skip = true;
        });
    }
    testFunction.dependents.forEach(dependent => executeTest(dependent));
}

function executeDataDrivenTestSet(TestFunction testFunction) {
    DataProviderReturnType? params = testFunction.params;
    if params is map<AnyOrError[]> {
        foreach [string, AnyOrError[]] entry in params.entries() {
            boolean beforeFailed = executeBeforeFunction(testFunction);
            if (beforeFailed) {
                reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
            } else {
                executeDataDrivenTest(testFunction, entry[0], DATA_DRIVEN_MAP_OF_TUPLE, entry[1]);
                var _ = executeAfterFunction(testFunction);
            }
        }
    } else if params is AnyOrError[][] {
        int i = 0;
        foreach AnyOrError[] entry in params {
            boolean beforeFailed = executeBeforeFunction(testFunction);
            if (beforeFailed) {
                reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
            } else {
                executeDataDrivenTest(testFunction, i.toString(), DATA_DRIVEN_TUPLE_OF_TUPLE, entry);
                var _ = executeAfterFunction(testFunction);
            }
            i += 1;
        }
    }
}

function executeDataDrivenTest(TestFunction testFunction, string suffix, TestType testType, AnyOrError[] params) {
    if (skipDataDrivenTest(testFunction, suffix, testType)) {
        return;
    }

    ExecutionError|boolean err = executeTestFunction(testFunction, suffix, testType, params);
    if err is ExecutionError {
        reportData.onFailed(name = testFunction.name, message = "[fail data provider for the function " + testFunction.name
            + "]\n" + getErrorMessage(err), testType = testType);
        exitCode = 1;
    }
}

function executeNonDataDrivenTest(TestFunction testFunction) returns boolean {
    boolean failed = false;
    boolean beforeFailed = executeBeforeFunction(testFunction);
    if (beforeFailed) {
        testFunction.skip = true;
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        return true;
    }
    ExecutionError|boolean output = executeTestFunction(testFunction, "", GENERAL_TEST);
    if output is ExecutionError {
        failed = true;
        reportData.onFailed(name = testFunction.name, message = output.message(), testType = GENERAL_TEST);
    } else if output {
        failed = true;
    }
    boolean afterFailed = executeAfterFunction(testFunction);
    if (afterFailed) {
        return true;
    }
    return failed;
}

function executeBeforeSuiteFunctions() {
    ExecutionError? err = executeFunctions(beforeSuiteRegistry.getFunctions());
    if err is ExecutionError {
        shouldSkip = true;
        shouldAfterSuiteSkip = true;
        exitCode = 1;
        printExecutionError(err, "before test suite function");
    }
}

function executeAfterSuiteFunctions() {
    ExecutionError? err = executeFunctions(afterSuiteRegistry.getFunctions(), shouldAfterSuiteSkip);
    if err is ExecutionError {
        exitCode = 1;
        printExecutionError(err, "after test suite function");
    }
}

function executeBeforeEachFunctions() {
    ExecutionError? err = executeFunctions(beforeEachRegistry.getFunctions(), shouldSkip);
    if err is ExecutionError {
        shouldSkip = true;
        exitCode = 1;
        printExecutionError(err, "before each test function for the test");
    }
}

function executeAfterEachFunctions() {
    ExecutionError? err = executeFunctions(afterEachRegistry.getFunctions(), shouldSkip);
    if err is ExecutionError {
        shouldSkip = true;
        exitCode = 1;
        printExecutionError(err, "after each test function for the test");
    }
}

function executeBeforeFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if testFunction.before is function && !shouldSkip && !testFunction.skip {
        ExecutionError? err = executeFunction(<function>testFunction.before);
        if err is ExecutionError {
            exitCode = 1;
            printExecutionError(err, "before test function for the test");
            failed = true;
        }
    }
    return failed;
}

function executeAfterFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if testFunction.after is function && !shouldSkip && !testFunction.skip {
        ExecutionError? err = executeFunction(<function>testFunction.after);
        if err is ExecutionError {
            exitCode = 1;
            printExecutionError(err, "after test function for the test");
            failed = true;
        }
    }
    return failed;
}

function executeBeforeGroupFunctions(TestFunction testFunction) {
    foreach string group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions(group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted(group) {
            ExecutionError? err = executeFunctions(beforeGroupFunctions, shouldSkip);
            if err is ExecutionError {
                testFunction.skip = true;
                groupStatusRegistry.setSkipAfterGroup(group);
                exitCode = 1;
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
                exitCode = 1;
                printExecutionError(err, "after test group function for the test");
            }
        }
    }
}

function skipDataDrivenTest(TestFunction testFunction, string suffix, TestType testType) returns boolean {
    string functionName = testFunction.name;
    if (!hasFilteredTests) {
        return false;
    }
    TestFunction[] dependents = testFunction.dependents;

    // if a dependent in a below level is enabled, this test should run
    if (dependents.length() > 0 && nestedEnabledDependentsAvailable(dependents)) {
        return false;
    }
    string functionKey = functionName;

    // check if prefix matches directly
    boolean prefixMatch = filterSubTests.hasKey(functionName);

    // if prefix matches to a wildcard
    if (!prefixMatch && hasTest(functionName)) {

        // get the matching wildcard
        prefixMatch = true;
        foreach string filter in filterTests {
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
    boolean suffixMatch = !filterSubTests.hasKey(functionKey);

    // if a subtest is found specified
    if (!suffixMatch) {
        string[] subTests = filterSubTests.get(functionKey);
        foreach string subFilter in subTests {

            string updatedSubFilter = subFilter;
            if (testType == DATA_DRIVEN_MAP_OF_TUPLE) {
                if (subFilter.startsWith(SINGLE_QUOTE) && subFilter.endsWith(SINGLE_QUOTE)) {
                    updatedSubFilter = subFilter.substring(1, subFilter.length() - 1);
                } else {
                    continue;
                }
            }
            string|error decodedSubFilter = decode(updatedSubFilter, UTF8_ENC);
            updatedSubFilter = decodedSubFilter is string? decodedSubFilter : updatedSubFilter;
            string|error decodedSuffix = decode(suffix, UTF8_ENC);
            string updatedSuffix = decodedSuffix is string? decodedSuffix : suffix;
            
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

function printExecutionError(ExecutionError err, string functionSuffix)
    => println("\t[fail] " + err.detail().functionName + "[" + functionSuffix + "]" + ":\n\t    " + err.message());

function executeFunctions(TestFunction[] testFunctions, boolean skip = false) returns ExecutionError? {
    foreach TestFunction testFunction in testFunctions {
        if !skip || testFunction.alwaysRun {
            check executeFunction(testFunction);
        }
    }
}

function executeTestFunction(TestFunction testFunction, string suffix, TestType testType, AnyOrError[]? params = ()) returns ExecutionError|boolean {
    any|error output = params == () ? trap function:call(testFunction.executableFunction)
        : trap function:call(testFunction.executableFunction, ...params);
    if output is TestError {
        exitCode = 1;
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = getErrorMessage(output), testType = testType);
        return true;
    } else if output is any {
        reportData.onPassed(name = testFunction.name, suffix = suffix, testType = testType);
        return false;
    } else {
        exitCode = 1;
        return error(getErrorMessage(output), functionName = testFunction.name);
    }
}

function executeFunction(TestFunction|function testFunction) returns ExecutionError? {
    any|error output = trap function:call(testFunction is function ? testFunction : testFunction.executableFunction);
    if output is error {
        exitCode = 1;
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

    foreach function dependsOnFunction in testFunction.dependsOn {
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

function getTestType(TestFunction testFunction) returns TestType {
    DataProviderReturnType? params = testFunction.params;
    if (params is map<AnyOrError[]>) {
        return DATA_DRIVEN_MAP_OF_TUPLE;
    } else if (params is AnyOrError[][]) {
        return DATA_DRIVEN_TUPLE_OF_TUPLE;
    }
    return GENERAL_TEST;
}

function nestedEnabledDependentsAvailable(TestFunction[] dependents) returns boolean {
    if (dependents.length() == 0) {
        return false;
    }
    TestFunction[] queue = [];
    foreach TestFunction dependent in dependents {
        if(dependent.enabled) {
            return true;
        }
        dependent.dependents.forEach((superDependent) => queue.push(superDependent));
    }
    return nestedEnabledDependentsAvailable(queue);
}
