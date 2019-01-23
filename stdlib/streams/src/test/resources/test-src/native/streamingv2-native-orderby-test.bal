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

import ballerina/runtime;
import ballerina/streams;

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type TeacherOutput record {
    string name;
    int age;
    int sumAge;
    int count;
};

int index = 0;
stream<Teacher> inputStream = new;
stream<TeacherOutput> outputStream = new;

TeacherOutput[] globalEmployeeArray = [];

public function startOrderByQuery() returns any {

    Teacher[] teachers = [];
    Teacher t1 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t2 = { name: "Raja", age: 45, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher t3 = { name: "Lahiru", age: 45, status: "single", batch: "LK2014", school: "Hindu College" };
    teachers[0] = t1;
    teachers[1] = t2;
    teachers[2] = t2;
    teachers[3] = t1;
    teachers[4] = t3;

    teachers[5] = t1;
    teachers[6] = t2;
    teachers[7] = t1;
    teachers[8] = t3;
    teachers[9] = t1;

    foo();

    outputStream.subscribe(function (TeacherOutput e) {printTeachers(e);});
    foreach var t in teachers {
        runtime:sleep(1);
        inputStream.publish(t);
    }

    int count = 0;
    while (true) {
        runtime:sleep(500);
        count += 1;
        if ((globalEmployeeArray.length()) == 10 || count == 10) {
            break;
        }
    }

    return globalEmployeeArray;
}


//  ------------- Query to be implemented -------------------------------------------------------
//  from inputStream where inputStream.age > getValue()
//  select inputStream.name, inputStream.age, sum (inputStream.age) as sumAge, count() as count
//  group by inputStream.name
//      => (TeacherOutput [] o) {
//            outputStream.publish(o);
//      }
//

function getValue(int a) returns int {
    return a;
}

function foo() {

    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach var m in events {
            // just cast input map into the output type
            TeacherOutput t = <TeacherOutput>TeacherOutput.stamp(m.clone());
            outputStream.publish(t);
        }
    };

    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    streams:Sum sumAggregator = new();
    streams:Count countAggregator = new();
    streams:Aggregator[] aggregatorArr = [];
    aggregatorArr[0] = sumAggregator;
    aggregatorArr[1] = countAggregator;

    (function (map<anydata>) returns anydata)?[] fields = [function (map<anydata> x) returns anydata {
        return getValue(<int>x["OUTPUT.age"]);
    }];

    streams:OrderBy orderByProcess =
    streams:createOrderBy(function (streams:StreamEvent?[] e) {outputProcess.process(e);}, fields, ["descending"]);

    streams:Select select = streams:createSelect(function (streams:StreamEvent?[] e) {orderByProcess.process(e);},
        aggregatorArr,
        [function (streams:StreamEvent e) returns anydata {
            return e.data["inputStream.name"];
        }],
        function (streams:StreamEvent e, streams:Aggregator[] aggregatorArr1) returns map<anydata> {
            streams:Sum sumAggregator1 = <streams:Sum>aggregatorArr1[0];
            streams:Count countAggregator1 = <streams:Count>aggregatorArr1[1];
            // got rid of type casting
            return {
                "name": e.data["inputStream.name"],
                "age": e.data["inputStream.age"],
                "sumAge": sumAggregator1.process(e.data["inputStream.age"], e.eventType),
                "count": countAggregator1.process((), e.eventType)
            };
        }
    );

    streams:Window tmpWindow = streams:lengthBatch([5], nextProcessPointer = function (streams:StreamEvent?[] e)
        {select.process(e);});

    streams:Filter filter = streams:createFilter(function (streams:StreamEvent?[] e) {tmpWindow.process(e);},
        function (map<anydata> m) returns boolean {
            // simplify filter
            return <int>m["inputStream.age"] > 25;
        }
    );

    inputStream.subscribe(function (Teacher t) {
            // make it type unaware and proceed
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(t, "inputStream");
            filter.process(eventArr);
        }
    );
}

function printTeachers(TeacherOutput e) {
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray(TeacherOutput e) {
    globalEmployeeArray[index] = e;
    index = index + 1;
}
