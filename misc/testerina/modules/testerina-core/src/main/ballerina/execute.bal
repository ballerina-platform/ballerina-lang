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
final ExecutionManager executionManager = new;
map<DataProviderReturnType?> dataDrivenTestParams = {};
decimal executionTime = 0;

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
            println(string `${"\n"}${"\t"}${"\t"}Test execution time : ${executionTime / 1000}s`);
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
            executionManager.addInitialParallelTest(testFunction);
            continue;
        }
        executionManager.addInitialSerialTest(testFunction);
    }
    while !executionManager.isExecutionDone() {
        if executionManager.getSerialQueueLength() != 0 && executionManager.countTestInExecution() == 0 {
            TestFunction testFunction = executionManager.getSerialTest();
            executionManager.addTestInExecution(testFunction);
            executeTest(testFunction);
        } else if executionManager.getParallelQueueLength() != 0 {
            TestFunction testFunction = executionManager.getParallelTest();
            executionManager.addTestInExecution(testFunction);
            DataProviderReturnType? testFunctionArgs = dataDrivenTestParams[testFunction.name];
            _ = start executeTestIsolated(testFunction, testFunctionArgs);
        }
        executionManager.populateExecutionQueues();
    }
    executionTime = currentTimeInMillis() - startTime;
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
    from TestFunction testFunction in testRegistry.getDependentFunctions()
    where !executionManager.isVisited(testFunction.name) && executionManager.isEnabled(testFunction.name)
    do {
        check restructureTest(testFunction, descendants);
    };
}

function restructureTest(TestFunction testFunction, string[] descendants) returns error? {
    descendants.push(testFunction.name);
    foreach function dependsOnFunction in testFunction.dependsOn {
        TestFunction dependsOnTestFunction = check testRegistry.getTestFunction(dependsOnFunction);

        // if the dependsOnFunction is disabled by the user, throw an error
        // dependsOnTestFunction.config?.enable is used instead of dependsOnTestFunction.enable to ensure that
        // the user has deliberately passed enable=false
        boolean? dependentEnabled = dependsOnTestFunction.config?.enable;
        if dependentEnabled == false {
            string errMsg = string `error: Test [${testFunction.name}] depends on function` +
            string ` [${dependsOnTestFunction.name}], but it is either disabled or not included.`;
            return error(errMsg);
        }
        executionManager.addDependent(dependsOnTestFunction.name, testFunction);

        // Contains cyclic dependencies
        int? startIndex = descendants.indexOf(dependsOnTestFunction.name);
        if startIndex is int {
            string[] newCycle = descendants.slice(startIndex);
            newCycle.push(dependsOnTestFunction.name);
            return error("Cyclic test dependencies detected: " + string:'join(" -> ", ...newCycle));
        } else if !executionManager.isVisited(dependsOnTestFunction.name) {
            check restructureTest(dependsOnTestFunction, descendants);
        }
    }
    executionManager.setEnabled(testFunction.name);
    executionManager.setVisited(testFunction.name);
    _ = descendants.pop();
}

isolated function printExecutionError(ExecutionError err, string functionSuffix) {
    println("\t[fail] " + err.detail().functionName + "[" + functionSuffix + "]" + ":\n\t    " +
    formatFailedError(err.message(), 2));
}

isolated function getErrorMessage(error err) returns string {
    string message = err.toBalString();
    string accumulatedTrace = "";
    from langError:StackFrame stackFrame in err.stackTrace()
    do {
        accumulatedTrace = accumulatedTrace + "\t" + stackFrame.toString() + "\n";
    };
    return message + "\n" + accumulatedTrace;
}

isolated function getTestType(DataProviderReturnType? params) returns TestType {
    if params is map<AnyOrError[]> {
        return DATA_DRIVEN_MAP_OF_TUPLE;
    }
    if params is AnyOrError[][] {
        return DATA_DRIVEN_TUPLE_OF_TUPLE;
    }
    return GENERAL_TEST;
}

isolated function nestedEnabledDependentsAvailable(TestFunction[] dependents) returns boolean {
    if dependents.length() == 0 {
        return false;
    }
    TestFunction[] queue = [];
    foreach TestFunction dependent in dependents {
        if executionManager.isEnabled(dependent.name) {
            return true;
        }
        foreach TestFunction superDependent in executionManager.getDependents(dependent.name) {
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
    if !executionManager.isEnabled(testFunction.name) {
        executionManager.setExecutionSuspended(testFunction.name);
        return false;
    }
    error? diagnoseError = testFunction.diagnostics;
    if diagnoseError is error {
        reportData.onFailed(name = testFunction.name, message = diagnoseError.message(), testType =
        getTestType(testFunctionArgs));
        println(string `${"\n\t"}${testFunction.name} has failed.${"\n"}`);
        enableExit();
        executionManager.setExecutionSuspended(testFunction.name);
        return false;
    }
    return true;
}

isolated function finishTestExecution(TestFunction testFunction, boolean shouldSkipDependents) {
    if shouldSkipDependents {
        executionManager.getDependents(testFunction.name).forEach(isolated function(TestFunction dependent) {
            executionManager.setSkip(dependent.name);
        });
    }
    executionManager.setExecutionCompleted(testFunction.name);
}

isolated function handleBeforeGroupOutput(TestFunction testFunction, string 'group, ExecutionError? err) {
    if err is ExecutionError {
        executionManager.setSkip(testFunction.name);
        groupStatusRegistry.setSkipAfterGroup('group);
        enableExit();
        printExecutionError(err, "before groups function for the test");
    }
}

isolated function handleBeforeEachOutput(ExecutionError? err) {
    if err is ExecutionError {
        enableShouldSkip();
        enableExit();
        printExecutionError(err, "before each function for the test");
    }
}

isolated function handleNonDataDrivenTestOutput(TestFunction testFunction, ExecutionError|boolean output) returns boolean {
    boolean failed = false;
    if output is ExecutionError {
        failed = true;
        reportData.onFailed(name = testFunction.name, message = output.message(), testType = GENERAL_TEST);
        println(string `${"\n\t"}${testFunction.name} has failed.${"\n"}`);
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
        reportData.onFailed(name = testFunction.name, suffix = suffix, message =
        string `[fail data provider for the function ${testFunction.name}]${"\n"} ${getErrorMessage(err)}`,
        testType = testType);
        println(string `${"\n\t"}${testFunction.name}:${suffix} has failed.${"\n"}`);
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

isolated function handleTestFuncOutput(any|error output, TestFunction testFunction, string suffix, TestType testType)
returns ExecutionError|boolean {
    if output is TestError {
        enableExit();
        reportData.onFailed(name = testFunction.name, suffix = suffix, message = getErrorMessage(output),
        testType = testType);
        println(string `${"\n\t"}${testFunction.name}:${suffix} has failed.${"\n"}`);
        return true;
    }
    if output is any {
        reportData.onPassed(name = testFunction.name, suffix = suffix, testType = testType);
        return false;
    }
    enableExit();
    return error(getErrorMessage(output), functionName = testFunction.name);
}

isolated function prepareDataSet(DataProviderReturnType? testFunctionArgs, string[] keys,
        AnyOrError[][] values) returns TestType {
    TestType testType = DATA_DRIVEN_MAP_OF_TUPLE;
    if testFunctionArgs is map<AnyOrError[]> {
        foreach [string, AnyOrError[]] [k, v] in testFunctionArgs.entries() {
            keys.push(k);
            values.push(v);
        }
    } else if testFunctionArgs is AnyOrError[][] {
        testType = DATA_DRIVEN_TUPLE_OF_TUPLE;
        foreach AnyOrError[] [k, v] in testFunctionArgs.enumerate() {
            keys.push(k.toBalString());
            values.push(v);
        }
    }
    return testType;
}

isolated function skipDataDrivenTest(TestFunction testFunction, string suffix, TestType testType) returns boolean {
    string functionName = testFunction.name;
    if !testOptions.getHasFilteredTests() {
        return false;
    }
    TestFunction[] dependents = executionManager.getDependents(functionName);

    // if a dependent in a below level is enabled, this test should run
    if dependents.length() > 0 && nestedEnabledDependentsAvailable(dependents) {
        return false;
    }
    string functionKey = functionName;

    // check if prefix matches directly
    boolean prefixMatch = testOptions.isFilterSubTestsContains(functionName);

    // if prefix matches to a wildcard
    if !prefixMatch && hasTest(functionName) {

        // get the matching wildcard
        prefixMatch = true;
        foreach string filter in testOptions.getFilterTests() {
            if filter.includes(WILDCARD) {
                boolean|error wildCardMatch = matchWildcard(functionKey, filter);
                if wildCardMatch is boolean && wildCardMatch && matchModuleName(filter) {
                    functionKey = filter;
                    break;
                }
            }
        }
    }

    // check if no filterSubTests found for a given prefix
    boolean suffixMatch = !testOptions.isFilterSubTestsContains(functionKey);

    // if a subtest is found specified
    if !suffixMatch {
        string[] subTests = testOptions.getFilterSubTest(functionKey);
        foreach string subFilter in subTests {
            string updatedSubFilter = subFilter;
            if testType == DATA_DRIVEN_MAP_OF_TUPLE {
                if subFilter.startsWith(SINGLE_QUOTE) && subFilter.endsWith(SINGLE_QUOTE) {
                    updatedSubFilter = subFilter.substring(1, subFilter.length() - 1);
                }
            }
            string|error decodedSubFilter = escapeSpecialCharacters(updatedSubFilter);
            updatedSubFilter = decodedSubFilter is string ? decodedSubFilter : updatedSubFilter;
            string|error decodedSuffix = escapeSpecialCharacters(suffix);
            string updatedSuffix = decodedSuffix is string ? decodedSuffix : suffix;

            boolean wildCardMatchBoolean = false;
            if updatedSubFilter.includes(WILDCARD) {
                boolean|error wildCardMatch = matchWildcard(updatedSuffix, updatedSubFilter);
                wildCardMatchBoolean = wildCardMatch is boolean && wildCardMatch;
            }
            if (updatedSubFilter == updatedSuffix) || wildCardMatchBoolean {
                suffixMatch = true;
                break;
            }
        }
    }

    // do not skip iff both matches
    return !(prefixMatch && suffixMatch);
}

isolated function isBeforeFuncConditionMet(TestFunction testFunction) returns boolean =>
                testFunction.before is function && !getShouldSkip() && !executionManager.isSkip(testFunction.name);

isolated function isAfterFuncConditionMet(TestFunction testFunction) returns boolean =>
                testFunction.after is function && !getShouldSkip() && !executionManager.isSkip(testFunction.name);

isolated function isSkipFunction(TestFunction testFunction) returns boolean =>
                executionManager.isSkip(testFunction.name) || getShouldSkip();
