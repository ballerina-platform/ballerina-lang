import ballerina/io;

public type SimpleSelect object {
    private {
        function (StreamEvent[]) nextProcessorPointer1;
        function(any o) returns any selectFunc;
    }

    new(nextProcessorPointer1, selectFunc) {
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] newStreamEventArr = [];
        int index = 0;
        foreach event in streamEvents {
            StreamEvent streamEvent = {eventType: event.eventType, timestamp: event.timestamp, eventObject: selectFunc(event)};
            newStreamEventArr[index] = streamEvent;
            index += 1;
        }
        if (index > 0) {
            nextProcessorPointer1(newStreamEventArr);
        }

    }
};

public function createSimpleSelect(function (StreamEvent[]) nextProcPointer, function(any o) returns any selectFunc)
        returns SimpleSelect {
    SimpleSelect simpleSelect = new(nextProcPointer, selectFunc);
    return simpleSelect;
}