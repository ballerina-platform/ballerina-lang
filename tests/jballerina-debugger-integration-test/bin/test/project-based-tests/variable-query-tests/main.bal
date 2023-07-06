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

type Employee record {|
    readonly int id;
    string name;
|};

public function main() {
    int[] arrVar = [];
    json[] jsonVar = [];
    map<int> mapVar = {};
    table<Employee> key(id) tableVar = table [];
    [int...] tupleVar = [];
    xml xmlVar = xml ``;

    foreach var i in 0 ... 999 {
        arrVar[i] = i + 1;
        jsonVar[i] = i + 1;
        mapVar[i.toString()] = i + 1;
        tableVar.add({id: i + 1, name: "John"});
        tupleVar.push(i + 1);
        xmlVar = xmlVar + xml `<Children>${i + 1}</Children>`;
    }
}
