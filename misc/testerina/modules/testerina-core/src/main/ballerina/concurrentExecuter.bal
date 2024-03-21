// Copyright (c) 2024 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

isolated function executeTestIsolated(TestFunction testFunction, DataProviderReturnType? testFunctionArgs) {
    if !isTestReadyToExecute(testFunction, testFunctionArgs) {
        return;
    }
    executeBeforeGroupFunctionsIsolated(testFunction);
    executeBeforeEachFunctionsIsolated();
    boolean shouldSkipDependents = false;
    if !isSkipFunction(testFunction) {
        if isDataDrivenTest(testFunctionArgs) {
            executeDataDrivenTestSetIsolated(testFunction, testFunctionArgs);
        } else {
            shouldSkipDependents = executeNonDataDrivenTestIsolated(testFunction, testFunctionArgs);
        }
    } else {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunctionArgs));
        shouldSkipDependents = true;
    }
    testFunction.groups.forEach('group => groupStatusRegistry.incrementExecutedTest('group));
    executeAfterEachFunctionsIsolated();
    executeAfterGroupFunctionsIsolated(testFunction);
    finishTestExecution(testFunction, shouldSkipDependents);
}

isolated function executeBeforeGroupFunctionsIsolated(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted('group) {
            handleBeforeGroupOutput(testFunction, 'group, executeFunctionsIsolated(beforeGroupFunctions,
            getShouldSkip()));
        }
    }
}

isolated function executeBeforeEachFunctionsIsolated() =>
    handleBeforeEachOutput(executeFunctionsIsolated(beforeEachRegistry.getFunctions(), getShouldSkip()));

isolated function executeDataDrivenTestSetIsolated(TestFunction testFunction,
        DataProviderReturnType? testFunctionArgs) {
    string[] keys = [];
    AnyOrError[][] values = [];
    TestType testType = prepareDataSet(testFunctionArgs, keys, values);
    map<future> futuresMap = {};
    while keys.length() != 0 {
        string key = keys.remove(0);
        AnyOrError[] value = values.remove(0);
        final readonly & readonly[] readOnlyVal = from any|error item in value
            where item is readonly
            select item;
        if readOnlyVal.length() != value.length() {
            reportData.onFailed(name = testFunction.name, suffix = key, message =
            string `[fail data provider for the function ${testFunction.name}]${"\n"}` +
            string ` Data provider returned non-readonly values`, testType = testType);
            println(string `${"\n\t"}${testFunction.name}:${key} has failed.${"\n"}`);
            enableExit();
        }
        future<()> futureResult = start prepareDataDrivenTestIsolated(testFunction, key, readOnlyVal, testType);
        futuresMap[key] = futureResult;
    }
    foreach [string, future<any|error>] futureResult in futuresMap.entries() {
        string suffix = futureResult[0];
        any|error parallelDataProviderResult = wait futureResult[1];
        if parallelDataProviderResult is error {
            reportData.onFailed(name = testFunction.name, suffix = suffix, message =
            string `[fail data provider for the function ` +
            string `${testFunction.name}]${"\n"} ${getErrorMessage(parallelDataProviderResult)}`, testType = testType);
            println(string `${"\n\t"}${testFunction.name}:${suffix} has failed.${"\n"}`);
            enableExit();
        }
    }
}

isolated function executeNonDataDrivenTestIsolated(TestFunction testFunction,
        DataProviderReturnType? testFunctionArgs) returns boolean {
    if executeBeforeFunctionIsolated(testFunction) {
        executionManager.setSkip(testFunction.name);
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunctionArgs));
        return true;
    }
    boolean failed = handleNonDataDrivenTestOutput(testFunction, executeTestFunctionIsolated(testFunction, "",
    GENERAL_TEST));
    if executeAfterFunctionIsolated(testFunction) {
        return true;
    }
    return failed;
}

isolated function executeAfterEachFunctionsIsolated() =>
    handleAfterEachOutput(executeFunctionsIsolated(afterEachRegistry.getFunctions(), getShouldSkip()));

isolated function executeAfterGroupFunctionsIsolated(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? afterGroupFunctions = afterGroupsRegistry.getFunctions('group);
        if afterGroupFunctions != () && groupStatusRegistry.lastExecuted('group) {
            ExecutionError? err = executeFunctionsIsolated(afterGroupFunctions,
                    getShouldSkip() || groupStatusRegistry.getSkipAfterGroup('group));
            handleAfterGroupOutput(err);
        }
    }
}

isolated function executeFunctionsIsolated(TestFunction[] testFunctions, boolean skip = false)
returns ExecutionError? {
    foreach TestFunction testFunction in testFunctions {
        if !skip || testFunction.alwaysRun {
            check executeFunctionIsolated(testFunction);
        }
    }
}

isolated function prepareDataDrivenTestIsolated(TestFunction testFunction, string key, AnyOrError[] value,
        TestType testType) {
    if executeBeforeFunctionIsolated(testFunction) {
        reportData.onSkipped(name = testFunction.name, testType = testType);
    } else {
        executeDataDrivenTestIsolated(testFunction, key, testType, value);
        _ = executeAfterFunctionIsolated(testFunction);
    }
}

isolated function executeDataDrivenTestIsolated(TestFunction testFunction, string suffix, TestType testType,
        AnyOrError[] params) {
    if skipDataDrivenTest(testFunction, suffix, testType) {
        return;
    }
    ExecutionError|boolean err = executeTestFunctionIsolated(testFunction, suffix, testType, params);
    handleDataDrivenTestOutput(err, testFunction, suffix, testType);
}

isolated function executeBeforeFunctionIsolated(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if isBeforeFuncConditionMet(testFunction) {
        failed = handleBeforeFunctionOutput(executeFunctionIsolated(<function>testFunction.before));
    }
    return failed;
}

isolated function executeTestFunctionIsolated(TestFunction testFunction, string suffix, TestType testType,
        AnyOrError[]? params = ()) returns ExecutionError|boolean {
    isolated function isolatedTestFunction = <isolated function>testFunction.executableFunction;
    any|error output = params == () ? trap function:call(isolatedTestFunction)
        : trap function:call(isolatedTestFunction, ...params);
    return handleTestFuncOutput(output, testFunction, suffix, testType);
}

isolated function executeAfterFunctionIsolated(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if isAfterFuncConditionMet(testFunction) {
        failed = handleAfterFunctionOutput(executeFunctionIsolated(<function>testFunction.after));
    }
    return failed;
}

isolated function executeFunctionIsolated(TestFunction|function testFunction) returns ExecutionError? {
    isolated function isolatedTestFunction = <isolated function>(testFunction is function ? testFunction :
        testFunction.executableFunction);
    // casting is done outside the function to avoid the error of casting inside the function and trapping it.
    any|error output = trap function:call(isolatedTestFunction);
    if output is error {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction is function ? "" : testFunction.name);
    }
}
