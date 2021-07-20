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

function getPerson(string name, int age) {
    Address adrs = {city: "Colombo 3", country: "Sri Lanka"};

    Person p = {
        name: name,
        age,
        [foo()]: "FOO",
        ...adrs
    };
}

function foo() returns string => "foo";

type Person record {
    string name;
    int age;
};

type Address record {
    string city;
    string country;
};
