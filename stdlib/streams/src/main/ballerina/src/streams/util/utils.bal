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

# Creates  `streams:StreamEvent` object array for a record `t` received by the stream denoted by the name `streamNme`.
# + t - A record received by the stream `streamName`.
# + streamName - Name of the stream to which the record `t` is received.
# + return - Returns an array of streams:StreamEvents|()
public function buildStreamEvent(anydata t, string streamName) returns StreamEvent?[] {
    EventType evntType = "CURRENT";
    var keyVals = <map<anydata>>map<anydata>.constructFrom(t.clone());
    StreamEvent event = new([streamName, keyVals], evntType, time:currentTime().time);
    StreamEvent?[] streamEvents = [event];
    return streamEvents;
}

# Creates a RESET event from a given event.
# + event - The event from which the reset event is created.
# + return - A stream event of type streams:RESET.
public function createResetStreamEvent(StreamEvent event) returns StreamEvent {
    StreamEvent resetStreamEvent = new(event.data, "RESET", event.timestamp);
    return resetStreamEvent;
}

# Get the stream event from any? field. This function can only be used only if we are sure that the `anyEvent` is a
# streams:StreamEvent.
# + anyEvent - The object from which, the stream event is extracted.
# + return - Returns the extracted streams:StreamEvent object.
public function getStreamEvent(any? anyEvent) returns StreamEvent {
    return <StreamEvent>anyEvent;
}

# Converts a given array of streams:StreamEvent objects to an array of `streams:SnapshottableStreamEvent`.
# + events - The events to be coverted to snapshotable events.
# + return - Returns the converted snapshotable events.
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

# Converts a given array of snapshotable events to an array of `streams:StreamEvent` objects.
# + events - Snapshotable events to be converted to `streams:StreamEvents`.
# + return - Returns the converted `streams:StreamEvents` objects array.
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

# Convert a single `streams:StreamEvent` object to `streams:SnapshottableStreamEvent` object.
# + event - The `streams:StreamEvent` object to be converted to snapshotable event.
# + return - The converted `streams:SnapshottableStreamEvent` object.
public function toSnapshottableEvent(StreamEvent event) returns SnapshottableStreamEvent {
    // todo: add the eventId
    return {
        eventType: event.eventType,
        timestamp: event.timestamp,
        data: event.data,
        streamName: event.streamName
    };
}

# Convert a single `streams:SnapshottableStreamEvent` object to `streams:StreamEvent` object.
# + event - The `streams:SnapshottableStreamEvent` object to be converted to a stream event.
# + return - The converted `streams:StreamEvent` object.
public function toStreamEvent(SnapshottableStreamEvent event) returns StreamEvent {
    // todo: add the eventId
    StreamEvent se = new(event.data, event.eventType, event.timestamp);
    return se;
}
