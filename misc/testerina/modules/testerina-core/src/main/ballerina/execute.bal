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
TestFunction[] parallelTestExecutionList = [];
TestFunction[] serialTestExecutionList = [];
isolated int unAllocatedTestWorkers = 1;

public function startSuite() {
    // exit if setTestOptions has failed
    exitOnError();
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
    exitOnError();
}

function exitOnError() {
    lock {
        if exitCode > 0 {
            panic (error(""));
        }
    }
}

function executeTests() returns error? {
    decimal startTime = currentTimeInMillis();
    // Add intial independant tests to the execution queue based on parallelizable condition
    foreach TestFunction testFunction in testRegistry.getFunctions() {
        if testFunction.parallelizable {
            parallelTestExecutionList.push(testFunction);
        } else {
            serialTestExecutionList.push(testFunction);
        }

    }

    int paralalTestExecutionListLength = parallelTestExecutionList.length();
    int serialTestExecutionListLength = serialTestExecutionList.length();

    while true {

        TestFunction? testFunction = ();
        lock {
            paralalTestExecutionListLength = parallelTestExecutionList.length();
            serialTestExecutionListLength = serialTestExecutionList.length();
        }
        //Exit from the loop if there are no tests to execute and all jobs are released  
        if (paralalTestExecutionListLength == 0 && getAvailableWorkerCount() == testWorkers
            && serialTestExecutionListLength == 0) {
            break;
        }

        //Execute tests if there are available workers
        if (getAvailableWorkerCount() != 0) {

            //If there are no tests to execute, wait for tests to be added to the execution queue
            if paralalTestExecutionListLength == 0 && serialTestExecutionListLength == 0 {
                runtime:sleep(0.0001);
                continue;

            }

            //Execute serial tests if there are no test in execution process
            if (serialTestExecutionListLength != 0 && getAvailableWorkerCount() == testWorkers) {
                lock {
                    testFunction = serialTestExecutionList.remove(0);
                }
                // wait until the test is complete execution
                if testFunction is TestFunction {
                    allocateWorker();
                    future<error?> serialWaiter = start executeTest(testFunction);
                    any _ = check wait serialWaiter;
                }

                // Execute parallel tests if there are no serial tests in execution queue
            } else if paralalTestExecutionListLength != 0 && serialTestExecutionListLength == 0 {
                lock {
                    testFunction = parallelTestExecutionList.remove(0);
                }

                // wait for the data driven tests to allocate workers
                if testFunction is TestFunction {
                    allocateWorker();
                    future<(error?)> parallelWaiter = start executeTest(testFunction);
                    if isDataDrivenTest(testFunction) {
                        any _ = check wait parallelWaiter;
                    }
                }

            }
        }
        runtime:sleep(0.0001);
    }
    println("\n\t\tTest execution time :" + (currentTimeInMillis() - startTime).toString() + "ms\n");
}

function executeTest(TestFunction testFunction) returns error? {
    // release worker if test is not enabled
    if !testFunction.enabled {
        releaseWorker();
        return;
    }

    // relese worker if test has diagnostic errors
    error? diagnoseError = testFunction.diagnostics;
    if diagnoseError is error {
        lock {
            reportData.onFailed(name = testFunction.name, message = diagnoseError.message(), testType = getTestType(testFunction));
            println("\n****************************************************\n" + testFunction.name + " has failed.\n****************************************************\n");
        }
        enableExit();
        releaseWorker();
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

    testFunction.groups.forEach(group => groupStatusRegistry.incrementExecutedTest(group));
    executeAfterEachFunctions();
    executeAfterGroupFunctions(testFunction);

    if shouldSkipDependents {
        testFunction.dependents.forEach(function(TestFunction dependent) {
            lock {
                dependent.skip = true;
            }
        });
    }
    testFunction.dependents.forEach(dependent => checkExecutionReadiness(dependent));
    releaseWorker();
}

//Reduce the dependents count and add to the execution queue if all the dependencies are executed
function checkExecutionReadiness(TestFunction testFunction) {
    lock {
        testFunction.dependsOnCount -= 1;
        if testFunction.dependsOnCount == 0 && testFunction.isInExecutionQueue != true {
            testFunction.isInExecutionQueue = true;
            if testFunction.parallelizable {
                parallelTestExecutionList.push(testFunction);
            } else {
                serialTestExecutionList.push(testFunction);
            }
        }
    }
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

    while true {
        //Break if there is no keys to execute or all keys are executed
        if keys.length() == 0 {
            break;
        }

        //Reuse the already assigned worker for 1st data driven test or 
        // if there are available workers assign a worker for the next data driven test
        if isIntialJob || getAvailableWorkerCount() > 0 {
            string key = keys.remove(0);
            AnyOrError[] value = values.remove(0);

            // Allocate worker from 2nd data driven test onwards
            if !isIntialJob {
                allocateWorker();
            }

            future<()> serialWaiter = start prepareDataDrivenTest(testFunction, key, value, testType);

            //Wait for the test to complete if the test is not parallelizable
            if !testFunction.parallelizable {
                any _ = check wait serialWaiter;
            }

        }

        isIntialJob = false;

        //Wait until at least one worker is available
        if getAvailableWorkerCount() == 0 {
            runtime:sleep(0.0001);
            continue;
        }
        runtime:sleep(0.0001);
    }

    //Wait until at least one worker is available
    while true {
        if getAvailableWorkerCount() > 0 {
            break;
        }
        runtime:sleep(0.0001);
    }

    // Allocate the worker for the remaining processing of data driven test
    if !isIntialJob {
        allocateWorker();
    }
}

// Execute the data driven test and release the worker
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
    releaseWorker();
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
            println("\n****************************************************\n" + testFunction.name + ":" + suffix + " has failed.\n****************************************************\n");
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
            println("\n****************************************************\n" + testFunction.name + " has failed.\n****************************************************\n");
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
    foreach string group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions(group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted(group) {
            ExecutionError? err = executeFunctions(beforeGroupFunctions, getShouldSkip());
            if err is ExecutionError {
                testFunction.skip = true;
                groupStatusRegistry.setSkipAfterGroup(group);
                enableExit();
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
                getShouldSkip() || groupStatusRegistry.getSkipAfterGroup(group));
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
            println("\n****************************************************\n" + testFunction.name + ":" + suffix + " has failed.\n****************************************************\n");
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

isolated function allocateWorker() {
    lock {
        unAllocatedTestWorkers -= 1;
    }
}

isolated function releaseWorker() {
    lock {
        unAllocatedTestWorkers += 1;
    }
}

isolated function getAvailableWorkerCount() returns int {
    lock {
        return unAllocatedTestWorkers;
    }
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

