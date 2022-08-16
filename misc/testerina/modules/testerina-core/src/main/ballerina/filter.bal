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
boolean hasFilteredTests = false;
string projectTargetPath = "";

public function setTestOptions(string targetPath, string jacocoAgentJarPath, string report,
    string coverage, string groups, string disableGroups, string tests, string rerunFailed) {
    projectTargetPath = targetPath;

    filterGroups = parseStringArrayInput(groups);
    filterDisableGroups = parseStringArrayInput(disableGroups);
    boolean|error rerunFailedBoolean = boolean:fromString(rerunFailed);
    if rerunFailedBoolean is error {
        println("Invalid rerun parameter: " + rerunFailedBoolean.message());
        return;
    }

    if rerunFailedBoolean {
        string[]|error output = readContentFromJson(projectTargetPath + RERUN_TEST_JSON);
        if output is error {
            println("Unable to read the 'rerun_test.json'");
            return;
        } else {
            filterTests = output;
        }
    } else {
        filterTests = parseStringArrayInput(tests);
    }
    hasFilteredTests = filterTests.length() > 0;
}

function parseStringArrayInput(string arrArg) returns string[] => arrArg == "" ? [] : split(arrArg, ",");
