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
string[] filterTests = [];
map<string?> filterTestModules = {};
map<string[]> filterSubTests = {};
string moduleName = "";
string packageName = "";
boolean hasFilteredTests = false;
string targetPath = "";
boolean terminate = false;
boolean listGroups = false;

public function setTestOptions(string inTargetPath, string inPackageName, string inModuleName, string inReport,
    string inCoverage, string inGroups, string inDisableGroups, string inTests, string inRerunFailed,
    string inListGroups) {
    targetPath = inTargetPath;
    packageName = inPackageName;
    moduleName = inModuleName;
    filterGroups = parseStringArrayInput(inGroups);
    filterDisableGroups = parseStringArrayInput(inDisableGroups);
    boolean rerunFailed = parseBooleanInput(inRerunFailed, "rerun-failed");
    boolean testReport = parseBooleanInput(inReport, "test-report");
    boolean codeCoverage = parseBooleanInput(inCoverage, "code-coverage");
    listGroups = parseBooleanInput(inListGroups, "list-groups");

    if rerunFailed {
        error? err = parseRerunJson();
        if err is error {
            println("Unable to read the 'rerun_test.json': " + err.message());
            return;
        }
        hasFilteredTests = true;
    } else {
        string[] singleExecTests = parseStringArrayInput(inTests);
        filterKeyBasedTests(inPackageName, moduleName, singleExecTests);
        hasFilteredTests = filterTests.length() > 0;
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
        if (containsModulePrefix(packageName, moduleName, testName)) {
            int separatorIndex = <int>updatedName.indexOf(MODULE_SEPARATOR);
            prefix = updatedName.substring(0, separatorIndex);
            updatedName = updatedName.substring(separatorIndex + 1);
        }
        if (containsDataKeySuffix(updatedName)) {
            int separatorIndex = <int>updatedName.indexOf(DATA_KEY_SEPARATOR);
            string suffix = updatedName.substring(separatorIndex + 1);
            string testPart = updatedName.substring(0, separatorIndex);
            if (filterSubTests.hasKey(updatedName) && filterSubTests[updatedName] is string[]) {
                string[] subTestList = <string[]>filterSubTests[testPart];
                subTestList.push(suffix);
            } else {
                filterSubTests[testPart] = [suffix];
            }
            updatedName = testPart;
        }
        filterTests.push(updatedName);
        filterTestModules[updatedName] = prefix;
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

function parseRerunJson() returns error? {
    map<ModuleRerunJson> rerunJson = check readRerunJson();

    ModuleRerunJson? moduleRerunJson = rerunJson[moduleName];
    if moduleRerunJson is ModuleRerunJson {
        filterTests = moduleRerunJson.testNames;
        filterTestModules = moduleRerunJson.testModuleNames;
        filterSubTests = moduleRerunJson.subTestNames;
    }
}

function readRerunJson() returns map<ModuleRerunJson>|error {
    string|error content = trap readContent(targetPath + "/" + RERUN_JSON_FILE);
    return content is string ? content.fromJsonStringWithType() : content;
}

function containsModulePrefix(string packageName, string moduleName, string testName) returns boolean {
    if (containsAPrefix(testName)) {
        return isPrefixInCorrectFormat(packageName, moduleName, testName);
    }
    return false;
}

function containsAPrefix(string testName) returns boolean {
    if (testName.includes(MODULE_SEPARATOR)) {
        if (containsDataKeySuffix(testName)) {
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

function getFullModuleName() returns string {
    return packageName == moduleName ? packageName : packageName + DOT + moduleName;
}
