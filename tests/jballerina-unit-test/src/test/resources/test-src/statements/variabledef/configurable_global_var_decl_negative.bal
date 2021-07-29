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

// Configurable variable must be initialized
configurable int x;
// Configurable variable cannot be declared with var
configurable var a = 5;
// Configurable variable type must be a subtype of anydata.
configurable object {int a;} b = object {int a = 5;};
// Configurable variable type must be a subtype of readonly.
configurable record {int c;} d = {c:5};

function foo() {
    // Cannot declare configurable variable locally
    configurable int e = 6;
}
// TODO: remove this after runtime supports all configurable types
configurable json & readonly j2 = {name: "apple", color: "red"};

// configurable var not allowed with complex variables
configurable [int, string] [intVar, stringVar] = ?;

// 'final' qualifier not allowed: configurable variables are implicitly final
final configurable string systemAlias = "Ballerina";

// Unsupported configurable types from runtime

type Person record {|
    readonly string name;
|};

type Person1 record {|
    json jsonField;
    anydata anydataField;
|};

type Person2 record {|
    int|json unionField;
|};

type Person3 record {|
    json[] jsonArr;
|};

type Person4 record {|
    string name;
    Person1 person;
|};

type Person5 record {|
    json field1;
    json field2;
|};

type Colors "Red" | "Green";

// Unsupported array of table
configurable table<Person> key(name)[] tableArr1 = ?;
configurable (table<Person> key(name) & readonly)[] tableArr2 = ?;

// Unsupported record field
configurable Person1 person1 = ?;
configurable Person2 person2 = ?;
configurable Person3 person3 = ?;
configurable Person4 person4 = ?;
configurable Person5 person5 = ?;

// Unsupported table constraint
configurable table<map<json>> tableVar1 = ?;
configurable table<Person1> tableVar2 = ?;
configurable table<json> tableVar3 = ?;

// Unsupported array constraint
configurable json[] arrayVar = ?;

// Unsupported map constraint
configurable map<json> & readonly mapVar = ?;

// Unsupported union types
configurable string|json unionVar1 = ?;
configurable json unionVar2 = ?;

// Unsupported tuple type
configurable [int, string] tupleVar = ?;
