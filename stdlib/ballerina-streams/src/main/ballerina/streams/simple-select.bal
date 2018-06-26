import ballerina/io;

public type SimpleSelect object {
    private {
        function (StreamEvent[]) nextProcessorPointer;
        function(any o) returns any selectFunc;
    }

    new(nextProcessorPointer, selectFunc) {
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] newStreamEventArr = [];
        int index = 0;
        foreach event in streamEvents {
            StreamEvent streamEvent = {eventType: event.eventType, timestamp: event.timestamp, eventObject: event
.eventObject};
            newStreamEventArr[index] = streamEvent;
            index += 1;
        }
        if (index > 0) {
            nextProcessorPointer(newStreamEventArr);
        }

    }
};

public function createSimpleSelect(function (StreamEvent[]) nextProcPointer, function(any o) returns any selectFunc) returns SimpleSelect {
    SimpleSelect simpleSelect = new(nextProcPointer, selectFunc);
    return simpleSelect;
}