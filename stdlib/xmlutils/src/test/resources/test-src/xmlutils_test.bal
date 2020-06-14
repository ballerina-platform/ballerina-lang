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

import ballerina/xmlutils;

type Person record {
    int id;
    int age = -1;
    decimal salary;
    string name;
    boolean married;
};

function testFromJSON() returns xml|error {
    json data = {
        name: "John",
        age: 30
    };
    xml|error x = xmlutils:fromJSON(data);
    return x;
}

public function testFromTable() returns string {
    table<Person> personTable = table[ { id: 1, age: 30,  salary: 300.5, name: "Mary", married: true },
          { id: 2, age: 20,  salary: 300.5, name: "John", married: true }
        ];

    return xmlutils:fromTable(personTable).toString();
}
