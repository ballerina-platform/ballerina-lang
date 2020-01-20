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

import ballerina/lang.'int as langint;
import ballerina/stringutils;
import ballerina/system;

# The `StreamEvent` object is a wrapper around the actual data being received to the input stream. If a record is
# receive to a input stream, that record is converted to a map of anydata values and set that map to a field called
# `data` in a new `StreamEvent` object. `StreamEvent` is only used internally to transmit event data from one
# processor to another processor. At the time the record is converted to a map, the timestamp is set. If the record
# is first received by the input stream, the eventType is set to streams:CURRENT. Other than stream events of type
# streams:CURRENT, there are 3 types of StreamEvents. They are streams:EXPIRED, streams:RESET, streams:TIMER. An expired
# event is used to remove the state of its respective current event. A reset event is used to completely wipe the
# state and a timer event is used to trigger the `process` method of a particular processor in timely manner.
#
# + eventType - event type
# + timestamp - arrival timestamp
# + data - event data
# + dataMap - event data map
# + streamName - name of the initial stream
# + eventId - unique id of the event
public type StreamEvent object {
    public EventType eventType;
    public int timestamp;
    public map<anydata> data = {};
    public map<map<anydata>[]> dataMap = {};
    public string streamName;
    public string eventId;

    public function __init([string, map<map<anydata>[]>]|[string, map<anydata>]|map<anydata> eventData,
    EventType eventType, int timestamp) {
        self.eventType = eventType;
        self.timestamp = timestamp;
        self.streamName = "";
        self.eventId = system:uuid();
        if (eventData is [string, map<map<anydata>[]>]) {
            self.streamName = eventData[0];
            self.dataMap = eventData[1];
            self.toData(self.dataMap);
        } else if (eventData is [string, map<anydata>]) {
            self.streamName = eventData[0];
            foreach var [k, v] in eventData[1].entries() {
                self.data[eventData[0] + DELIMITER + k] = v;
            }
            self.toDataMap(self.data);
        } else {
            self.data = eventData;
            string key = (eventData.length() > 0) ? eventData.keys()[0] : "";
            self.streamName = stringutils:split(key, "\\.")[0];
            self.toDataMap(self.data);
        }
    }

    # Returns a copy of the stream event instance.
    #
    # + return - A copy of the `StreamEvent` object with its state.
    public function copy() returns StreamEvent {
        [string, map<map<anydata>[]>] data = [self.streamName, self.cloneDataMap()];
        StreamEvent clone = new(data, self.eventType, self.timestamp);
        clone.eventId = self.eventId;
        return clone;
    }

    # Adds key values pairs in a given map to the field `data`.
    #
    # + eventData - map of anydata values to be added to field `data`.
    public function addData(map<anydata> eventData) {
        foreach var [k, v] in eventData.entries() {
            self.data[k] = v;
        }
        self.toDataMap(eventData);
    }

    # Adds an attribute of an event to the map with its value.
    #
    # + key - The key of the map entry.
    # + val - Respective value of the `key`.
    public function addAttribute(string key, anydata val) {
        string k = self.getStreamName() + "." + key;
        self.data[k] = val;
        // add to dataMap
        self.dataMap[self.getStreamName()] = self.dataMap[self.getStreamName()] ?: [];
        map<anydata>[]? values = self.dataMap[self.getStreamName()];
        map<anydata> dataMap = values is map<anydata>[] ? values[0] : {};
        self.dataMap[self.getStreamName()][0] = dataMap;
        dataMap[key] = val;
    }

    # Returns the value of an attribute.
    #
    # + path - the path
    # + useDataMap - whether to use the dataMap or the data
    # + return - the attribute value.
    public function getAttributeValue(string path, boolean useDataMap) returns anydata {
        if (useDataMap) {
            string[] attribSplit = stringutils:split(path, "\\.");
            string[] aliasSplit = stringutils:split(attribSplit[0], "\\[");
            string attrib = attribSplit[1];
            string alias = aliasSplit[0];
            int index = 0;
            map<anydata>[] dArray = self.dataMap[alias] ?: [{}];
            if (aliasSplit.length() > 1) {
                string replacedString = stringutils:replaceAll(aliasSplit[1], "]", "");
                string indexStr = replacedString.trim();
                if (stringutils:contains(indexStr, "last")) {
                    int lastIndex = dArray.length();
                    if (stringutils:contains(indexStr, "-")) {
                        string[] vals = stringutils:split(indexStr, "-");
                        string subCount = vals[1].trim();
                        index = lastIndex - checkpanic langint:fromString(subCount);
                    } else {
                        index = lastIndex;
                    }
                } else {
                    index = checkpanic langint:fromString(indexStr);
                }
            }
            return dArray[index][attrib];
        }
        return self.get(path);
    }

    # Returns the value of an attribute.
    #
    # + path - the absolute attribute path
    # + return - the attribute value.
    public function get(string path) returns anydata {
        return self.data[path];
    }

    # Returns the name of the stream.
    #
    # + return - the stream name.
    public function getStreamName() returns string {
        return self.streamName;
    }

    # Returns the id of the event.
    #
    # + return - the event id.
    public function getEventId() returns string {
        return self.eventId;
    }

    # Returns a clone of the event data.
    #
    # + return - clone of the event data.
    public function cloneData() returns map<anydata> {
        map<anydata> dataClone = self.data.clone();
        return dataClone;
    }

    # Returns a clone of the event data map.
    #
    # + return - clone of the event data map.
    public function cloneDataMap() returns map<map<anydata>[]> {
        map<map<anydata>[]> dataMapClone = self.dataMap.clone();
        return dataMapClone;
    }

    # Copy values of a given `dataMap` into the `self.data` field.
    #
    # + dataMap - map containg event attribute values.
    public function toData(map<map<anydata>[]> dataMap) {
        foreach var [key, val] in dataMap.entries() {
            map<anydata> data = (val.length() > 0) ? val[0] : {};
            foreach var [k, v] in data.entries() {
                self.data[key + DELIMITER + k] = v;
            }
        }
    }

    # Copy values of a given `data` into the `self.dataMap` field.
    #
    # + data - map containg event attribute values.
    public function toDataMap(map<anydata> data) {
        foreach var [k, v] in data.entries() {
            string[] key = stringutils:split(k, "\\.");
            if (key.length() == 2) {
                map<anydata>[] dataMapArray = self.dataMap[key[0]] ?: [];
                self.dataMap[key[0]] = dataMapArray;
                map<anydata> dataMap = dataMapArray.length() > 0 ? dataMapArray[0] : {};
                self.dataMap[key[0]][0] = dataMap;
                dataMap[key[1]] = v;
            }
        }
    }
};

# This record represents a stream event which can be persisted.
#
# + eventType - description
# + timestamp - description
# + data - description
# + streamName - description
public type SnapshottableStreamEvent record {|
    EventType eventType = "CURRENT";
    int timestamp = 0;
    map<anydata> data = {};
    string streamName = "";
|};
