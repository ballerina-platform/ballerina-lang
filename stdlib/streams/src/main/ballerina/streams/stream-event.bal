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

public type StreamEvent object {
    public EventType eventType;
    public int timestamp;
    public map<anydata> data = {};

    public function __init((string, map<anydata>)|map<anydata> eventData, EventType eventType, int timestamp) {
        self.eventType = eventType;
        self.timestamp = timestamp;
        if (eventData is (string, map<anydata>)) {
            foreach var (k, v) in eventData[1] {
                self.data[eventData[0] + DELIMITER + k] = v;
            }
        }
        else if (eventData is map<anydata>) {
            self.data = eventData;
        }
    }

    public function copy() returns StreamEvent {
        StreamEvent clone = new(self.cloneData(), self.eventType, self.timestamp);
        return clone;
    }

    public function addData(map<anydata> eventData) {
        foreach var (k, v) in eventData {
            self.data[k] = v;
        }
    }

    public function addAttribute(string key, anydata val) {
        string k = self.getStreamName() + "." + key;
        self.data[k] = val;
    }

    function cloneData() returns map<anydata> {
        map<anydata> dataClone = {};
        foreach var (k, v) in self.data {
            dataClone[k] = v;
        }
        return dataClone;
    }

    function getStreamName() returns string {
        string key = (self.data.length() > 0) ? self.data.keys()[0] : "";
        return key.split("\\.")[0];
    }

};
