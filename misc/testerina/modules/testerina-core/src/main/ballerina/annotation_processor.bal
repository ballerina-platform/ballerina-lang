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

type AnnotationProcessor function (string name, function f, string[] dependsOn) returns boolean;

AnnotationProcessor[] annotationProcessors = [
    processConfigAnnotation,
    processBeforeSuiteAnnotation,
    processAfterSuiteAnnotation,
    processBeforeEachAnnotation,
    processAfterEachAnnotation,
    processBeforeGroupsAnnotation,
    processAfterGroupsAnnotation
];

function processAnnotation(string name, function f, string[] dependsOn) {
    boolean annotationProcessed = false;
    foreach AnnotationProcessor annotationProcessor in annotationProcessors {
        if (annotationProcessor(name, f, dependsOn)) {
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

function processConfigAnnotation(string name, function f, string[] dependsOn) returns boolean {
    TestConfig? config = (typeof f).@Config;
    if config != () {
        DataProviderReturnType? params = ();
        error? diagnostics = ();
        if config.dataProvider != () {
            DataProviderReturnType|error providerOutput = trap config.dataProvider();
            if providerOutput is error {
                diagnostics = error(
                    string `Failed to execute the data provider for '${name}', ` + "\n" + providerOutput.message());
            } else {
                params = providerOutput;
            }
        }
        config.groups.forEach(group => groupStatusRegistry.incrementTotalTest(group));
        boolean enabled = config.enable && (filterGroups.length() == 0 ? true : hasGroup(config.groups, filterGroups))
            && (filterDisableGroups.length() == 0 ? true : !hasGroup(config.groups, filterDisableGroups)) && hasTest(name);

        testRegistry.addFunction(name = name, executableFunction = f, params = params, before = config.before,
            after = config.after, groups = config.groups, diagnostics = diagnostics, dependsOn = config.dependsOn,
            dependsOnString = dependsOn, enabled = enabled, dependsOnCount = dependsOn.length());
        return true;
    }
    return false;
}

function processBeforeSuiteAnnotation(string name, function f, string[] dependsOn) returns boolean {
    boolean? isTrue = (typeof f).@BeforeSuite;
    if isTrue == true {
        beforeSuiteRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterSuiteAnnotation(string name, function f, string[] dependsOn) returns boolean {
    AfterSuiteConfig? config = (typeof f).@AfterSuite;
    if config != () {
        afterSuiteRegistry.addFunction(name = name, executableFunction = f, alwaysRun = config.alwaysRun);
        return true;
    }
    return false;
}

function processBeforeEachAnnotation(string name, function f, string[] dependsOn) returns boolean {
    boolean? isTrue = (typeof f).@BeforeEach;
    if isTrue == true {
        beforeEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterEachAnnotation(string name, function f, string[] dependsOn) returns boolean {
    boolean? isTrue = (typeof f).@AfterEach;
    if isTrue == true {
        afterEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processBeforeGroupsAnnotation(string name, function f, string[] dependsOn) returns boolean {
    BeforeGroupsConfig? config = (typeof f).@BeforeGroups;
    if config != () {
        config.value.forEach(group => beforeGroupsRegistry.addFunction(group,
            name = name, executableFunction = f));
        return true;
    }
    return false;
}

function processAfterGroupsAnnotation(string name, function f, string[] dependsOn) returns boolean {
    AfterGroupsConfig? config = (typeof f).@AfterGroups;
    if config != () {
        config.value.forEach(group => afterGroupsRegistry.addFunction(group,
            name = name, executableFunction = f, alwaysRun = (<AfterGroupsConfig>config).alwaysRun));
        return true;
    }
    return false;
}

function hasGroup(string[] groups, string[] filter) returns boolean {
    foreach string group in groups {
        if filter.indexOf(group) is int {
            return true;
        }
    }
    return false;
}

function hasTest(string name) returns boolean {
    if hasFilteredTests {
        string testName = name;
        int? testIndex = filterTests.indexOf(testName);
        if testIndex == () {
            foreach string filter in filterTests {
                if (filter.includes(WILDCARD) && matchWildcard(testName, filter) && matchModuleName(filter)) {
                    return true;
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

function matchModuleName(string testName) returns boolean {
    string? filterModule = filterTestModules[testName];
    return filterModule == () ? true : filterModule == getFullModuleName();
}
