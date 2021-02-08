// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


type Person record {
    readonly int id;
    readonly string name;
    string lname;
};

type Annot record {
    string foo;
};

@v1 {
    foo: "bar"
}
public type T1 record {
    string name;
};

public annotation Annot v1 on type;

function test() {
    record {|
        string name;
        int age?;
    |} person = {name: "John", age: 20};

    string name = person.name;
    int? age = person?.age;
    string optName = person["name"];

    table<Person> key(id, name) tbl = table [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                             { id: 23 , name: "James" , lname: "Clark" }];
    Person p = tbl[13, "Sanjiva"];

    xml x = xml `<root attr="attr-val"><a></a><b></b></root>`;
    string|error val1 = x.attr;
    string|error|() val2 = x?.attr;

    T1 a = { name: "John" };
    typedesc<any> t = typeof a;
    Annot? annot = t.@v1;
}
