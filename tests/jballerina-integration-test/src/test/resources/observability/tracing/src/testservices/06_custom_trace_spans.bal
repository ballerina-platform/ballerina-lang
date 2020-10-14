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

import ballerina/testobserve;
import ballerina/observe;

service testServiceFive on new testobserve:Listener(9095) {
    resource function resourceOne(testobserve:Caller caller) {
        var customSpanOneId = checkpanic observe:startSpan("customSpanOne");
        _ = checkpanic observe:addTagToSpan("resource", "resourceOne", customSpanOneId);
        _ = checkpanic observe:addTagToSpan("custom", "true", customSpanOneId);
        _ = checkpanic observe:addTagToSpan("index", "1", customSpanOneId);
        var a = 12;
        var b = 27;
        var sum = calculateSumWithObservability(a, b);
        var expectedSum = a + b;
        if (sum != expectedSum) {
            error err = error("failed to find the sum of " + a.toString() + " and " + b.toString()
                + ". expected: " + expectedSum.toString() + " received: " + sum.toString());
            panic err;
        }
        checkpanic observe:finishSpan(customSpanOneId);

        var customSpanTwoId = checkpanic observe:startSpan("customSpanTwo");
        _ = checkpanic observe:addTagToSpan("resource", "resourceOne", customSpanTwoId);
        _ = checkpanic observe:addTagToSpan("custom", "true", customSpanTwoId);
        _ = checkpanic observe:addTagToSpan("index", "2", customSpanTwoId);
        checkpanic caller->respond("Hello! from resource one");
        checkpanic observe:finishSpan(customSpanTwoId);

        var err = observe:addTagToSpan("disallowed_tag", "true", customSpanTwoId);
        if (!(err is error)) {
            error panicErr = error("tag added to finished span which should not have been added");
            panic panicErr;
        }
    }

    resource function resourceTwo(testobserve:Caller caller) {
        int customSpanThreeId = observe:startRootSpan("customSpanThree");
        _ = checkpanic observe:addTagToSpan("resource", "resourceTwo", customSpanThreeId);
        _ = checkpanic observe:addTagToSpan("custom", "true", customSpanThreeId);
        _ = checkpanic observe:addTagToSpan("index", "3", customSpanThreeId);
        testFunctionForCustomUserTrace(customSpanThreeId);
        checkpanic observe:finishSpan(customSpanThreeId);

        checkpanic caller->respond("Hello! from resource two");
    }
}

function testFunctionForCustomUserTrace(int customSpanThreeId) {
    int customSpanFourId = checkpanic observe:startSpan("customSpanFour", (),  customSpanThreeId);
    _ = checkpanic observe:addTagToSpan("resource", "resourceTwo", customSpanFourId);
    _ = checkpanic observe:addTagToSpan("custom", "true", customSpanFourId);
    _ = checkpanic observe:addTagToSpan("index", "4", customSpanFourId);
    var a = 12;
    var b = 27;
    var sum = calculateSumWithObservability(a, b);
    var expectedSum = a + b;
    if (sum != expectedSum) {
        error err = error("failed to find the sum of " + a.toString() + " and " + b.toString()
            + ". expected: " + expectedSum.toString() + " received: " + sum.toString());
        panic err;
    }
    checkpanic observe:finishSpan(customSpanFourId);
}
