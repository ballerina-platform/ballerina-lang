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

ReportData reportData = new ();

ReportGenerate[] reportGenerators = [consoleReport, failedTestsReport];

type ResultData record {|
    string name;
    string suffix = "";
    string message = "";
    TestType testType;
|};

class Result {
    private ResultData data;

    function init(ResultData data) {
        self.data = data;
    }

    function fullName() returns string =>
        self.data.suffix == "" ? self.data.name : self.data.name + DATA_KEY_SEPARATOR + self.data.suffix;

    function isDataProvider() returns boolean => self.data.suffix != "";

    function testPrefix() returns string => self.data.name;

    function testSuffix() returns string => self.data.suffix;

    function message() returns string => self.data.message;

    function testType() returns TestType => self.data.testType;
}

class ReportData {
    private Result[] passed = [];
    private Result[] failed = [];
    private Result[] skipped = [];

    function onPassed(*ResultData result) => self.passed.push(new Result(result));
    function onFailed(*ResultData result) => self.failed.push(new Result(result));
    function onSkipped(*ResultData result) => self.skipped.push(new Result(result));

    function passedCases() returns Result[] => self.passed;
    function failedCases() returns Result[] => self.failed;
    function skippedCases() returns Result[] => self.skipped;

    function passedCount() returns int => self.passed.length();
    function failedCount() returns int => self.failed.length();
    function skippedCount() returns int => self.skipped.length();
}

function consoleReport(ReportData data) {
    if (!isSystemConsole()) {
        data.passedCases().forEach(entry => println("\t\t[pass] " + entry.fullName()));
    }
    data.failedCases().forEach(function(Result entry) {
        println("\n\t\t[fail] " + entry.fullName() + ":");
        println("\n\t\t    " + formatFailedError(entry.message()));
    });

    int totalTestCount = data.passedCount() + data.failedCount() + data.skippedCount();
    println("\n");
    if (totalTestCount == 0) {
        println("\t\tNo tests found");
    } else {
        println("\t\t" + data.passedCount().toString() + " passing");
        println("\t\t" + data.failedCount().toString() + " failing");
        println("\t\t" + data.skippedCount().toString() + " skipped");
    }
}

function formatFailedError(string message) returns string {
    string[] lines = split(message, "\n");
    lines.push("");
    return string:'join("\n\t\t\t", ...lines);
}

function failedTestsReport(ReportData data) {
    string[] testNames = [];
    map<string?> testModuleNames = {};
    map<string[]> subTestNames = {};
    foreach Result result in data.failedCases() {
        string testPrefix = result.testPrefix();
        string testSuffix = result.testType() == DATA_DRIVEN_MAP_OF_TUPLE ?
            SINGLE_QUOTE + result.testSuffix() + SINGLE_QUOTE : result.testSuffix();
        testNames.push(testPrefix);
        testModuleNames[testPrefix] = moduleName;
        if (result.isDataProvider()) {
            if (subTestNames.hasKey(testPrefix) && subTestNames[testPrefix] is string[]) {
                string[] subTestList = <string[]>subTestNames[testPrefix];
                subTestList.push(testSuffix);
            } else {
                subTestNames[testPrefix] = [testSuffix];
            }
        }
    }
    ModuleRerunJson moduleReport = {testNames, testModuleNames, subTestNames};
    string filePath = targetPath + "/" + RERUN_JSON_FILE;

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
    rerunJson[moduleName] = moduleReport;

    error? err = writeContent(filePath, rerunJson.toString());
    if err is error {
        println(err.message());
    }
}

function moduleStatusReport(ReportData data) {
    map<string>[] tests = [];
    data.passedCases().forEach(result => tests.push({
        "name": result.fullName(),
        "status": "PASSED"
    }));
    data.failedCases().forEach(result => tests.push({
        "name": result.fullName(),
        "status": "FAILURE",
        "failureMessage": result.message()
    }));
    data.skippedCases().forEach(result => tests.push({
        "name": result.fullName(),
        "status": "SKIPPED"
    }));

    map<json> output = {
        "totalTests": data.passedCount() + data.failedCount() + data.skippedCount(),
        "passed": data.passedCount(),
        "failed": data.failedCount(),
        "skipped": data.skippedCount(),
        "tests": tests
    };

    error? err = writeContent(targetPath + "/" + CACHE_DIRECTORY + "/" + TESTS_CACHE_DIRECTORY
        + "/" + moduleName + "/" + MODULE_STATUS_JSON_FILE, output.toString());
    if err is error {
        println(err.message());
    }
}
