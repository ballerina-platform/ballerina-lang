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

import ballerina/test;

type Person record {|
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
|};

type Student record {|
    string name = "";
    int age = 0;
|};

type Person2 record {|
    string name = "";
    int age = 0;
|};

type StructWithoutDefaults record {|
    string s = "";
    int a;
    float f = 0.0;
    boolean b = false;
    json j = {};
    byte[] blb = [];
|};

type T1 record {
    int x = 0;
    int y = 0;
};

type T2 record {|
    int x = 0;
    int y = 0;
    int z = 0;
|};
    
public type TX record {
    int x = 0;
    int y = 0;
    xml[] xmls = [xml `hello`];
};

type Manager record {
    string name = "";
    int age = 0;
    Manager? parent = ();
};

type Engineer record {
    string name = "";
    int age = 0;
    Engineer? parent = ();
};


function testIncompatibleJsonToStructWithErrors () returns (Person | error) {
    json j = {  name:"Child",
                age:25,
                parent:{
                    name:"Parent",
                    age:50,
                    parent:"Parent",
                    address:{"city":"Colombo", "country":"SriLanka"},
                    info:null,
                    marks:null
                },
                address:{"city":"Colombo", "country":"SriLanka"},
                info:{status:"single"},
                marks:[87, 94, 72]
    };
    Person p  = check j.cloneWithType(Person);
    return p;
}


function testEmptyJSONtoStructWithoutDefaults () returns (StructWithoutDefaults | error) {
    json j = {};
    var testStruct = check j.cloneWithType(StructWithoutDefaults);
    return testStruct;
}

function testEmptyMaptoStructWithoutDefaults () returns StructWithoutDefaults|error {
    map<anydata> m = {};
    var testStruct = check m.cloneWithType(StructWithoutDefaults);
    return testStruct;
}

function testArrayToJsonFail() returns json|error {
    TX[] x = [];
    TX a = {};
    TX b = {};
    a.x = 10;
    b.x = 15;
    x[0] = a;
    x[1] = b;
    return x.cloneWithType(json);
}

function testIncompatibleImplicitConversion() returns int|error {
    json operationReq = { "toInt": "abjd" };
    var abjd = check operationReq.toInt;
    return abjd.cloneWithType(int);
}

function testConvertRecordToRecordWithCyclicValueReferences() returns Engineer|error {
    Manager p = { name: "Waruna", age: 25, parent: () };
    Manager p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    Engineer|error e =  trap p.cloneWithType(Engineer); // Cyclic value will be check with isLikeType method.
    return e;
}

function testConvertRecordToJsonWithCyclicValueReferences() returns json|error {
    Manager p = { name: "Waruna", age: 25, parent: () };
    Manager p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    json j =  check trap p.cloneWithType(json); // Cyclic value will be check with isLikeType method.
    return j;
}

function testConvertRecordToMapWithCyclicValueReferences() returns map<anydata>|error {
    Manager p = { name: "Waruna", age: 25, parent: () };
    Manager p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    map<anydata> m =  check trap p.cloneWithType(AnydataMap); // Cyclic value will be check when stamping the value.
    return m;
}

function testConvertFromJsonWithCyclicValueReferences() {
    json p = { name: "Waruna", age: 25, parent: () };
    json p2 = { name: "Milinda", age: 25, parent: p };
    map<json> p3 = <map<json>> p2;
    p3["parent"] = p2;
    json p4 = p3;
    Engineer|error p5 = trap p4.fromJsonWithType(Engineer);
    error err = <error> p5;
    test:assertEquals(<string> checkpanic err.detail()["message"], "'map<json>' value has cyclic reference");
    test:assertEquals(err.message(),"{ballerina/lang.value}ConversionError");
}

type AnydataMap map<anydata>;
type T1_T2 [T1, T2];

type Boss record {
    Person3 man;
    string department;
};

type Factory record {|
    Person3 man1;
    Person3 man2;
    Boss man3;
    float grade;
    boolean permanant = false;
    Student1 intern;
    boolean...;
|};

type Person3 record {|
    string name;
    int age;
|};

type Apple record {
    string color;
};

type Orange record {|
    string colour;
|};

type Mango record {
    string taste;
    int amount;
};

type Student1 record {|
    string name;
    Apple|Orange|Mango fruit;
|};

function testConvertJsonToNestedRecordsWithErrors() {
    json j = {
        "man1": {
            "fname": "Jane",
            "age": "14"
        },
        "man2": {
            "name": 2,
            "aage": 14,
            "height":67.5
        },
        "man3": {
            "man": {
                "namee": "Jane",
                "age": "14",
                "height":67.5
            },
            "department": 4
        },
        "intern": {
            "name": 12,
            "fruit": {
                "color": 4,
                "amount": "five"
            }
        },
        "black": "color",
        "blue": 4,
        "white": true,
        "yellow": "color",
        "green": 4
    };

    Factory|error val = trap j.cloneWithType(Factory);

    error err = <error> val;
    string errorMsg = "'map<json>' value cannot be converted to 'Factory': " +
        "\n\t\tmissing required field 'grade' of type 'float' in record 'Factory'" +
        "\n\t\tmissing required field 'man1.name' of type 'string' in record 'Person3'" +
        "\n\t\tfield 'man1.fname' cannot be added to the closed record 'Person3'" +
        "\n\t\tfield 'man1.age' in record 'Person3' should be of type 'int'" +
        "\n\t\tmissing required field 'man2.age' of type 'int' in record 'Person3'" +
        "\n\t\tfield 'man2.name' in record 'Person3' should be of type 'string'" +
        "\n\t\tfield 'man2.aage' cannot be added to the closed record 'Person3'" +
        "\n\t\tfield 'man2.height' cannot be added to the closed record 'Person3'" +
        "\n\t\tmissing required field 'man3.man.name' of type 'string' in record 'Person3'" +
        "\n\t\tfield 'man3.man.namee' cannot be added to the closed record 'Person3'" +
        "\n\t\tfield 'man3.man.age' in record 'Person3' should be of type 'int'" +
        "\n\t\tfield 'man3.man.height' cannot be added to the closed record 'Person3'" +
        "\n\t\tfield 'man3.department' in record 'Boss' should be of type 'string'" +
        "\n\t\tfield 'intern.name' in record 'Student1' should be of type 'string'" +
        "\n\t\tfield 'intern.fruit.color' in record 'Apple' should be of type 'string'" +
        "\n\t\tmissing required field 'intern.fruit.colour' of type 'string' in record 'Orange'" +
        "\n\t\tfield 'intern.fruit.color' cannot be added to the closed record 'Orange'" +
        "\n\t\tfield 'intern.fruit.amount' cannot be added to the closed record 'Orange'" +
        "\n\t\tmissing required field 'intern.fruit.taste' of type 'string' in record 'Mango'" +
        "\n\t\tfield 'intern.fruit.amount' in record 'Mango' should be of type 'int'" +
        "\n\t\t...";
    test:assertEquals(<string> checkpanic err.detail()["message"], errorMsg);
    test:assertEquals(err.message(),"{ballerina/lang.value}ConversionError");
}
