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

type T1 int|string|boolean;

type T2 1|2|3;

type T3 "A"|"B"|4;

type T4 "1"|"2"|T2;

type T5 1|2|("3"|"4");

type T6 T1|T2;

type T7 [int, string]|object {int age;};

type Person record {|
    string name;
    int age;
|};

type T8 map<string>|Person;

type T9 int[]|T8;

type Keyword KEY | boolean | "string" | 100 | "200" | true;
const KEY = "int";
