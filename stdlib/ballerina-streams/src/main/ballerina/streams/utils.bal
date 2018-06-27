import ballerina/time;

public function buildStreamEvent(any o) returns StreamEvent[] {
    EventType evntType = "CURRENT";
    StreamEvent[] streamEvents = [{eventType: evntType, eventObject: o,
        timestamp: time:currentTime().time }];
    return streamEvents;
}