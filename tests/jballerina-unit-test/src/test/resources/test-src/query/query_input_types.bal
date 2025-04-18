// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org)
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public type MyRecord record {
    int id;
    string name;
};

public function testArrayQuery() returns int[] {
    int[] arr = [1, 2, 3];
    return from var item in arr
           select item;
}

public function testMapQuery() returns int[] {
    map<int> m = {"key1": 1, "key2": 2, "key3": 3};
    return from int i in m
           select i;
}

public function testRecordQuery() returns MyRecord[] {
    MyRecord[] records = [
        {id: 1, name: "record1"},
        {id: 2, name: "record2"}
    ];
    return from var rec in records
           select rec;
}

public function testStringQuery() returns string[] {
    string str = "Ballerina";
    return from var ch in str
           select ch.toString();
}
