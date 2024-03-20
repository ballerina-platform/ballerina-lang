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

const string RERUN_JSON_FILE = "rerun_test.json";
const string MODULE_STATUS_JSON_FILE = "module_status.json";
const string CACHE_DIRECTORY = "cache";
const string TESTS_CACHE_DIRECTORY = "tests_cache";

type ReportGenerate function (ReportData data);

final ReportData reportData = new ();

ReportGenerate[] reportGenerators = [consoleReport, failedTestsReport];

type ResultData record {|
    string name;
    string suffix = "";
    string message = "";
    TestType testType;
|} & readonly;

isolated class Result {
    private ResultData data;

    isolated function init(ResultData data) {
        lock {
            self.data = data.clone();
        }
    }

    isolated function fullName() returns string {
        lock {
            return self.data.suffix == "" ? self.data.name : self.data.name + DATA_KEY_SEPARATOR + self.data.suffix;
        }
    }

    isolated function isDataProvider() returns boolean {
        lock {
            return self.data.suffix != "";
        }
    }

    isolated function testPrefix() returns string {
        lock {
            return self.data.name;
        }
    }

    isolated function testSuffix() returns string {
        lock {
            return self.data.suffix;
        }
    }

    isolated function message() returns string {
        lock {
            return self.data.message;
        }
    }

    isolated function testType() returns TestType {
        lock {
            return self.data.testType;
        }
    }
}

isolated class ReportData {
    private final ResultData[] passed = [];
    private final ResultData[] failed = [];
    private final ResultData[] skipped = [];

    isolated function onPassed(*ResultData result) {
        lock {
            self.passed.push(result);
        }
    }

    isolated function onFailed(*ResultData result) {
        lock {
            self.failed.push(result);
        }
    }

    isolated function onSkipped(*ResultData result) {
        lock {
            self.skipped.push(result);
        }
    }

    isolated function passedCases() returns ResultData[] {
        lock {
            return self.passed.clone();
        }
    }

    isolated function failedCases() returns ResultData[] {
        lock {
            return self.failed.clone();
        }
    }

    function skippedCases() returns ResultData[] {
        lock {
            return self.skipped.clone();
        }
    }

    isolated function passedCount() returns int {
        lock {
            return self.passed.length();
        }
    }

    isolated function failedCount() returns int {
        lock {
            return self.failed.length();
        }
    }

    isolated function skippedCount() returns int {
        lock {
            return self.skipped.length();
        }
    }
}

isolated function consoleReport(ReportData data) {
    if !isSystemConsole() {
        data.passedCases().forEach(isolated function(ResultData entrydata) {
            Result entry = new (entrydata);
            println("\t\t[pass] " + entry.fullName());
        });
    }

    data.failedCases().forEach(isolated function(ResultData entrydata) {
        Result entry = new (entrydata);
        println("\n\t\t[fail] " + entry.fullName() + ":");
        println("\n\t\t    " + formatFailedError(entry.message(), 3));
    });

    int totalTestCount = data.passedCount() + data.failedCount() + data.skippedCount();

    println("\n");
    if totalTestCount == 0 {
        println("\t\tNo tests found");
    } else {
        println("\t\t" + data.passedCount().toString() + " passing");
        println("\t\t" + data.failedCount().toString() + " failing");
        println("\t\t" + data.skippedCount().toString() + " skipped");
    }
}

isolated function formatFailedError(string message, int tabCount) returns string {
    string[] lines = split(message, "\n");
    lines.push("");
    string tabs = "";
    int count = 0;
    foreach int i in 1 ... tabCount {
        count += 1;
        tabs += "\t";
    }
    return string:'join("\n" + tabs, ...lines);
}

isolated function failedTestsReport(ReportData data) {
    string[] testNames = [];
    map<string?> testModuleNames = {};
    map<string[]> subTestNames = {};
    foreach ResultData resultdata in data.failedCases() {
        Result result = new (resultdata);
        string testPrefix = result.testPrefix();
        string testSuffix = result.testType() == DATA_DRIVEN_MAP_OF_TUPLE ?
            SINGLE_QUOTE + result.testSuffix() + SINGLE_QUOTE : result.testSuffix();
        testNames.push(testPrefix);
        testModuleNames[testPrefix] = testOptions.getModuleName();
        if result.isDataProvider() {
            if subTestNames.hasKey(testPrefix) && subTestNames[testPrefix] is string[] {
                string[] subTestList = <string[]>subTestNames[testPrefix];
                subTestList.push(testSuffix);
            } else {
                subTestNames[testPrefix] = [testSuffix];
            }
        }
    }
    ModuleRerunJson moduleReport = {testNames, testModuleNames, subTestNames};
    string filePath = testOptions.getTargetPath() + "/" + RERUN_JSON_FILE;

    map<ModuleRerunJson> rerunJson;
    if fileExists(filePath) {
        map<ModuleRerunJson>|error content = readRerunJson();
        if content is error {
            println(content.message());
            return;
        }
        rerunJson = content;
    } else {
        rerunJson = {};
    }
    rerunJson[testOptions.getModuleName()] = moduleReport;

    error? err = writeContent(filePath, rerunJson.toString());
    if err is error {
        println(err.message());
    }
}

function moduleStatusReport(ReportData data) {
    map<string>[] tests = [];
    data.passedCases().forEach(result => tests.push({
        "name": escapeSpecialCharactersJson(new Result(result).fullName()),
        "status": "PASSED"
    }));
    data.failedCases().forEach(result => tests.push({
        "name": escapeSpecialCharactersJson(new Result(result).fullName()),
        "status": "FAILURE",
        "failureMessage": replaceDoubleQuotes(new Result(result).message())
    }));
    data.skippedCases().forEach(result => tests.push({
        "name": escapeSpecialCharactersJson((new Result(result).fullName())),
        "status": "SKIPPED"
    }));

    map<json> output = {
        "totalTests": data.passedCount() + data.failedCount() + data.skippedCount(),
        "passed": data.passedCount(),
        "failed": data.failedCount(),
        "skipped": data.skippedCount(),
        "tests": tests
    };

    error? err = writeContent(testOptions.getTargetPath() + "/" + CACHE_DIRECTORY + "/" + TESTS_CACHE_DIRECTORY
                + "/" + testOptions.getModuleName() + "/" + MODULE_STATUS_JSON_FILE, output.toString());
    if err is error {
        println(err.message());
    }
}

function escapeSpecialCharactersJson(string name) returns string {
    string|error encodedName = escapeSpecialCharacters(name);
    return ((encodedName is string) ? encodedName : name);
}

function replaceDoubleQuotes(string originalString) returns string {
    string updatedString = "";
    foreach string chr in originalString {
        if chr == "\"" {
            updatedString += "\\\"";
        } else {
            updatedString += chr;
        }
    }
    return updatedString;
}
