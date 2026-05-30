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
    if !isEvaluationTest(testFunction) {
        executeBeforeEachFunctions();
    }

    boolean shouldSkipDependents = false;
    if !isSkipFunction(testFunction) {
        if isDataDrivenTest(dataDrivenTestParams[testFunction.name]) {
            executeDataDrivenTestSet(testFunction);
        } else {
            shouldSkipDependents = executeNonDataDrivenTest(testFunction);
        }
    } else {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(dataDrivenTestParams[testFunction.name]));
        shouldSkipDependents = true;
    }
    testFunction.groups.forEach('group => groupStatusRegistry.incrementExecutedTest('group));
    if !isEvaluationTest(testFunction) {
        executeAfterEachFunctions();
    }
    executeAfterGroupFunctions(testFunction);
    finishTestExecution(testFunction, shouldSkipDependents);
}

function executeBeforeGroupFunctions(TestFunction testFunction) {
    foreach string 'group in testFunction.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions != () && !groupStatusRegistry.firstExecuted('group) {
            handleBeforeGroupOutput(testFunction, 'group, executeFunctions(beforeGroupFunctions, getShouldSkip()));
        }
    }
}

function executeBeforeEachFunctions() =>
    handleBeforeEachOutput(executeFunctions(beforeEachRegistry.getFunctions(), getShouldSkip()));

function executeDataDrivenTestSet(TestFunction testFunction) {
    if isEvaluationTest(testFunction) {
        executeDataDrivenEvaluation(testFunction);
        return;
    }

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

function executeDataDrivenEvaluation(TestFunction testFunction) {
    EvaluationConfig evalConfig = getEvalConfig(testFunction);
    int runs = evalConfig.runs;
    EvaluationRunWithDataSet[] entries = [];

    string[] keys = [];
    AnyOrError[][] values = [];
    DataProviderReturnType? params = dataDrivenTestParams[testFunction.name];
    _ = prepareDataSet(params, keys, values);

    foreach int iteration in 1 ... runs {
        executeBeforeEachFunctions();
        if executeBeforeFunction(testFunction) {
            executionManager.setSkip(testFunction.name);
            reportData.onSkipped(name = testFunction.name, testType = EVAL_TEST);
            return;
        }
        int totalEntries = 0;
        int passedEntries = 0;
        EvaluationOutcome[] outcomes = [];
        foreach int i in 0 ..< keys.length() {
            AnyOrError[] value = values[i];
            final readonly & readonly[] readonlyArgs = from any|error item in value
                where item is readonly
                select item;

            if readonlyArgs.length() != value.length() {
                reportData.onFailed(name = testFunction.name, testType = EVAL_TEST,
                message = string `[fail data provider for the function ${testFunction.name}]${"\n"}` +
                    "data provider returned non-readonly values"
                );
                enableExit();
                return;
            }

            ExecutionError|TestError? result = executeEvaluation(testFunction, readonlyArgs);
            error? cause = result is error ? result.cause() : ();
            if result is InvalidArgumentError && cause is error {
                string errMsg = string `[fail data provider for the function ${testFunction.name}]${"\n"}`
                    + getErrorMessage(cause);
                reportData.onFailed(name = testFunction.name, message = errMsg, testType = EVAL_TEST);
                enableExit();
                return;
            }

            if result is () {
                passedEntries += 1;
            }
            outcomes.push({id: keys[totalEntries], errorMessage: getErrorMessageFromResult(result)});
            totalEntries += 1;
        }

        if totalEntries == 0 {
            reportData.onFailed(name = testFunction.name,
                    message = string `[fail data provider for the function ${testFunction.name}]${"\n"}`
                    + "the data provider returned no data.", testType = EVAL_TEST);
            enableExit();
            return;
        }

        float passRate = <float>passedEntries / totalEntries;
        entries.push({id: iteration, outcomes: outcomes.cloneReadOnly(), passRate});
        _ = executeAfterFunctionIsolated(testFunction);
        executeAfterEachFunctions();
    }

    float cumulativePassRateSum = entries.'map(entry => entry.passRate)
        .reduce(isolated function(float total, float next) returns float => total + next, 0);
    float averagePassRate = cumulativePassRateSum / runs;

    EvaluationSummary evaluationSummary = {
        targetPassRate: evalConfig.minPassRate,
        observedPassRate: averagePassRate,
        evaluationRuns: entries
    };
    if averagePassRate >= evalConfig.minPassRate {
        reportData.onPassed(name = testFunction.name,
        message = string `evaluation passed with an average pass rate of ${averagePassRate}`,
            evaluationSummary = evaluationSummary.cloneReadOnly(), testType = EVAL_TEST
        );
        return;
    }
    reportData.onFailed(name = testFunction.name,
    message = string `evaluation failed with an average pass rate of ${averagePassRate}`,
        evaluationSummary = evaluationSummary.cloneReadOnly(), testType = EVAL_TEST
    );
    enableExit();
}

function executeNonDataDrivenTest(TestFunction testFunction) returns boolean {
    if isEvaluationTest(testFunction) {
        return executeNonDataDrivenEvaluation(testFunction);
    }
    if executeBeforeFunction(testFunction) {
        executionManager.setSkip(testFunction.name);
        reportData.onSkipped(name = testFunction.name, testType = getTestType(
                dataDrivenTestParams[testFunction.name]));
        return true;
    }
    boolean failed = handleNonDataDrivenTestOutput(testFunction, executeTestFunction(testFunction, "",
                    GENERAL_TEST));
    return executeAfterFunction(testFunction) || failed;
}

function executeNonDataDrivenEvaluation(TestFunction testFunction) returns boolean {
    EvaluationConfig evalConfig = getEvalConfig(testFunction);
    int runs = evalConfig.runs;
    float requiredConfidence = evalConfig.minPassRate;
    int passedIterations = 0;
    EvaluationRunWithoutDataSet[] entries = [];
    boolean[] afterFunctionResults = [];

    foreach int run in 1 ... runs {
        executeBeforeEachFunctions();
        if executeBeforeFunction(testFunction) {
            executionManager.setSkip(testFunction.name);
            reportData.onSkipped(name = testFunction.name, testType = EVAL_TEST);
            return true;
        }
        ExecutionError|TestError? result = executeEvaluation(testFunction);
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
        boolean afterFunctionResult = executeAfterFunction(testFunction);
        afterFunctionResults.push(afterFunctionResult);
        executeAfterEachFunctions();
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

function prepareDataDrivenTest(TestFunction testFunction, string key, AnyOrError[] value, TestType testType) {
    if executeBeforeFunction(testFunction) {
        reportData.onSkipped(name = testFunction.name, testType = getTestType(dataDrivenTestParams[testFunction.name]));
    } else {
        executeDataDrivenTest(testFunction, key, testType, value);
        _ = executeAfterFunction(testFunction);
    }
}

function executeDataDrivenTest(TestFunction testFunction, string suffix, TestType testType, AnyOrError[] params) {
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

function executeEvaluation(TestFunction testFunction, AnyOrError[]? params = ()) returns ExecutionError|TestError? {
    record {any|error result;}|error output = trap callEvaluationFunction(testFunction.executableFunction, params);
    if output is error && output !is TestError {
        return error InvalidArgumentError(output.message(), output, functionName = testFunction.name);
    }
    any|error result = output is TestError ? output : output.result;
    return getEvaluationOutput(result, testFunction);
}

function callEvaluationFunction(function executableFunction, AnyOrError[]? params = ())
    returns record {any|error result;} {
    any|error result = params == () ? function:call(executableFunction)
        : function:call(executableFunction, ...params);
    return {result};
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

isolated function getErrorMessageFromResult(any|error result) returns string? {
    if result is TestError|ExecutionError|InvalidArgumentError {
        return result.message();
    }
    return result is error ? result.toString() : ();
}
