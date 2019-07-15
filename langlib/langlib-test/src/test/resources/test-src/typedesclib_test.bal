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

type Person record {
    string name;
    int age;
};

function testRecToJson() returns json|error {
    Person  p = {name: "N", age: 3};
    json|error j = typedesc<json>.constructFrom(p);
    return j;
}

function testOnTypeName() returns [Person|error, json|error] {
   json p = { name : "tom", age: 2};
   Person|error pp = Person.constructFrom(p);

   Person s = { name : "bob", age: 4};
   json|error ss = json.constructFrom(s);

   return [pp, ss];
}
