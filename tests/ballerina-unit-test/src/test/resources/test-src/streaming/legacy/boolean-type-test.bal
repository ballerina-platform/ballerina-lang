// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/runtime;
import ballerina/io;

type ClientRequest record {
    string host;
};

type RequestCount record {
    string host;
    int count;
    boolean test;
};

stream<ClientRequest> requestStream;
RequestCount[] globalRequestsArray = [];
int requestCount = 0;

function initRealtimeRequestCounter() {

    stream<RequestCount> requestCountStream;
    stream<RequestCount> requestCountStream2;

    // Whenever the `requestCountStream` stream receives an event from the streaming rules defined in the `forever` block,
    // the `printRequestCount` function is invoked.
    requestCountStream.subscribe(printRequestCount);
    requestCountStream2.subscribe(printRequestCount2);

    // Gather all the events coming in to the `requestStream` for five seconds, group them by the host, count the number
    // of requests per host, and check if the count is more than six. If yes, publish the output (host and the count) to
    // the `requestCountStream` stream as an alert. This `forever` block is executed once, when initializing the service.
    // The processing happens asynchronously each time the `requestStream` receives an event.
    forever {
        from requestStream
        window timeBatch(5000)
        select host, count(host) as count, (host == "local") as test
        group by host
        having count > 6
        => (RequestCount[] counts) {
        // `counts` is the output of the streaming rules and is published to the `requestCountStream`.
        // The `select` clause should match the structure of the `RequestCount` struct.
            foreach c in counts {
                requestCountStream.publish(c);
            }
        }

        from requestCountStream
        select host, count, test
        => (RequestCount[] counts) {
            foreach c in counts {
                requestCountStream2.publish(c);
            }
        }
    }
}

function startStreamingQuery() returns (RequestCount[]) {

    int i = 0;
    int count = 0;
    initRealtimeRequestCounter();
    ClientRequest t1 = {host:"local"};

    while ( i < 7) {
        requestStream.publish(t1);
        runtime:sleep(100);
        i = i + 1;
    }

    while(true) {
        runtime:sleep(500);
        count++;
        if((lengthof globalRequestsArray) > 0 || count == 10) {
            break;
        }
    }
    return globalRequestsArray;
}

// Define the `printRequestCount` function.
function printRequestCount(RequestCount reqCount) {
    io:println("ALERT!! : Received more than 6 requests from the " +
            "host within 5 seconds : " + reqCount.host);
}

function printRequestCount2(RequestCount reqCount) {
    globalRequestsArray[requestCount] = reqCount;
    requestCount = requestCount + 1;
}
