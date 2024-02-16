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

isolated function executeTestIso(TestFunction testFunction, DataProviderReturnType? testFunctionArgs) {
    if !isTestReadyToExecute(testFunction, testFunctionArgs) {
        return;
    }
    executeBeforeGroupFunctionsIso(testFunction);
    executeBeforeEachFunctionsIso();
    boolean shouldSkipDependents = false;
    if !isSkipFunction(testFunction) {
        if (isDataDrivenTest(testFunctionArgs)) {
            executeDataDrivenTestSetIso(testFunction, testFunctionArgs);
        } else {
            shouldSkipDependents = executeNonDataDrivenTestIso(testFunction, testFunctionArgs);
        }
    } else {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunctionArgs));
        shouldSkipDependents = true;
    }
    testFunction.groups.forEach('group => groupStatusRegistry.incrementExecutedTest('group));
    executeAfterEachFunctionsIso();
    executeAfterGroupFunctionsIso(testFunction);
    finishTestExecution(testFunction, shouldSkipDependents);
}

isolated function executeBeforeGroupFunctionsIso(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted('group) {
            handleBeforeGroupOutput(testFunction, 'group, executeFunctionsIso(beforeGroupFunctions, getShouldSkip()));
        }
    }
}

isolated function executeBeforeEachFunctionsIso() =>
    handleBeforeEachOutput(executeFunctionsIso(beforeEachRegistry.getFunctions(), getShouldSkip()));

isolated function executeDataDrivenTestSetIso(TestFunction testFunction, DataProviderReturnType? testFunctionArgs) {
    string[] keys = [];
    AnyOrError[][] values = [];
    TestType testType = prepareDataSet(testFunctionArgs, keys, values);
    map<future> futuresMap = {};
    while keys.length() != 0 {
        string key = keys.remove(0);
        final readonly & readonly[] readOnlyVal = from any|error item in values.remove(0)
            where item is readonly
            select item;
        future<()> futureResult = start prepareDataDrivenTestIso(testFunction, key, readOnlyVal, testType);
        futuresMap[key] = futureResult;
    }
    foreach [string, future<any|error>] futureResult in futuresMap.entries() {
        string suffix = futureResult[0];
        any|error parallelDataProviderResult = wait futureResult[1];
        if parallelDataProviderResult is error {
            reportData.onFailed(name = testFunction.name, suffix = suffix, message = "[fail data provider for the function " + testFunction.name
                + "]\n" + getErrorMessage(parallelDataProviderResult), testType = testType);
            println(string `${"\n"}${testFunction.name}:${suffix} has failed.${"\n"}`);
            enableExit();
        }
    }
}

isolated function executeNonDataDrivenTestIso(TestFunction testFunction, DataProviderReturnType? testFunctionArgs) returns boolean {
    if executeBeforeFunctionIso(testFunction) {
        conMgr.setSkip(testFunction.name);
        reportData.onSkipped(name = testFunction.name, testType = getTestType(testFunctionArgs));
        return true;
    }
    boolean failed = handleNonDataDrivenTestOutput(testFunction, executeTestFunctionIso(testFunction, "", GENERAL_TEST));
    if executeAfterFunctionIso(testFunction) {
        return true;
    }
    return failed;
}

isolated function executeAfterEachFunctionsIso() =>
    handleAfterEachOutput(executeFunctionsIso(afterEachRegistry.getFunctions(), getShouldSkip()));

isolated function executeAfterGroupFunctionsIso(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? afterGroupFunctions = afterGroupsRegistry.getFunctions('group);
        if afterGroupFunctions != () && groupStatusRegistry.lastExecuted('group) {
            ExecutionError? err = executeFunctionsIso(afterGroupFunctions,
                    getShouldSkip() || groupStatusRegistry.getSkipAfterGroup('group));
            handleAfterGroupOutput(err);
        }
    }
}

isolated function executeFunctionsIso(TestFunction[] testFunctions, boolean skip = false) returns ExecutionError? {
    foreach TestFunction testFunction in testFunctions {
        if !skip || testFunction.alwaysRun {
            check executeFunctionIso(testFunction);
        }
    }
}

isolated function prepareDataDrivenTestIso(TestFunction testFunction, string key, AnyOrError[] value, TestType testType) {
    if executeBeforeFunctionIso(testFunction) {
        reportData.onSkipped(name = testFunction.name, testType = testType);
    } else {
        executeDataDrivenTestIso(testFunction, key, testType, value);
        var _ = executeAfterFunctionIso(testFunction);
    }
}

isolated function executeDataDrivenTestIso(TestFunction testFunction, string suffix, TestType testType, AnyOrError[] params) {
    if (skipDataDrivenTest(testFunction, suffix, testType)) {
        return;
    }
    ExecutionError|boolean err = executeTestFunctionIso(testFunction, suffix, testType, params);
    handleDataDrivenTestOutput(err, testFunction, suffix, testType);
}

isolated function executeBeforeFunctionIso(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if isBeforeFuncConditionMet(testFunction) {
        failed = handleBeforeFunctionOutput(executeFunctionIso(<function>testFunction.before));
    }
    return failed;
}

isolated function executeTestFunctionIso(TestFunction testFunction, string suffix, TestType testType, AnyOrError[]? params = ()) returns ExecutionError|boolean {
    any|error output = params == () ? trap function:call(<isolated function>testFunction.executableFunction)
        : trap function:call(<isolated function>testFunction.executableFunction, ...params);
    return handleTestFuncOutput(output, testFunction, suffix, testType);
}

isolated function executeAfterFunctionIso(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if isAfterFuncConditionMet(testFunction) {
        failed = handleAfterFunctionOutput(executeFunctionIso(<function>testFunction.after));
    }
    return failed;
}

isolated function executeFunctionIso(TestFunction|function testFunction) returns ExecutionError? {
    any|error output = trap function:call(<isolated function>(testFunction is function ? testFunction : testFunction.executableFunction));
    if output is error {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction is function ? "" : testFunction.name);
    }
}
