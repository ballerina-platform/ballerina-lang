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
string[] filterSubTests = [];
string moduleName = "";
boolean hasFilteredTests = false;
string targetPath = "";
boolean terminate = false;
boolean listGroups = false;

public function setTestOptions(string inTargetPath, string inModuleName, string inReport, string inCoverage, 
    string inGroups, string inDisableGroups, string inTests, string inRerunFailed, string inListGroups) {

    targetPath = inTargetPath;
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
    } else {
        filterTests = parseStringArrayInput(inTests);
    }

    if testReport || codeCoverage {
        reportGenerators.push(moduleStatusReport);
    }

    hasFilteredTests = filterTests.length() > 0;
}

function parseStringArrayInput(string arrArg) returns string[] => arrArg == "" ? [] : split(arrArg, ",");

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
        filterSubTests = moduleRerunJson.subTestNames;
    }
}

function readRerunJson() returns map<ModuleRerunJson>|error {
    string|error content = trap readContent(targetPath + "/" + RERUN_JSON_FILE);
    return content is string ? content.fromJsonStringWithType() : content;
}
