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

type PersonObj object { 
    public string name = "Waruna";
    public int age = 10;
};

type Person record {
    string name = "";
    int age = 0;
    Person? parent = ();
    json info?;
    map<anydata>? address?;
    int[]? marks?;
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
    Person[]? children?;
    !...;
};

type Person2 record {
    string name = "";
    int age = 0;
    !...;
};

type Person3 record {
    string name = "";
    int age = 0;
    string gender = "";
    !...;
};

function testFloatToIntWithMultipleArguments() returns int {
    float a = 5.0;
    return int.convert(a, a);
}

function testFloatToIntWithNoArguments() {
    float a = 5.0;
    return int.convert();
}

function testObjectToJson() returns json|error {
    PersonObj p = new PersonObj();
    return json.convert(p);
}

function testStructToJsonConstrained1() returns json|error {
    Person p = { name: "Child",
        age: 25,
        parent: { name: "Parent", age: 50 },
        address: { "city": "Colombo", "country": "SriLanka" },
        info: { status: "single" },
        marks: [87, 94, 72]
    };
    json<Person2> j = json<Person2>.convert(p);
    return j;
}

function testStructToJsonConstrainedNegative() returns json {
    Person2 p = {   name:"Child",
                    age:25
                };
    json<Person3> j = ();
    var result = json<Person3>.convert(p);
    if (result is json<Person3>) {
        j = result;
    } else if (result is error) {
        panic result;
    }
    return j;
}

function testTypeCheckingRecordToMapConversion() returns map<int>|error {
    Person2 p = { name: "Supun" };
    return map<int>.convert(p);
}
