import ballerina/time;

public function buildStreamEvent(map keyVals, string streamName) returns StreamEvent[] {
    EventType evntType = "CURRENT";
    StreamEvent[] streamEvents = [new((streamName, keyVals), evntType, time:currentTime().time)];
    return streamEvents;
}

public function createResetStreamEvent(StreamEvent event) returns StreamEvent {
    StreamEvent resetStreamEvent = new(event.data, "RESET", event.timestamp);
    return resetStreamEvent;
}