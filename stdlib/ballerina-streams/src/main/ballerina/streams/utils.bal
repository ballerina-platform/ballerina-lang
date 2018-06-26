import ballerina/time;

public type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET";

public type StreamEvent record {
    EventType eventType;
    any eventObject;
    int timestamp;
};

public function buildStreamEvent(any o) returns StreamEvent[] {
    EventType evntType = "CURRENT";
    StreamEvent[] streamEvents = [{ eventType: evntType, eventObject: o,
        timestamp: time:currentTime().time }];
    return streamEvents;
}