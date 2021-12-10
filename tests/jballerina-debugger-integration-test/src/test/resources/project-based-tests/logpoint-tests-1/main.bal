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

type Employee record {
    string name;
    int id;
};

type Person record {
    string name;
};

final Employee moduleEmployee = {name: "John", id: 2102};

public function main() {
    int x = 5;
    int y = 10;
    int z = 20;

    while (y > 0) {
        y = y - 1;
    }

    map<string> countryCapitals = {
        "USA": "Washington, D.C.",
        "Sri Lanka": "Colombo",
        "England": "London"
    };

    foreach var capital in countryCapitals {
        z = z + 1;
    }

    Employee e1 = getModuleEmployee();
    Person e2 = getModuleEmployee();
    Employee e3 = {name: "John", id: 2102};

    x = 6;
    x = 7;
    x = 8;
    x = 9;
}

function getModuleEmployee() returns Employee {
    return moduleEmployee;
}
