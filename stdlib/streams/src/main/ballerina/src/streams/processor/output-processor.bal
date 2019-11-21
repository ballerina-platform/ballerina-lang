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

import ballerina/stringutils;

# The `OutputProcess` object is responsible for sending the output (only the events of type 'streams:CURRENT' to the
# destination stream. It takes a function pointer `outputFunc` which actually has the logic to process the output.
public type OutputProcess object {

    private function (map<anydata>[]) outputFunc;

    public function __init(function (map<anydata>[]) outputFunc) {
        self.outputFunc = outputFunc;
    }

    # Sends the output to the streaming action. most of the time the output is published to a destination stream
    # at the streaming action. Only the events with type-`streams:CURRENT` are passed to the `outputFunc`.
    # + streamEvents - The array of stream events to be filtered out for CURRENT events.
    public function process(StreamEvent?[] streamEvents) {
        int index = 0;
        map<anydata>[] events = [];
        int i = 0;
        foreach var ev in streamEvents {
            StreamEvent event = <StreamEvent> ev;
            if (event.eventType == "CURRENT") {
                map<anydata> outputData = {};
                foreach var [k, v] in event.data.entries() {
                    string[] s = stringutils:split(k, "\\.");
                    if (stringutils:equalsIgnoreCase(OUTPUT, s[0])) {
                        outputData[s[1]] = v;
                    }
                }
                events[i] = outputData;
                i += 1;
            }
        }
        function (map<anydata>[]) outFunction = self.outputFunc;
        outFunction(events);
    }
};

# Creates and return a `OutputProcess` object.
# + outputFunc - The function pointer to a lambda function created out of the statements in the streaming action
# + return - Returns a ` OutputProcess` object.
public function createOutputProcess(function (map<anydata>[]) outputFunc) returns OutputProcess {
    OutputProcess outputProcess = new(outputFunc);
    return outputProcess;
}
