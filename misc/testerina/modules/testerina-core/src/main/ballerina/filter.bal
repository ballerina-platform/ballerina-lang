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

string[] filterGroups = [];
string[] filterDisableGroups = [];
boolean terminate = false;
boolean listGroups = false;
final TestOptions testOptions = new ();

public function setTestOptions(string inTargetPath, string inPackageName, string inModuleName, string inReport,
        string inCoverage, string inGroups, string inDisableGroups, string inTests, string inRerunFailed,
        string inListGroups, string inIsParallelExecution) {
    testOptions.setModuleName(inModuleName);
    testOptions.setPackageName(inPackageName);
    testOptions.setTargetPath(inTargetPath);
    filterGroups = parseStringArrayInput(inGroups);
    filterDisableGroups = parseStringArrayInput(inDisableGroups);
    boolean rerunFailed = parseBooleanInput(inRerunFailed, "rerun-failed");
    boolean testReport = parseBooleanInput(inReport, "test-report");
    boolean codeCoverage = parseBooleanInput(inCoverage, "code-coverage");
    listGroups = parseBooleanInput(inListGroups, "list-groups");
    boolean isParallelExecution = parseBooleanInput(inIsParallelExecution, "isParallelExecution");
    executionManager.setParallelExecutionStatus(isParallelExecution);

    if rerunFailed {
        error? err = parseRerunJson();
        if err is error {
            println("error: " + err.message());
            enableExit();
            return;
        }
        testOptions.setHasFilteredTests(true);
    } else {
        string[] singleExecTests = parseStringArrayInput(inTests);
        filterKeyBasedTests(inPackageName, inModuleName, singleExecTests);
        testOptions.setHasFilteredTests(testOptions.getFilterTestSize() > 0);
    }

    if testReport || codeCoverage {
        reportGenerators.push(moduleStatusReport);
    }
}

function parseStringArrayInput(string arrArg) returns string[] => arrArg == "" ? [] : split(arrArg, ",");

function filterKeyBasedTests(string packageName, string moduleName, string[] tests) {
    foreach string testName in tests {
        string updatedName = testName;
        string? prefix = ();
        if containsModulePrefix(packageName, moduleName, testName) {
            int separatorIndex = <int>updatedName.indexOf(MODULE_SEPARATOR);
            prefix = updatedName.substring(0, separatorIndex);
            updatedName = updatedName.substring(separatorIndex + 1);
        }
        if containsDataKeySuffix(updatedName) {
            int separatorIndex = <int>updatedName.indexOf(DATA_KEY_SEPARATOR);
            string suffix = updatedName.substring(separatorIndex + 1);
            string testPart = updatedName.substring(0, separatorIndex);
            if testOptions.isFilterSubTestsContains(updatedName) && testOptions.getFilterSubTest(updatedName)
            is string[] {
                string[] subTestList = <string[]>testOptions.getFilterSubTest(testPart);
                subTestList.push(suffix);
                testOptions.addFilterSubTest(testPart, subTestList);
            } else {
                testOptions.addFilterSubTest(testPart, [suffix]);
            }
            updatedName = testPart;
        }
        testOptions.addFilterTest(updatedName);
        testOptions.setFilterTestModule(updatedName, prefix);
    }
}

function parseBooleanInput(string input, string variableName) returns boolean {
    boolean|error booleanVariable = boolean:fromString(input);
    if booleanVariable is error {
        println(string `Invalid '${variableName}' parameter: ${booleanVariable.message()}`);
        terminate = true;
        return false;
    }
    return booleanVariable;
}

function parseIntegerInput(string input, string variableName) returns int {
    int|error intVariable = int:fromString(input);
    if intVariable is error {
        println(string `Invalid '${variableName}' parameter: ${intVariable.message()}`);
        terminate = true;
        return 0;
    }
    return intVariable;
}

function parseRerunJson() returns error? {
    string rerunJsonFilePath = testOptions.getTargetPath() + "/" + RERUN_JSON_FILE;

    // if there are no previous `bal test`` runs
    if !fileExists(rerunJsonFilePath) {
        return error("error while running failed tests : No previous test executions found.");
    }

    map<ModuleRerunJson>|error rerunJson = readRerunJson();
    if rerunJson is error {
        // error could be due to,
        //      1. rerun_test.json cannot be read
        //      2. The json cannot be converted into a ModuleRerunJson
        // but they are abstracted from the user
        return error("error while running failed tests : Invalid failed test data. Please run `bal test` command.");
    }
    ModuleRerunJson? moduleRerunJson = rerunJson[testOptions.getModuleName()];
    if moduleRerunJson is () {
        return error("error while running failed tests : Invalid failed test data. Please run `bal test` command.");
    }
    testOptions.setFilterTests(moduleRerunJson.testNames);
    testOptions.setFilterTestModules(moduleRerunJson.testModuleNames);
    testOptions.setFilterSubTests(moduleRerunJson.subTestNames);
}

isolated function readRerunJson() returns map<ModuleRerunJson>|error {
    string|error content = trap readContent(testOptions.getTargetPath() + "/" + RERUN_JSON_FILE);
    if content is error {
        return content;
    }
    return content.fromJsonStringWithType();
}

function containsModulePrefix(string packageName, string moduleName, string testName) returns boolean {
    if containsAPrefix(testName) {
        return isPrefixInCorrectFormat(packageName, moduleName, testName);
    }
    return false;
}

function containsAPrefix(string testName) returns boolean {
    if testName.includes(MODULE_SEPARATOR) {
        if containsDataKeySuffix(testName) {
            return testName.indexOf(MODULE_SEPARATOR) < testName.indexOf(DATA_KEY_SEPARATOR);
        }
        return true;
    }
    return false;
}

function containsDataKeySuffix(string testName) returns boolean {
    return testName.includes(DATA_KEY_SEPARATOR);
}

function isPrefixInCorrectFormat(string packageName, string moduleName, string testName) returns boolean {
    string prefix = testName.substring(0, <int>testName.indexOf(MODULE_SEPARATOR));
    return prefix.includes(packageName) || prefix.includes(packageName + DOT + moduleName);
}

isolated function getFullModuleName() returns string {
    return testOptions.getPackageName() == testOptions.getModuleName() ? testOptions.getPackageName()
        : testOptions.getPackageName() + DOT + testOptions.getModuleName();
}
