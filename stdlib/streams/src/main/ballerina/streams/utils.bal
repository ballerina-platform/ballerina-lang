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

import ballerina/time;

public function buildStreamEvent(any t, string streamName) returns StreamEvent?[] {
    EventType evntType = "CURRENT";
    var keyVals = <map<anydata>>map<anydata>.stamp(t.clone());
    StreamEvent event = new((streamName, keyVals), evntType, time:currentTime().time);
    StreamEvent?[] streamEvents = [event];
    return streamEvents;
}

public function createResetStreamEvent(StreamEvent event) returns StreamEvent {
    StreamEvent resetStreamEvent = new(event.data, "RESET", event.timestamp);
    return resetStreamEvent;
}

public function getStreamEvent(any? anyEvent) returns StreamEvent {
    return <StreamEvent>anyEvent;
}

public function toSnapshottableEvents(StreamEvent[]|any[]? events) returns SnapshottableStreamEvent?[] {
    SnapshottableStreamEvent?[] evts = [];
    if (events is StreamEvent[]) {
        foreach StreamEvent e in events {
            evts[evts.length()] = toSnapshottableEvent(e);
        }
    } else if (events is any[]) {
        foreach var e in events {
            if (e is StreamEvent) {
                evts[evts.length()] = toSnapshottableEvent(e);
            }
        }
    }
    return evts;
}

public function toStreamEvents(SnapshottableStreamEvent[]|any[]? events) returns StreamEvent?[] {
    StreamEvent?[] evts = [];
    if (events is SnapshottableStreamEvent[]) {
        foreach SnapshottableStreamEvent e in events {
            evts[evts.length()] = toStreamEvent(e);
        }
    } else if (events is any[]) {
        foreach var e in events {
            if (e is SnapshottableStreamEvent) {
                evts[evts.length()] = toStreamEvent(e);
            }
        }
    }
    return evts;
}

// todo: add the eventId
public function toSnapshottableEvent(StreamEvent event) returns SnapshottableStreamEvent {
    return {
        eventType: event.eventType,
        timestamp: event.timestamp,
        data: event.data,
        streamName: event.streamName
    };
}

// todo: add the eventId
public function toStreamEvent(SnapshottableStreamEvent event) returns StreamEvent {
    StreamEvent se = new(event.data, event.eventType, event.timestamp);
    return se;
}
