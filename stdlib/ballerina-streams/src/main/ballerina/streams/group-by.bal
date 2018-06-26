public type GroupBy object {
    public {
        function (StreamEvent[]) nextProcessorPointer;
        string[] groupByFields;
        map groupedStreamEvents;
    }

    new(nextProcessorPointer, groupByFields) {

    }

    public function performGrouping(StreamEvent[] streamEvents) {
        if (lengthof groupByFields > 0) {
            foreach streamEvent in streamEvents {
                string key = generateGroupByKey(streamEvent);
                if (!groupedStreamEvents.hasKey(key)) {
                    StreamEvent[] events = [];
                    groupedStreamEvents[key] = events;
                }
                StreamEvent[] groupedEvents = check <StreamEvent[]>groupedStreamEvents[key];
                groupedEvents[lengthof groupedEvents] = streamEvent;
            }

            foreach arr in groupedStreamEvents.values() {
                StreamEvent[] eventArr = check <StreamEvent[]>arr;
                nextProcessorPointer(eventArr);
            }
        } else {
            nextProcessorPointer(streamEvents);
        }
    }

    function generateGroupByKey(StreamEvent event) returns string {
        string key;

        map|error mappedStreamEvent = <map>event.eventObject;
        match mappedStreamEvent {
            map mapValue => {
                foreach field in groupByFields {
                    key += ", ";
                    string? fieldValue = <string>mapValue[field];
                    match fieldValue {
                        string value => {
                            key += value;
                        }
                        () => {

                        }
                    }
                }
            }
            error => {

            }
        }
        return key;
    }
};

public function createGroupBy(function(StreamEvent[]) nextProcPointer, string[] groupByFields) returns GroupBy {
    GroupBy groupBy = new(nextProcPointer, groupByFields);
    return groupBy;
}