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

type AnnotationProcessor function (string name, function f) returns boolean;

AnnotationProcessor[] annotationProcessors = [
    processConfigAnnotation,
    processBeforeSuiteAnnotation,
    processAfterSuiteAnnotation,
    processBeforeEachAnnotation,
    processAfterEachAnnotation,
    processBeforeGroupsAnnotation,
    processAfterGroupsAnnotation
];

public function registerTest(string name, function f) {
    boolean annotationProcessed = false;
    foreach AnnotationProcessor annotationProcessor in annotationProcessors {
        if (annotationProcessor(name.trim(), f)) {
            annotationProcessed = true;
            break;
        }
    }

    //TODO: Enable dynamic registration upon approval
    // Process the register functions under the test factory method.
    // Currently the dynamic registration does not support groups filtration.
    // if !annotationProcessed && filterGroups.length() == 0 {
    //     testRegistry.addFunction(name = name, executableFunction = f);
    // }
}

function processConfigAnnotation(string name, function f) returns boolean {
    TestConfig? config = (typeof f).@Config;
    if config != () {
        // Evaluate the test function to determine the parallelizability of the test function.
        boolean isTestFunctionIsolated = f is isolated function;
        boolean isDataProviderIsolated = true;
        boolean isTestFunctionParamSafe = true;
        boolean isSatisfiedParallelizableConditions = isTestFunctionIsolated;
        string[] reasonToSerialExecution = [];

        DataProviderReturnType? params = ();
        error? diagnostics = ();
        if config.dataProvider != () {
            var providerFn = config.dataProvider;

            if providerFn is function () returns (DataProviderReturnType?) {
                isDataProviderIsolated = (<function>providerFn is isolated function);
                isTestFunctionParamSafe = isFunctionParamConcurrencySafe(f);
                isSatisfiedParallelizableConditions = isTestFunctionIsolated && isDataProviderIsolated && isTestFunctionParamSafe;
                DataProviderReturnType providerOutput = providerFn();
                params = <DataProviderReturnType>providerOutput;
            } else {
                diagnostics = error("Failed to execute the data provider");
            }
        }

        // Register the reason for serial execution.
        if !isTestFunctionIsolated {
            reasonToSerialExecution.push("non-isolated test function");
        }

        if !isDataProviderIsolated {
            reasonToSerialExecution.push("non-isolated data-provider function");
        }

        if !isTestFunctionParamSafe {
            reasonToSerialExecution.push("unsafe test parameters");
        }

        // If the test function is not parallelizable, then print the reason for serial execution.
        if !isSatisfiedParallelizableConditions && !config.serialExecution && (conMgr.getConfiguredWorkers() > 1) {
            println("WARNING: Test function '" + name + "' cannot be parallelized due to " + string:'join(",", ...reasonToSerialExecution));
        }

        boolean enabled = config.enable && (filterGroups.length() == 0 ? true : hasGroup(config.groups, filterGroups))
            && (filterDisableGroups.length() == 0 ? true : !hasGroup(config.groups, filterDisableGroups)) && hasTest(name);
        config.groups.forEach('group => groupStatusRegistry.incrementTotalTest('group, enabled));
        dataDrivenTestParams[name] = params;
        testRegistry.addFunction(name = name, executableFunction = f, before = config.before,
            after = config.after, groups = config.groups.cloneReadOnly(), diagnostics = diagnostics, dependsOn = config.dependsOn.cloneReadOnly(),
            parallelizable = (!config.serialExecution && isSatisfiedParallelizableConditions && (conMgr.getConfiguredWorkers() > 1)), config = config.cloneReadOnly());
        conMgr.createTestFunctionMetaData(functionName = name, dependsOnCount = config.dependsOn.length(), enabled = enabled);
    }
    return false;
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
    if testOptions.getHasFilteredTests() {
        string testName = name;
        int? testIndex = testOptions.getFilterTestIndex(testName);
        if testIndex == () {
            foreach string filter in testOptions.getFilterTests() {
                if (filter.includes(WILDCARD)) {
                    boolean|error wildCardMatch = matchWildcard(testName, filter);
                    if (wildCardMatch is boolean && wildCardMatch && matchModuleName(filter)) {
                        return true;
                    }
                }
            }
            return false;
        } else if (matchModuleName(testName)) {
            return true;
        }
        return false;
    }
    return true;
}

isolated function matchModuleName(string testName) returns boolean {
    string? filterModule = testOptions.getFilterTestModule(testName);
    return filterModule == () ? true : filterModule == getFullModuleName();
}
