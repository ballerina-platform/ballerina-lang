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

function executeTest(TestFunction testFunction) {
    if !isTestReadyToExecute(testFunction, dataDrivenTestParams[testFunction.name]) {
        return;
    }

    executeBeforeGroupFunctions(testFunction);
    executeBeforeEachFunctions();

    boolean shouldSkipDependents = false;
    if !isSkipFunction(testFunction) {
        if isDataDrivenTest(dataDrivenTestParams[testFunction.name]) {
            executeDataDrivenTestSet(testFunction);
        } else {
            shouldSkipDependents = executeNonDataDrivenTest(testFunction);
        }
    } else {
        reportData.onSkipped(name = testFunction.name, testType =
        getTestType(dataDrivenTestParams[testFunction.name]));
        shouldSkipDependents = true;
    }
    testFunction.groups.forEach('group => groupStatusRegistry.incrementExecutedTest('group));
    executeAfterEachFunctions();
    executeAfterGroupFunctions(testFunction);
    finishTestExecution(testFunction, shouldSkipDependents);
}

function executeBeforeGroupFunctions(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted('group) {
            handleBeforeGroupOutput(testFunction, 'group, executeFunctions(beforeGroupFunctions,
            getShouldSkip()));
        }
    }
}

function executeBeforeEachFunctions() =>
    handleBeforeEachOutput(executeFunctions(beforeEachRegistry.getFunctions(), getShouldSkip()));

function executeDataDrivenTestSet(TestFunction testFunction) {
    DataProviderReturnType? params = dataDrivenTestParams[testFunction.name];
    string[] keys = [];
    AnyOrError[][] values = [];
    TestType testType = prepareDataSet(params, keys, values);

    while keys.length() != 0 {
        string key = keys.remove(0);
        AnyOrError[] value = values.remove(0);
        prepareDataDrivenTest(testFunction, key, value, testType);
    }
}

function executeNonDataDrivenTest(TestFunction testFunction) returns boolean {
    if executeBeforeFunction(testFunction) {
        executionManager.setSkip(testFunction.name);
        reportData.onSkipped(name = testFunction.name, testType = getTestType(
            dataDrivenTestParams[testFunction.name]));
        return true;
    }
    boolean failed = handleNonDataDrivenTestOutput(testFunction, executeTestFunction(testFunction, "",
    GENERAL_TEST));
    if executeAfterFunction(testFunction) {
        return true;
    }
    return failed;
}

function executeAfterEachFunctions() =>
    handleAfterEachOutput(executeFunctions(afterEachRegistry.getFunctions(), getShouldSkip()));

function executeAfterGroupFunctions(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? afterGroupFunctions = afterGroupsRegistry.getFunctions('group);
        if afterGroupFunctions != () && groupStatusRegistry.lastExecuted('group) {
            ExecutionError? err = executeFunctions(afterGroupFunctions,
                    getShouldSkip() || groupStatusRegistry.getSkipAfterGroup('group));
            handleAfterGroupOutput(err);
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

function prepareDataDrivenTest(TestFunction testFunction, string key, AnyOrError[] value,
        TestType testType) {
    if executeBeforeFunction(testFunction) {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(
            dataDrivenTestParams[testFunction.name]));
    } else {
        executeDataDrivenTest(testFunction, key, testType, value);
        _ = executeAfterFunction(testFunction);
    }
}

function executeDataDrivenTest(TestFunction testFunction, string suffix, TestType testType,
        AnyOrError[] params) {
    if skipDataDrivenTest(testFunction, suffix, testType) {
        return;
    }
    ExecutionError|boolean err = executeTestFunction(testFunction, suffix, testType, params);
    handleDataDrivenTestOutput(err, testFunction, suffix, testType);
}

function executeBeforeFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if isBeforeFuncConditionMet(testFunction) {
        failed = handleBeforeFunctionOutput(executeFunction(<function>testFunction.before));
    }
    return failed;
}

function executeTestFunction(TestFunction testFunction, string suffix, TestType testType,
        AnyOrError[]? params = ()) returns ExecutionError|boolean {
    any|error output = params == () ? trap function:call(testFunction.executableFunction)
        : trap function:call(testFunction.executableFunction, ...params);
    return handleTestFuncOutput(output, testFunction, suffix, testType);
}

function executeAfterFunction(TestFunction testFunction) returns boolean {
    boolean failed = false;
    if isAfterFuncConditionMet(testFunction) {
        failed = handleAfterFunctionOutput(executeFunction(<function>testFunction.after));
    }
    return failed;
}

function executeFunction(TestFunction|function testFunction) returns ExecutionError? {
    any|error output = trap function:call(testFunction is function ? testFunction :
        testFunction.executableFunction);
    if output is error {
        enableExit();
        return error(getErrorMessage(output), functionName = testFunction is function ? "" :
            testFunction.name);
    }
}
