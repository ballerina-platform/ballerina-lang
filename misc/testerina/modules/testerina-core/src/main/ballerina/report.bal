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

type ReportGenerate function (ReportData data);

ReportData reportData = new ();

ReportGenerate[] reportGenerators = [consoleReport];

function consoleReport(ReportData data) {
    data.passedCases().forEach(entry => println("\t\t[pass] " + entry));
    data.failedCases().entries().forEach(function([string, string] entry) {
        println("\t\t[fail] " + entry[0] + ":");
        println("\n\t\t    " + formatFailedError(entry[1]));
    });

    println("\t\t" + data.passedCount().toString() + " passing");
    println("\t\t" + data.failedCount().toString() + " failing");
    println("\t\t" + data.skippedCount().toString() + " skipped");
}

function formatFailedError(string message) returns string {
    string[] lines = split(message, "\n");
    lines.push("");
    return string:'join("\n\t\t\t", ...lines);
}

class ReportData {
    private string[] passed = [];
    private map<string> failed = {};
    private string[] skipped = [];

    function onPassed(string name) => self.passed.push(name);
    function onFailed(string name, string errorMessage) {
        self.failed[name] = errorMessage;
    }
    function onSkipped(string name) => self.skipped.push(name);

    function passedCases() returns string[] => self.passed;
    function failedCases() returns map<string> => self.failed;
    function skippedCases() returns string[] => self.skipped;

    function passedCount() returns int => self.passed.length();
    function failedCount() returns int => self.failed.length();
    function skippedCount() returns int => self.skipped.length();
}

