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

type Student record {
    string name;
    int age;
    float gpa;
};

function testQueryExpression() {
    Student s1 = {name: "Foo", age: 1, gpa: 2.1};
    Student s2 = {name: "Bar", age: 2, gpa: 3.2};
    Student s3 = {name: "Baz", age: 3, gpa: 1.3};

    Student[] students = [s1, s2, s3];

    var x = from var st in students
        where st.name == "Foo"
        select {name: st.name};
}
