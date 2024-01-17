// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

json j = {
    n: table key(id) [
        {id: 1, firstName: "John", lastName: "Smith", salary: 100},
        {id: 2, firstName: "Fred", lastName: "Bloggs", salary: 200}
    ]
};

json j2 = <json> {
    n: table key(id) [
        {id: 1, firstName: "John", lastName: "Smith", salary: 100},
        {id: 2, firstName: "Fred", lastName: "Bloggs", salary: 200}
    ]
};

json j3 = {
    n: table [
        {id: 1, firstName: "John", lastName: "Smith", salary: 100},
        {id: 2, firstName: "Fred", lastName: "Bloggs", salary: 200}
    ]
};

json j4 = <json> {
    n: table key(id) []
};

json j5 = <json> {
    n: table []
};

public function convertTableTypeIntoJson() {
    json j = {
        n: table key(id) [
            {id: 1, firstName: "John", lastName: "Smith", salary: 100},
            {id: 2, firstName: "Fred", lastName: "Bloggs", salary: 200}
        ]
    };

    json j2 = <json> {
        n: table key(id) [
            {id: 1, firstName: "John", lastName: "Smith", salary: 100},
            {id: 2, firstName: "Fred", lastName: "Bloggs", salary: 200}
        ]
    };

    json j3 = {
        n: table [
            {id: 1, firstName: "John", lastName: "Smith", salary: 100},
            {id: 2, firstName: "Fred", lastName: "Bloggs", salary: 200}
        ]
    };

    json j4 = <json> {
        n: table key(id) []
    };

    json j5 = <json> {
        n: table []
    };
}
