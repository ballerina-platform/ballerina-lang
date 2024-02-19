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

isolated boolean shouldSkip = false;
boolean shouldAfterSuiteSkip = false;
isolated int exitCode = 0;
isolated final ConcurrentExecutionManager conMgr = new;
map<DataProviderReturnType?> dataDrivenTestParams = {};

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
        if !testFunction.serialExecution {
            conMgr.addInitialParallelTest(testFunction);
            continue;
        }
        conMgr.addInitialSerialTest(testFunction);
    }
    while !conMgr.isExecutionDone() {
        if conMgr.getSerialQueueLength() != 0 && conMgr.countTestInExecution() == 0 {
            TestFunction testFunction = conMgr.getSerialTest();
            conMgr.addTestInExecution(testFunction);
            executeTest(testFunction);
        } else if conMgr.getParallelQueueLength() != 0 {
            TestFunction testFunction = conMgr.getParallelTest();
            conMgr.addTestInExecution(testFunction);
            DataProviderReturnType? testFunctionArgs = dataDrivenTestParams[testFunction.name];
            _ = start executeTestIso(testFunction, testFunctionArgs);
        }
        conMgr.populateExecutionQueues();
    }
    println(string `${"\n"}${"\t"}${"\t"}Test execution time : ${currentTimeInMillis() - startTime}ms${"\n"}`);
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

function orderTests() returns error? {
    string[] descendants = [];
    foreach TestFunction testFunction in testRegistry.getDependentFunctions() {
        if !conMgr.isVisited(testFunction.name) && conMgr.isEnabled(testFunction.name) {
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
        conMgr.addDependent(dependsOnTestFunction.name, testFunction);

        // Contains cyclic dependencies
        int? startIndex = descendants.indexOf(dependsOnTestFunction.name);
        if startIndex is int {
            string[] newCycle = descendants.slice(startIndex);
            newCycle.push(dependsOnTestFunction.name);
            return error("Cyclic test dependencies detected: " + string:'join(" -> ", ...newCycle));
        } else if !conMgr.isVisited(dependsOnTestFunction.name) {
            check restructureTest(dependsOnTestFunction, descendants);
        }
    }
    conMgr.setEnabled(testFunction.name);
    conMgr.setVisited(testFunction.name);
    _ = descendants.pop();
}

isolated function printExecutionError(ExecutionError err, string functionSuffix)
    => println("\t[fail] " + err.detail().functionName + "[" + functionSuffix + "]" + ":\n\t    " + formatFailedError(err.message(), 2));

isolated function getErrorMessage(error err) returns string {
    string message = err.toBalString();
    string accumulatedTrace = "";
    foreach langError:StackFrame stackFrame in err.stackTrace() {
        accumulatedTrace = accumulatedTrace + "\t" + stackFrame.toString() + "\n";
    }
    return message + "\n" + accumulatedTrace;
}

isolated function getTestType(DataProviderReturnType? params) returns TestType {
    if (params is map<AnyOrError[]>) {
        return DATA_DRIVEN_MAP_OF_TUPLE;
    } else if (params is AnyOrError[][]) {
        return DATA_DRIVEN_TUPLE_OF_TUPLE;
    }
    return GENERAL_TEST;
}

isolated function nestedEnabledDependentsAvailable(TestFunction[] dependents) returns boolean {
    if (dependents.length() == 0) {
        return false;
    }
    TestFunction[] queue = [];
    foreach TestFunction dependent in dependents {
        if (conMgr.isEnabled(dependent.name)) {
            return true;
        }
        foreach TestFunction superDependent in conMgr.getDependents(dependent.name) {
            queue.push(superDependent);
        }
    }
    return nestedEnabledDependentsAvailable(queue);
}

isolated function isDataDrivenTest(DataProviderReturnType? params) returns boolean =>
        params is map<AnyOrError[]> || params is AnyOrError[][];

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

isolated function isTestReadyToExecute(TestFunction testFunction, DataProviderReturnType? testFunctionArgs) returns boolean {
    if !conMgr.isEnabled(testFunction.name) {
        conMgr.setExecutionSuspended(testFunction.name);
        return false;
    }
    error? diagnoseError = testFunction.diagnostics;
    if diagnoseError is error {
        reportData.onFailed(name = testFunction.name, message = diagnoseError.message(), testType = getTestType(testFunctionArgs));
        println(string `${"\n"}${testFunction.name} has failed.${"\n"}`);
        enableExit();
        conMgr.setExecutionSuspended(testFunction.name);
        return false;
    }
    return true;
}

isolated function finishTestExecution(TestFunction testFunction, boolean shouldSkipDependents) {
    if shouldSkipDependents {
        conMgr.getDependents(testFunction.name).forEach(isolated function(TestFunction dependent) {
            conMgr.setSkip(dependent.name);
        });
    }
    conMgr.setExecutionCompleted(testFunction.name);
}

isolated function handleBeforeGroupOutput(TestFunction testFunction, string 'group, ExecutionError? err) {
    if err is ExecutionError {
        conMgr.setSkip(testFunction.name);
        groupStatusRegistry.setSkipAfterGroup('group);
        enableExit();
        printExecutionError(err, "before test group function for the test");
    }
}

isolated function handleBeforeEachOutput(ExecutionError? err) {
    if err is ExecutionError {
        enableShouldSkip();
        enableExit();
        printExecutionError(err, "before each test function for the test");
    }
}

isolated function handleNonDataDrivenTestOutput(TestFunction testFunction, ExecutionError|boolean output) returns boolean {
    boolean failed = false;
    if output is ExecutionError {
        failed = true;
        reportData.onFailed(name = testFunction.name, message = output.message(), testType = GENERAL_TEST);
        println(string `${"\n"}${testFunction.name} has failed.${"\n"}`);
    } else if output {
        failed = true;
    }
    return failed;
}

isolated function handleAfterEachOutput(ExecutionError? err) {
    if err is ExecutionError {
        enableShouldSkip();
        enableExit();
        printExecutionError(err, "after each test function for the test");
    }
}

isolated function handleAfterGroupOutput(ExecutionError? err) {
    if err is ExecutionError {
        enableExit();
        printExecutionError(err, "after test group function for the test");
    }
}

isolated function handleDataDrivenTestOutput(ExecutionError|boolean err, TestFunction testFunction, string suffix,
        TestType testType) {
    if err is ExecutionError {
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = "[fail data provider for the function " + testFunction.name
                + "]\n" + getErrorMessage(err), testType = testType);
        println(string `${"\n"}${testFunction.name}:${suffix} has failed.${"\n"}`);
        enableExit();
    }
}

isolated function handleBeforeFunctionOutput(ExecutionError? err) returns boolean {
    if err is ExecutionError {
        enableExit();
        printExecutionError(err, "before test function for the test");
        return true;
    }
    return false;
}

isolated function handleAfterFunctionOutput(ExecutionError? err) returns boolean {
    if err is ExecutionError {
        enableExit();
        printExecutionError(err, "after test function for the test");
        return true;
    }
    return false;
}

isolated function handleTestFuncOutput(any|error output, TestFunction testFunction, string suffix, TestType testType) returns ExecutionError|boolean {
    if output is TestError {
        enableExit();
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = getErrorMessage(output), testType = testType);
        println(string `${"\n"}${testFunction.name}:${suffix} has failed.${"\n"}`);
        return true;
    } else if output is any {
        reportData.onPassed(name = testFunction.name, suffix = suffix, testType = testType);
        return false;
    } else {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction.name);
    }
}

isolated function prepareDataSet(DataProviderReturnType? testFunctionArgs, string[] keys,
        AnyOrError[][] values) returns TestType {
    TestType testType = DATA_DRIVEN_MAP_OF_TUPLE;
    if testFunctionArgs is map<AnyOrError[]> {
        foreach [string, AnyOrError[]] entry in testFunctionArgs.entries() {
            keys.push(entry[0]);
            values.push(entry[1]);
        }
    } else if testFunctionArgs is AnyOrError[][] {
        testType = DATA_DRIVEN_TUPLE_OF_TUPLE;
        int i = 0;
        foreach AnyOrError[] entry in testFunctionArgs {
            keys.push(i.toString());
            values.push(entry);
            i += 1;
        }
    }
    return testType;
}

isolated function skipDataDrivenTest(TestFunction testFunction, string suffix, TestType testType) returns boolean {
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

isolated function isBeforeFuncConditionMet(TestFunction testFunction) returns boolean =>
                testFunction.before is function && !getShouldSkip() && !conMgr.isSkip(testFunction.name);

isolated function isAfterFuncConditionMet(TestFunction testFunction) returns boolean =>
                testFunction.after is function && !getShouldSkip() && !conMgr.isSkip(testFunction.name);

isolated function isSkipFunction(TestFunction testFunction) returns boolean =>
                conMgr.isSkip(testFunction.name) || getShouldSkip();
