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

public type SimpleSelect object {

    private function (StreamEvent[]) nextProcessorPointer;
    private function(StreamEvent o) returns map selectFunc;


    new(nextProcessorPointer, selectFunc) {
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] newStreamEventArr = [];
        int index = 0;
        foreach event in streamEvents {
            StreamEvent streamEvent = new((OUTPUT, selectFunc(event)), event.eventType, event.timestamp);
            newStreamEventArr[index] = streamEvent;
            index += 1;
        }
        if (index > 0) {
            nextProcessorPointer(newStreamEventArr);
        }

    }
};

public function createSimpleSelect(function (StreamEvent[]) nextProcPointer, function(StreamEvent o) returns map selectFunc)
        returns SimpleSelect {
    SimpleSelect simpleSelect = new(nextProcPointer, selectFunc);
    return simpleSelect;
}