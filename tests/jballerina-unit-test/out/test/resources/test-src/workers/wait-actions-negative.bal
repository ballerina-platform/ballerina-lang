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

type firstRec record {
    int id = 1;
    string name = "default";
};

type secondRec record {
    int f1 = 1;
    string f2 = "default";
};

type thirdRec record {
    int f1;
    string f2;
    int f4?;
};

type sealedRec record {|
    int id = 0;
    string name = "default";
|};

type restRec1 record {|
    int id = 0;
    string name = "default";
    int...;
|};

type restRec2 record {|
    int id = 0;
    string name = "default";
    string...;
|};

function waitForOneTest() {
    future< int > f1 = start getId();
    future< string > f2 = start getName();
    future< boolean > f3 = start getStatus();
    future< int > f4 = start getId();

    string s3 = wait f1;
    wait f2;
}

function waitForAnyTest() {
    future<int> f1 = start getId();
    future<string> f2 = start getName();
    future<boolean> f3 = start getStatus();
    future<int> f4 = start getId();

    int result1 = wait f1|f4 |f2;
    int|boolean result2 = wait f1 |f2|f3;
    map<int|boolean> result3 = wait f1 |f2|f3;
    future<int> result4 = f1|f4;
    int|string result5 = wait ((f1|f2) ? f2 : f1) |f4|f2;// f1 and f2 cannot be future types
    future< int|string> result6 = wait f1|f2;

}

function waitForAllTest() {
    future<int> f1 = start getId();
    future<string> f2 = start getName();
    future<boolean> f3 = start getStatus();
    future<int> f4 = start getId();

    map<int> result7 = wait {f1, f2};
    map<boolean|string> result8 = wait {f1, f4};
    record { int f1; int f2;} result9 = wait {f1, f2};
    record { int f1; string f2;} result10 = wait {f1, f2, f4};
    record { int f1; string f3;} result11 = wait {f1, f2};
    thirdRec result12 = wait {f1: f1};
    sealedRec result13 = wait {id: f1, name: f2, status: f3};
    sealedRec result14 = wait {id: f1, status: f2};
    restRec1 result15 = wait {id: f1, name: f2, city: f2};
    restRec2 result16 = wait {id: f1, name: f2, age: f4};
}

function print(string str) {
    string result = str.toUpperAscii();
}

function getId() returns int {
    return 10;
}

function getName() returns string {
    return "Natasha";
}

function getStatus() returns boolean {
    return true;
}

function getStdId() returns future<int> {
    future <int> id = start getId();
    return id;
}
