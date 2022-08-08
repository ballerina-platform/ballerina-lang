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

string[] passed = [];
[string, string][] failed = [];
string[] skipped = [];

function onPassed(string testName) {
    passed.push(testName);
}

function onFailed(string testName, string errorMessage) {
    failed.push([testName, errorMessage]);
}

function onSkipped(string testName) {
    skipped.push(testName);
}

function testSuiteResult() {
    println("\t\t" + passed.length().toString() + " passing");
    println("\t\t" + failed.length().toString() + " failing");
    println("\t\t" + skipped.length().toString() + " skipped");
}

public function report() {
    passed.forEach(entry => println("\t\t[pass] " + entry));
    failed.forEach(function([string, string] entry) {
        println("\t\t[fail] " + entry[0] + ":");
        println("\n\t\t    " + formatError(entry[1]));
    });

    testSuiteResult();
}

function formatError(string message) returns string {
    string[] lines = split(message, "\n");
    lines.push("");
    return string:'join("\n\t\t\t", ...lines);
}
