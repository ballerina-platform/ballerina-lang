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

public type OutputProcess object {

    private function (map<anydata>[]) outputFunc;

    public function __init(function (map<anydata>[]) outputFunc) {
        self.outputFunc = outputFunc;
    }

    public function process(StreamEvent?[] streamEvents) {
        int index = 0;
        map<anydata>[] events = [];
        int i = 0;
        foreach var ev in streamEvents {
            StreamEvent event = <StreamEvent> ev;
            if (event.eventType == "CURRENT") {
                map<anydata> outputData = {};
                foreach var (k, v) in event.data {
                    string[] s = k.split("\\.");
                    if (OUTPUT.equalsIgnoreCase(s[0])) {
                        outputData[s[1]] = v;
                    }
                }
                events[i] = outputData;
                i += 1;
            }
        }
        self.outputFunc.call(events);
    }
};

public function createOutputProcess(function (map<anydata>[]) outputFunc) returns OutputProcess {
    OutputProcess outputProcess = new(outputFunc);
    return outputProcess;
}
