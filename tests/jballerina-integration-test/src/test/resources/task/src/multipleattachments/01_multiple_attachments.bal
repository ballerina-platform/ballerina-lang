// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/task;

string name = "John";
int age = 0;
int acNumber = 100;
float balance = 0.00;

public type Person record {
    string name;
    int age;
};

public type Account record {
    int number;
    float balance;
};

public function main() {
    Person sam = {name: "Sam", age: 29};
    Account acc = {number: 150590, balance: 11.35};
    task:TimerConfiguration timerConfiguration = {intervalInMillis: 1000};
    task:Scheduler timer = new (timerConfiguration);
    var attachResult = timer.attach(TimerService, sam, acc);
    var startResult = timer.start();
}

service TimerService = service {
    resource function onTrigger(Person p, Account a) {
        name = <@untainted>p.name;
        age = <@untainted>p.age;
        acNumber = <@untainted>a.number;
        balance = <@untainted>a.balance;
    }
};

function getResult() returns string {
    return "Name: " + name + " Age: " + age.toString() + " A/C: " + acNumber.toString() + " Balance: " + balance
        .toString();
}

listener http:Listener multipleAttachmentsListener = new (15007);

@http:ServiceConfig {
    basePath: "/"
}
service MultipleAttachmentsService on multipleAttachmentsListener {
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function getMultipleAttachmentResult(http:Caller caller, http:Request request) {
        var result = caller->respond(getResult());
    }
}
