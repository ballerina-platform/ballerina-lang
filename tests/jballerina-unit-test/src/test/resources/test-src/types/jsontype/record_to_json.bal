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

type Foo record {|
    int i = 10;
    string s = "foo";
    float f = 12.34;
    decimal d = 23.456;
    boolean b = true;
    byte byt = 127;
    json j = {"name": "Pubudu"};
|};

function testClosedRecordToJson() {
    Foo f = {};
    json j = f;
    assert(<map<json>>{"i": 10, "s": "foo", "f": 12.34, "d": 23.456d, b: true, "byt": 127, "j": {"name": "Pubudu"}}, j);

    map<json> mJ = <map<json>>j;
    mJ["newField"] = 20;
}

function testOpenRecordToJson() {
    record {|
        string fname = "John";
        string lname = "Doe";
        string...;
    |} person = {"country": "Sri Lanka"};

    json j1 = person;
    assert(<json>{"fname": "John", "lname": "Doe", "country": "Sri Lanka"}, j1);

    map<json> mJ1 = <map<json>>j1;
    mJ1["city"] = "Colombo 3";
    assert(<json>{"fname": "John", "lname": "Doe", "country": "Sri Lanka", "city": "Colombo 3"}, mJ1);
}

type Address record {|
   string street;
   string city;
   string country;
|};

type Person record {|
    string name;
    record {|
        Address adr;
    |} residentialDetails;
|};

function testNestedRecordToJson() {
    Person p = {
        name: "John",
        residentialDetails: {
            adr: {
                street: "Palm Grove",
                city: "Colombo 3",
                country: "Sri Lanka"
            }
        }
    };

    json jPerson = p;

    json expectedP = {
        "name": "John",
        "residentialDetails": {
            "adr": {
                "street": "Palm Grove",
                "city": "Colombo 3",
                "country": "Sri Lanka"
            }
        }
    };

    assert(expectedP, jPerson);
}

function testNestedRecordModification() {
    record {|
        string name;
        record {|
            record {|
                int x;
                float...;
            |} nestedL2;
        |} nestedL1;
    |} rec = {
        name: "John",
        nestedL1: {
            nestedL2: {
                x: 100
            }
        }
    };

    json jR = rec;

    json nestedL2 = checkpanic jR.nestedL1.nestedL2;
    map<json> l2M = <map<json>>nestedL2;
    l2M["restF1"] = 12.34;

    json expected = {
        "name": "John",
        "nestedL1": {
            "nestedL2": {
                "x": 100,
                "restF1": 12.34
            }
        }
    };

    assert(expected, jR);
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}
