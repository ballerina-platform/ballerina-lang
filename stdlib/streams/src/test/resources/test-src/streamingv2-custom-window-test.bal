// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/runtime;

// Custom window implementation that injects status and phoneNo attributes to the stream.
public type CustomWindow object {
    public any[] windowParameters;
    public function (streams:StreamEvent?[])? nextProcessPointer;

    public function __init(function (streams:StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        // do nothing.
    }

    public function process(streams:StreamEvent?[] streamEvents) {
        streams:StreamEvent?[] outputEvents = [];
        foreach var event in streamEvents {
            event.addAttribute("status", "single");
            event.addAttribute("phoneNo", "123456");
            outputEvents[outputEvents.length()] = event;
        }
        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (streams:StreamEvent?[])) {
            nextProcessFuncPointer.call(outputEvents);
        }
    }

    public function getCandidateEvents(streams:StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (streams:StreamEvent?, streams:StreamEvent?)[] {
        (streams:StreamEvent?, streams:StreamEvent?)[] events = [];
        return events;
    }

};

public function customWindow(any[] windowParameters,
                            function (streams:StreamEvent[])? nextProcessPointer = ()) returns streams:Window {
    CustomWindow customWindow1 = new(nextProcessPointer, windowParameters);
    return customWindow1;
}

// Declare records types required with streams.
type Person record {
    string name;
    int age;
    string status;
    string address;
    string phoneNo;
};

type Child record {
    string name;
    int age;
    string city;
    string status?;
    string phoneNo?;
};

int index = 0;
stream<Person> personStream = new;
stream<Child> childrenStream = new;
Person[] personArray = [];

function startSelectQuery() returns (Person[]) {
    Child[] childArray = [];
    Child t1 = { name: "Grainier", age: 27, city: "Mountain View" };
    Child t2 = { name: "Mohan", age: 30, city: "Memphis" };
    Child t3 = { name: "Gimantha", age: 29, city: "Houston" };

    childArray[0] = t1;
    childArray[1] = t2;
    childArray[2] = t3;
    initFilterQuery();

    personStream.subscribe(printPerson);

    foreach var c in childArray {
        childrenStream.publish(c);
    }

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((personArray.length()) == 3) {
            break;
        }
    }
    return personArray;
}

function initFilterQuery() {
    forever {
        from childrenStream window customWindow()
        select childrenStream.name, childrenStream.age, childrenStream.status as status,
        childrenStream.city as address, childrenStream.phoneNo as phoneNo
        => (Person[] persons) {
            foreach var p in persons {
                personStream.publish(p);
            }
        }
    }
}

function printPerson(Person p) {
    addToPersonArray(p);
}

function addToPersonArray(Person p) {
    personArray[index] = p;
    index = index + 1;
}
