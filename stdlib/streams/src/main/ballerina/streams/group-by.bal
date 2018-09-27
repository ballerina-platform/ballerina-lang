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

public type GroupBy object {
    public function (StreamEvent[]) nextProcessorPointer;
    public string[] groupByFields;
    public map groupedStreamEvents;

    new (nextProcessorPointer, groupByFields) {

    }

    public function process(StreamEvent[] streamEvents) {
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

        foreach field in groupByFields {
            key += ", ";
            string? fieldValue = <string> event.data[field];
            match fieldValue {
                string value => {
                    key += value;
                }
                () => {

                }
            }
        }

        return key;
    }
};

public function createGroupBy(function(StreamEvent[]) nextProcPointer, string[] groupByFields) returns GroupBy {
    GroupBy groupBy = new (nextProcPointer, groupByFields);
    return groupBy;
}