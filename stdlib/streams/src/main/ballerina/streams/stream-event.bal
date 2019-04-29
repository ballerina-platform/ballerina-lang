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

# The `StreamEvent` object is a wrapper around the actual data being received to the input stream. If a record is
# receive to a input stream, that record is converted to a map of anydata values and set that map to a field called
# `data` in a new `StreamEvent` object. `StreamEvent` is only used internally to transmit event data from one
# processor to another processor. At the time the record is converted to a map, the timestamp is set. If the record
# is first received by the input stream, the eventType is set to streams:CURRENT. Other than stream events of type
# streams:CURRENT, there are 3 types of StreamEvents. They are streams:EXPIRED, streams:RESET, streams:TIMER. An expired
# event is used to remove the state of its respective current event. A reset event is used to completely wipe the
# state and a timer event is used to trigger the `process` method of a particular processor in timely manner.
#
# + eventType - description
# + timestamp - description
# + data - description
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
        } else {
            self.data = eventData;
        }
    }

    # Returns a copy of the stream event instance.
    #
    # + return - A copy of the `StreamEvent` object with its state.
    public function copy() returns StreamEvent {
        StreamEvent clone = new(self.cloneData(), self.eventType, self.timestamp);
        return clone;
    }

    # Adds key values pairs in a given map to the field `data`.
    #
    # + eventData - map of anydata values to be added to field `data`.
    public function addData(map<anydata> eventData) {
        foreach var (k, v) in eventData {
            self.data[k] = v;
        }
    }

    # Adds an attribute of an event to the map with its value.
    #
    # + key - The key of the map entry.
    # + val - Respective value of the `key`.
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

# This record represents a stream event which can be persisted.
#
# + eventType - description
# + timestamp - description
# + data - description
public type SnapshottableStreamEvent record {|
    EventType eventType = "CURRENT";
    int timestamp = 0;
    map<anydata> data = {};
|};
