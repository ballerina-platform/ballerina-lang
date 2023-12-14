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
import ballerina/lang.'error as langError;
import ballerina/lang.runtime;

isolated boolean shouldSkip = false;
boolean shouldAfterSuiteSkip = false;
isolated int exitCode = 0;
ConcurrentExecutionManager conMgr = new (1);

public function startSuite() returns int {
    // exit if setTestOptions has failed
    lock {
        if exitCode > 0 {
            return exitCode;
        }
    }

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
            lock {
                return exitCode;
            }

        }

        error? err = orderTests();
        if err is error {
            enableExit();
            println(err.message());
        } else {
            executeBeforeSuiteFunctions();
            err = executeTests();
            if err is error {
                enableExit();
                println(err.message());
            }
            executeAfterSuiteFunctions();
            reportGenerators.forEach(reportGen => reportGen(reportData));
        }
    }
    lock {
        return exitCode;
    }
}

function executeTests() returns error? {
    decimal startTime = currentTimeInMillis();
    foreach TestFunction testFunction in testRegistry.getFunctions() {
        _ = testFunction.parallelizable ? conMgr.addInitialParallelTest(testFunction) : conMgr.addInitialSerialTest(testFunction);
    }

    while !conMgr.isExecutionDone() {

        if conMgr.getAvailableWorkers() != 0 {
            conMgr.waitUntilEmptyQueueFilled();

            if conMgr.getSerialQueueLength() != 0 && conMgr.getAvailableWorkers() == testWorkers {
                TestFunction testFunction = conMgr.getSerialTest();
                conMgr.addTestInExecution(testFunction);
                conMgr.allocateWorker();
                future<error?> serialWaiter = start executeTest(testFunction);
                any _ = check wait serialWaiter;

            }

            else if conMgr.getParallelQueueLength() != 0 && conMgr.getSerialQueueLength() == 0 {
                TestFunction testFunction = conMgr.getParallelTest();
                conMgr.addTestInExecution(testFunction);
                conMgr.allocateWorker();
                future<(error?)> parallelWaiter = start executeTest(testFunction);
                if isDataDrivenTest(testFunction) {
                    any _ = check wait parallelWaiter;
                }

            }

        }
        runtime:sleep(0.0001); // sleep is added to yield the strand
    }

    println("\n\t\tTest execution time :" + (currentTimeInMillis() - startTime).toString() + "ms\n");
}

function executeTest(TestFunction testFunction) returns error? {
    if !testFunction.enabled {
        lock {
            testFunction.isExecutionDone = true;
        }
        conMgr.releaseWorker();
        return;
    }

    error? diagnoseError = testFunction.diagnostics;
    if diagnoseError is error {
        lock {
            reportData.onFailed(name = testFunction.name, message = diagnoseError.message(), testType = getTestType(testFunction));
            println("\n" + testFunction.name + " has failed.\n");
        }
        enableExit();
        lock {
            testFunction.isExecutionDone = true;
        }
        conMgr.releaseWorker();
        return;
    }

    executeBeforeGroupFunctions(testFunction);
    executeBeforeEachFunctions();

    boolean shouldSkipDependents = false;
    if !testFunction.skip && !getShouldSkip() {
        if (isDataDrivenTest(testFunction)) {
            check executeDataDrivenTestSet(testFunction);
        } else {
            shouldSkipDependents = executeNonDataDrivenTest(testFunction);
        }
    } else {
        lock {
            reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        }
        shouldSkipDependents = true;
    }

    testFunction.groups.forEach('group => groupStatusRegistry.incrementExecutedTest('group));
    executeAfterEachFunctions();
    executeAfterGroupFunctions(testFunction);

    println(testFunction.name + " on execution");
    if shouldSkipDependents {
        testFunction.dependents.forEach(function(TestFunction dependent) {
            lock {
                dependent.skip = true;
            }
        });
    }
    lock {
        testFunction.isExecutionDone = true;
    }
    conMgr.releaseWorker();
}

function executeDataDrivenTestSet(TestFunction testFunction) returns error? {
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

    boolean isIntialJob = true;

    while keys.length() != 0 {

        if isIntialJob || conMgr.getAvailableWorkers() > 0 {
            string key = keys.remove(0);
            AnyOrError[] value = values.remove(0);

            if !isIntialJob {
                conMgr.allocateWorker();
            }

            future<()> serialWaiter = start prepareDataDrivenTest(testFunction, key, value, testType);

            if !testFunction.parallelizable {
                any _ = check wait serialWaiter;
            }

        }

        isIntialJob = false;
        conMgr.waitForWorkers();
    }

    conMgr.waitForWorkers();

    if !isIntialJob {
        conMgr.allocateWorker();
    }
}

function prepareDataDrivenTest(TestFunction testFunction, string key, AnyOrError[] value, TestType testType) {
    boolean beforeFailed = executeBeforeFunction(testFunction);
    if (beforeFailed) {
        lock {
            reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        }
    } else {
        executeDataDrivenTest(testFunction, key, testType, value);
        var _ = executeAfterFunction(testFunction);
    }
    conMgr.releaseWorker();
}

function executeDataDrivenTest(TestFunction testFunction, string suffix, TestType testType, AnyOrError[] params) {
    if (skipDataDrivenTest(testFunction, suffix, testType)) {
        return;
    }

    ExecutionError|boolean err = executeTestFunction(testFunction, suffix, testType, params);
    if err is ExecutionError {
        lock {
            reportData.onFailed(name = testFunction.name, suffix = suffix, message = "[fail data provider for the function " + testFunction.name
                + "]\n" + getErrorMessage(err), testType = testType);
            println("\n" + testFunction.name + ":" + suffix + " has failed.\n");
            enableExit();
        }
    }
}

function executeNonDataDrivenTest(TestFunction testFunction) returns boolean {
    boolean failed = false;
    boolean beforeFailed = executeBeforeFunction(testFunction);
    if (beforeFailed) {
        testFunction.skip = true;
        lock {
            reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunction));
        }
        return true;
    }
    ExecutionError|boolean output = executeTestFunction(testFunction, "", GENERAL_TEST);
    if output is ExecutionError {
        failed = true;
        lock {
            reportData.onFailed(name = testFunction.name, message = output.message(), testType = GENERAL_TEST);
            println("\n" + testFunction.name + " has failed.\n");
        }
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

function executeBeforeSuiteFunctions() {
    ExecutionError? err = executeFunctions(beforeSuiteRegistry.getFunctions());
    if err is ExecutionError {
        enableShouldSkip();
        shouldAfterSuiteSkip = true;
        enableExit();
        printExecutionError(err, "before test suite function");
    }
}

function executeAfterSuiteFunctions() {
    ExecutionError? err = executeFunctions(afterSuiteRegistry.getFunctions(), shouldAfterSuiteSkip);
    if err is ExecutionError {
        enableExit();
        printExecutionError(err, "after test suite function");
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

function executeAfterEachFunctions() {
    ExecutionError? err = executeFunctions(afterEachRegistry.getFunctions(), getShouldSkip());
    if err is ExecutionError {
        enableShouldSkip();
        enableExit();
        printExecutionError(err, "after each test function for the test");
    }
}

function executeBeforeFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if testFunction.before is function && !getShouldSkip() && !testFunction.skip {
        ExecutionError? err = executeFunction(<function>testFunction.before);
        if err is ExecutionError {
            enableExit();
            printExecutionError(err, "before test function for the test");
            failed = true;
        }
    }
    return failed;
}

function executeAfterFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if testFunction.after is function && !getShouldSkip() && !testFunction.skip {
        ExecutionError? err = executeFunction(<function>testFunction.after);
        if err is ExecutionError {
            enableExit();
            printExecutionError(err, "after test function for the test");
            failed = true;
        }
    }
    return failed;
}

function executeBeforeGroupFunctions(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted('group) {
            ExecutionError? err = executeFunctions(beforeGroupFunctions, getShouldSkip());
            if err is ExecutionError {
                testFunction.skip = true;
                groupStatusRegistry.setSkipAfterGroup('group);
                enableExit();
                printExecutionError(err, "before test group function for the test");
            }
        }
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

function printExecutionError(ExecutionError err, string functionSuffix)
    => println("\t[fail] " + err.detail().functionName + "[" + functionSuffix + "]" + ":\n\t    " + formatFailedError(err.message(), 2));

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
        enableExit();
        lock {
            reportData.onFailed(name = testFunction.name, suffix = suffix, message = getErrorMessage(output), testType = testType);
            println("\n" + testFunction.name + ":" + suffix + " has failed\n");
        }
        return true;
    } else if output is any {
        lock {
            reportData.onPassed(name = testFunction.name, suffix = suffix, testType = testType);
        }
        return false;
    } else {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction.name);
    }
}

function executeFunction(TestFunction|function testFunction) returns ExecutionError? {
    any|error output = trap function:call(testFunction is function ? testFunction : testFunction.executableFunction);
    if output is error {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction is function ? "" : testFunction.name);
    }
}

function getErrorMessage(error err) returns string {
    string message = err.toBalString();

    string accumulatedTrace = "";
    foreach langError:StackFrame stackFrame in err.stackTrace() {
        accumulatedTrace = accumulatedTrace + "\t" + stackFrame.toString() + "\n";
    }
    return message + "\n" + accumulatedTrace;
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

        // if the dependsOnFunction is disabled by the user, throw an error
        // dependsOnTestFunction.config?.enable is used instead of dependsOnTestFunction.enable to ensure that
        // the user has deliberately passed enable=false
        boolean? dependentEnabled = dependsOnTestFunction.config?.enable;
        if dependentEnabled != () && !dependentEnabled {
            string errMsg = string `error: Test [${testFunction.name}] depends on function [${dependsOnTestFunction.name}], `
            + string `but it is either disabled or not included.`;
            return error(errMsg);
        }

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
        if (dependent.enabled) {
            return true;
        }
        dependent.dependents.forEach((superDependent) => queue.push(superDependent));
    }
    return nestedEnabledDependentsAvailable(queue);
}

isolated function enableShouldSkip() {
    lock {
        shouldSkip = true;
    }
}

isolated function getShouldSkip() returns boolean {
    lock {
        return shouldSkip;
    }
}

isolated function enableExit() {
    lock {
        exitCode = 1;
    }
}

