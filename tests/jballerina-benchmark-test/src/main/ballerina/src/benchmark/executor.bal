// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'int;

public function main(string... args) {
    if (args.length() < 3) {
        println("ERROR: Please specify the number of warm-up iterations and benchmark iterations.");
        return;
    }
    int warmupCount = <int>'int:fromString(args[0]);
    int benchmarkCount = <int>'int:fromString(args[1]);
    string functionName = args[2];
    loadFunctions();
    (function(int warmup, int benchmark) returns int)|function()|() func = getFunction(functionName);
    if (func is (function(int warmup, int benchmark) returns int)) {
        executeSingleExecFunction(func, functionName, warmupCount, benchmarkCount);
    } else if (func is function()) {
        executeMultiExecFunction(func, functionName, warmupCount, benchmarkCount);
    } else {
        println("NotFound,NA,NA");
    }
}

function executeSingleExecFunction((function(int w, int b) returns int) f,
        string functionName, int warmupCount, int benchmarkCount) {
    int totalTime = f(warmupCount, benchmarkCount);
    float totalTimeMilli = (totalTime / 1000000.0f);
    float avgLatency = (<float>totalTime / <float>benchmarkCount);
    float tps = (1000000000.0f / avgLatency);
    print(functionName + ",");
    print(sprintf("%10.2f,", totalTimeMilli));
    println(sprintf("%10.2f", tps));
}

function executeMultiExecFunction(function () f, string functionName,
        int warmupCount, int benchmarkCount) {
    int i = 0;
    while (i < warmupCount) {
        i = i + 1;
        f();
    }
    i = 0;
    int startTime = nanoTime();
    while (i < benchmarkCount) {
        i = i + 1;
        f();
    }
    int totalTime = (nanoTime() - startTime);
    float totalTimeMilli = (totalTime / 1000000.0f);
    float avgLatency = (<float>totalTime / <float>benchmarkCount);
    float tps = (1000000000.0f / avgLatency);
    print(functionName + ",");
    print(sprintf("%10.2f,", totalTimeMilli));
    println(sprintf("%10.2f", tps));
}
