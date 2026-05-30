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
    if !isEvaluationTest(testFunction) {
        executeBeforeEachFunctionsIsolated();
    }
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
    if !isEvaluationTest(testFunction) {
        executeAfterEachFunctionsIsolated();
    }
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
    if isEvaluationTest(testFunction) {
        executeDataDrivenEvaluationIsolated(testFunction, testFunctionArgs);
        return;
    }

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

isolated function executeDataDrivenEvaluationIsolated(TestFunction testFunction,
        DataProviderReturnType? testFunctionArgs) {
    EvaluationConfig evalConfig = getEvalConfig(testFunction);
    EvaluationRunWithDataSet[] entries = [];

    string[] keys = [];
    AnyOrError[][] values = [];
    _ = prepareDataSet(testFunctionArgs, keys, values);

    foreach int run in 1 ... evalConfig.runs {
        executeBeforeEachFunctionsIsolated();
        if executeBeforeFunctionIsolated(testFunction) {
            executionManager.setSkip(testFunction.name);
            reportData.onSkipped(name = testFunction.name, testType = EVAL_TEST);
            return;
        }
        map<future> futures = {};
        foreach int i in 0 ..< keys.length() {
            string key = keys[i];
            AnyOrError[] valueSet = values[i];
            readonly & readonly[] readonlyValues = from any|error item in valueSet
                where item is readonly
                select item;

            if readonlyValues.length() != valueSet.length() {
                reportData.onFailed(name = testFunction.name,
                message = string `[fail data provider for the function ${testFunction.name}]${"\n"}` +
                    "data provider returned non-readonly values", testType = EVAL_TEST
                );
                enableExit();
                return;
            }
            futures[key] = start executeEvaluationIsolated(testFunction, readonlyValues);
        }

        int totalEntries = 0;
        int passedEntries = 0;
        map<EvaluationOutcome> outcomesMap = {};
        foreach [string, future<any|error>] entry in futures.entries() {
            any|error result = wait entry[1];
            if result is InvalidArgumentError && result.cause() is error {
                reportData.onFailed(name = testFunction.name,
                    message = string `[fail data provider for the function ${testFunction.name}]${"\n"}`
                    + getErrorMessage(<error>result.cause()), testType = EVAL_TEST
                );
                enableExit();
                return;
            } else if result is () {
                passedEntries += 1;
            }
            totalEntries += 1;
            string key = entry[0];
            outcomesMap[key] = {id: key, errorMessage: getErrorMessageFromResult(result)};
        }
        // Preserve the original key order when constructing evaluation outcomes
        EvaluationOutcome[] outcomes = from string key in keys
            select outcomesMap.get(key);

        if totalEntries == 0 {
            reportData.onFailed(name = testFunction.name,
                    message = string `[fail data provider for the function ${testFunction.name}]${"\n"}`
                    + "the data provider returned no data.", testType = EVAL_TEST);
            enableExit();
            return;
        }

        float passRate = <float>passedEntries / totalEntries;
        entries.push({id: run, outcomes: outcomes.cloneReadOnly(), passRate});
        _ = executeAfterFunctionIsolated(testFunction);
        executeAfterEachFunctionsIsolated();
    }

    float passRateSum = entries.'map(entry => entry.passRate)
        .reduce(isolated function(float total, float next) returns float => total + next, 0);
    float averagePassRate = passRateSum / evalConfig.runs;

    EvaluationSummary evaluationSummary = {
        targetPassRate: evalConfig.minPassRate,
        observedPassRate: averagePassRate,
        evaluationRuns: entries
    };
    if averagePassRate >= evalConfig.minPassRate {
        reportData.onPassed(name = testFunction.name,
        message = string `evaluation passed with an average pass rate of ${averagePassRate}`,
        evaluationSummary = evaluationSummary.cloneReadOnly(), testType = EVAL_TEST);
        return;
    }
    reportData.onFailed(name = testFunction.name,
    message = string `evaluation failed with an average pass rate of ${averagePassRate}`,
    evaluationSummary = evaluationSummary.cloneReadOnly(),
    testType = EVAL_TEST);
    enableExit();
}

isolated function executeNonDataDrivenTestIsolated(TestFunction testFunction,
        DataProviderReturnType? testFunctionArgs) returns boolean {
    if isEvaluationTest(testFunction) {
        return executeNonDataDrivenEvaluationIsolated(testFunction, testFunctionArgs);
    }
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

isolated function executeNonDataDrivenEvaluationIsolated(TestFunction testFunction,
        DataProviderReturnType? testFunctionArgs) returns boolean {
    EvaluationConfig evalConfig = getEvalConfig(testFunction);
    int runs = evalConfig.runs;
    float requiredConfidence = evalConfig.minPassRate;
    int passedIterations = 0;
    EvaluationRunWithoutDataSet[] entries = [];
    boolean[] afterFunctionResults = [];

    foreach int run in 1 ... runs {
        executeBeforeEachFunctionsIsolated();
        if executeBeforeFunctionIsolated(testFunction) {
            executionManager.setSkip(testFunction.name);
            reportData.onSkipped(name = testFunction.name, testType = EVAL_TEST);
            return true;
        }
        ExecutionError|TestError? result = executeEvaluationIsolated(testFunction);
        if result is InvalidArgumentError && result.cause() is error {
            reportData.onFailed(name = testFunction.name,
                message = string `[fail evaluation for the function ${testFunction.name}]${"\n"}`
                + getErrorMessage(<error>result.cause()), testType = EVAL_TEST
            );
            enableExit();
            return true;
        } else if result is () {
            passedIterations += 1;
        }
        entries.push({id: run, errorMessage: getErrorMessageFromResult(result)});
        boolean afterFunctionResult = executeAfterFunctionIsolated(testFunction);
        afterFunctionResults.push(afterFunctionResult);
        executeAfterEachFunctionsIsolated();
    }

    float passRate = <float>passedIterations / runs;
    EvaluationSummary evaluationSummary = {
        targetPassRate: evalConfig.minPassRate,
        observedPassRate: passRate,
        evaluationRuns: entries
    };
    if passRate >= requiredConfidence {
        reportData.onPassed(name = testFunction.name,
            message = string `evaluation passed with an average pass rate of ${passRate}`,
            evaluationSummary = evaluationSummary.cloneReadOnly(), testType = EVAL_TEST);
        return afterFunctionResults.some(res => res);
    }

    reportData.onFailed(name = testFunction.name,
        message = string `evaluation failed with an average pass rate of ${passRate}`,
        evaluationSummary = evaluationSummary.cloneReadOnly(), testType = EVAL_TEST);
    enableExit();
    return true;
}

isolated function isEvaluationTest(TestFunction testFunction) returns boolean {
    TestConfig? testConfig = testFunction.config;
    if testConfig is () {
        return false;
    }
    return testConfig.runs != 1 || testConfig.minPassRate != 1.0;
}

isolated function getEvalConfig(TestFunction testFunction) returns EvaluationConfig {
    if !isEvaluationTest(testFunction) {
        panic error("unable to obtain valid evaluation config");
    }
    TestConfig testConfig = <TestConfig>testFunction.config;
    return {runs: testConfig.runs, minPassRate: testConfig.minPassRate};
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

isolated function executeEvaluationIsolated(TestFunction testFunction, AnyOrError[]? params = ())
    returns ExecutionError|TestError? {
    isolated function isolatedTestFunction = <isolated function>testFunction.executableFunction;
    record {any|error result;}|error output = trap callEvaluationFunctionIsolated(isolatedTestFunction, params);
    if output is error && output !is TestError {
        return error InvalidArgumentError(output.message(), output, functionName = testFunction.name);
    }
    any|error result = output is TestError ? output : output.result;
    return getEvaluationOutput(result, testFunction);
}

isolated function callEvaluationFunctionIsolated(isolated function executableFunction, AnyOrError[]? params = ())
    returns record {any|error result;} {
    any|error result = params == ()
        ? function:call(executableFunction)
        : function:call(executableFunction, ...params);
    return {result};
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
