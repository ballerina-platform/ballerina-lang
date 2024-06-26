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

enum SerialExecutionReason {
    NONE_ISOLATED_TEST_FUNCTION = "non-isolated test function",
    NONE_ISOLATED_DATA_PROVIDER_FUNCTION = "non-isolated data-provider function",
    UNSAFE_TEST_PARAMETERS = "unsafe test parameters",
    NON_ISOLATED_BEFORE_FUNCTION = "non-isolated before function",
    NON_ISOLATED_AFTER_FUNCTION = "non-isolated after function",
    NON_ISOLATED_BEFORE_GROUPS_FUNCTION = "non-isolated before-groups function",
    NON_ISOLATED_AFTER_GROUPS_FUNCTION = "non-isolated after-groups function",
    NON_ISOLATED_BEFORE_EACH_FUNCTION = "non-isolated before-each function",
    NON_ISOLATED_AFTER_EACH_FUNCTION = "non-isolated after-each function"
}

type AnnotationProcessor function (string name, function f) returns boolean;

AnnotationProcessor[] annotationProcessors = [
    processBeforeSuiteAnnotation,
    processAfterSuiteAnnotation,
    processBeforeEachAnnotation,
    processAfterEachAnnotation,
    processBeforeGroupsAnnotation,
    processAfterGroupsAnnotation,
    processConfigAnnotation
];

# Register a test function to run. This function is intended for internal use only.
#
# + name - test name  
# + f - test function
public function registerTest(string name, function f) {
    foreach AnnotationProcessor annotationProcessor in annotationProcessors {
        if annotationProcessor(name.trim(), f) {
            break;
        }
    }
}

function processConfigAnnotation(string name, function f) returns boolean {
    TestConfig? config = (typeof f).@Config;
    if config != () {
        // Evaluate the test function to determine the parallelizability of the test function.
        boolean isTestFunctionIsolated = f is isolated function;
        boolean isDataProviderIsolated = true;
        boolean isTestFunctionParamSafe = true;
        string[] reasonForSerialExecution = [];
        boolean isSatisfiedParallelizableConditions = isBeforeAfterFuncSetIsolated(config,
                reasonForSerialExecution) && isTestFunctionIsolated;
        DataProviderReturnType? params = ();
        error? diagnostics = ();
        if config.dataProvider != () {
            var providerFn = config.dataProvider;
            if providerFn is function () returns (DataProviderReturnType?) {
                isDataProviderIsolated = (<function>providerFn is isolated function);
                isTestFunctionParamSafe = isFunctionParamConcurrencySafe(f);
                isSatisfiedParallelizableConditions = isSatisfiedParallelizableConditions
                                                                && isDataProviderIsolated && isTestFunctionParamSafe;

                DataProviderReturnType providerOutput = providerFn();
                params = <DataProviderReturnType>providerOutput;
            } else {
                diagnostics = error("Failed to execute the data provider");
            }
        }

        // Register the reason for serial execution.
        if !isTestFunctionIsolated {
            reasonForSerialExecution.push(NONE_ISOLATED_TEST_FUNCTION);
        }
        if !isDataProviderIsolated {
            reasonForSerialExecution.push(NONE_ISOLATED_DATA_PROVIDER_FUNCTION);
        }
        if !isTestFunctionParamSafe {
            reasonForSerialExecution.push(UNSAFE_TEST_PARAMETERS);
        }

        // If the test function is not parallelizable, then print the reason for serial execution.
        if !isSatisfiedParallelizableConditions && config.serialExecution == ()
                && executionManager.isParallelExecutionEnabled() {
            println(string `WARNING: Test function '${name}' cannot be parallelized, reason: ${string:'join(", ",
                            ...reasonForSerialExecution)}`);
        }

        // if enable field is true, and groups to filter exist, and groups to disable exist and test exists, then enable
        boolean enabled = config.enable
                && (filterGroups.length() == 0 || hasGroup(config.groups, filterGroups))
                && (filterDisableGroups.length() == 0 || !hasGroup(config.groups, filterDisableGroups))
                && hasTest(name);
        config.groups.forEach('group => groupStatusRegistry.incrementTotalTest('group, enabled));
        dataDrivenTestParams[name] = params;

        // if the serial execution field is set, or if parallel execution is disabled, or if not parallelizable, then
        // set the serial execution to true.
        boolean serialExecution = config?.serialExecution != ()
                || !executionManager.isParallelExecutionEnabled()
                || !isSatisfiedParallelizableConditions;

        testRegistry.addFunction(name = name, executableFunction = f, before = config.before,
            after = config.after, groups = config.groups.cloneReadOnly(), diagnostics = diagnostics,
            dependsOn = config.dependsOn.cloneReadOnly(), serialExecution = serialExecution,
            config = config.cloneReadOnly());
        executionManager.createTestFunctionMetaData(functionName = name, dependsOnCount = config.dependsOn.length(),
            enabled = enabled);
    }
    return false;
}

function isBeforeAfterFuncSetIsolated(TestConfig config, string[] reasonForSerialExecution) returns boolean {
    boolean isBeforeAfterFunctionSetIsolated = true;
    (function () returns any|error)? before = config.before;
    if before !is () {
        if before !is isolated function () returns any|error {
            isBeforeAfterFunctionSetIsolated = false;
            reasonForSerialExecution.push(NON_ISOLATED_BEFORE_FUNCTION);
        }
    }
    (function () returns any|error)? after = config.after;
    if after !is () {
        if after !is isolated function () returns any|error {
            isBeforeAfterFunctionSetIsolated = false;
            reasonForSerialExecution.push(NON_ISOLATED_AFTER_FUNCTION);
        }
    }
    foreach string 'group in config.groups {
        TestFunction[]? beforeGroupFunctions = beforeGroupsRegistry.getFunctions('group);
        if beforeGroupFunctions !is () {
            foreach TestFunction beforeGroupFunction in beforeGroupFunctions {
                if beforeGroupFunction.executableFunction !is isolated function {
                    isBeforeAfterFunctionSetIsolated = false;
                    reasonForSerialExecution.push(NON_ISOLATED_BEFORE_GROUPS_FUNCTION);
                }
            }
        }
        TestFunction[]? afterGroupFunctions = afterGroupsRegistry.getFunctions('group);
        if afterGroupFunctions !is () {
            foreach TestFunction afterGroupFunction in afterGroupFunctions {
                if afterGroupFunction.executableFunction !is isolated function {
                    isBeforeAfterFunctionSetIsolated = false;
                    reasonForSerialExecution.push(NON_ISOLATED_AFTER_GROUPS_FUNCTION);
                }
            }
        }
    }
    TestFunction[] beforeEachFunctions = beforeEachRegistry.getFunctions();
    foreach TestFunction beforeEachFunction in beforeEachFunctions {
        if beforeEachFunction.executableFunction !is isolated function {
            isBeforeAfterFunctionSetIsolated = false;
            reasonForSerialExecution.push(NON_ISOLATED_BEFORE_EACH_FUNCTION);
        }
    }
    TestFunction[] afterEachFunctions = afterEachRegistry.getFunctions();
    foreach TestFunction afterEachFunction in afterEachFunctions {
        if afterEachFunction.executableFunction !is isolated function {
            isBeforeAfterFunctionSetIsolated = false;
            reasonForSerialExecution.push(NON_ISOLATED_AFTER_EACH_FUNCTION);
        }
    }
    return isBeforeAfterFunctionSetIsolated;
}

function processBeforeSuiteAnnotation(string name, function f) returns boolean {
    boolean? isTrue = (typeof f).@BeforeSuite;
    if isTrue == true {
        beforeSuiteRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterSuiteAnnotation(string name, function f) returns boolean {
    AfterSuiteConfig? config = (typeof f).@AfterSuite;
    if config != () {
        afterSuiteRegistry.addFunction(name = name, executableFunction = f, alwaysRun = config.alwaysRun);
        return true;
    }
    return false;
}

function processBeforeEachAnnotation(string name, function f) returns boolean {
    boolean? isTrue = (typeof f).@BeforeEach;
    if isTrue == true {
        beforeEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterEachAnnotation(string name, function f) returns boolean {
    boolean? isTrue = (typeof f).@AfterEach;
    if isTrue == true {
        afterEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processBeforeGroupsAnnotation(string name, function f) returns boolean {
    BeforeGroupsConfig? config = (typeof f).@BeforeGroups;
    if config != () {
        config.value.forEach('group => beforeGroupsRegistry.addFunction('group,
            name = name, executableFunction = f));
        return true;
    }
    return false;
}

function processAfterGroupsAnnotation(string name, function f) returns boolean {
    AfterGroupsConfig? config = (typeof f).@AfterGroups;
    if config != () {
        config.value.forEach('group => afterGroupsRegistry.addFunction('group,
            name = name, executableFunction = f, alwaysRun = (<AfterGroupsConfig>config).alwaysRun));
        return true;
    }
    return false;
}

function hasGroup(string[] groups, string[] filter) returns boolean {
    foreach string 'group in groups {
        if filter.indexOf('group) is int {
            return true;
        }
    }
    return false;
}

isolated function hasTest(string name) returns boolean {
    if !testOptions.getHasFilteredTests() {
        return true;
    }
    int? testIndex = testOptions.getFilterTestIndex(name);
    if testIndex == () {
        foreach string filter in testOptions.getFilterTests() {
            if filter.includes(WILDCARD) {
                return matchWildcard(name, filter) == true && matchModuleName(filter);
            }
        }
        return false;
    }
    return matchModuleName(name);
}

isolated function matchModuleName(string testName) returns boolean {
    string? filterModule = testOptions.getFilterTestModule(testName);
    return filterModule == () || filterModule == getFullModuleName();
}
