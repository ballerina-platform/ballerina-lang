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

type Emp record {
string name;
string dep;
};

type GlobalTable table<Person> key(name);

type StrTable table<Emp> key(name);

GlobalTable tab = table [
{ name: "AAA", age: 31 },
{ name: "BBB", age: 34 }
];


function testTableLength() returns int {
return tab.length();
}

//function getKey() returns Person? {
//    return tab.get("AAA");
//}

//function testNextKey() returns int {
//return tab.nextKey();
//}
//
//


function testForeach() returns string {
string result = "";

tab.forEach(function (Person? x) {
if (x is Person) {
result += x.name;
}
});

return result;
}

function testFilter() returns string {

GlobalTable  filteredT = tab.filter(function (Person p) returns boolean { return p.name ==  "AAA"; });
string result = "";

filteredT.forEach(function (Person? x) {
if (x is Person) {
result += x.name;
}
});

return result;
}


function testMap() returns string {
StrTable newTbl = tab.'map(function (Person x) returns Emp {
return {name: x.name, dep : "HR"};
});

                                                                                                     string result = "";
newTbl.forEach(function (Emp? x) {
                                    if (x is Emp) {
result += x.name;
            }
            });
return result;
}


//function testIterator() returns string {
//    string[] arr = ["Hello", "World!", "From", "Ballerina"];
//    abstract object {
//                 public function next() returns record {| Person value; |}?;
//} itr = tab.iterator();
//
//record {| Person value; |}|() elem = itr.next();
//
//string result = "";
//
//while (elem is record {| Person value; |}) {
//result += elem.value.name;
//               elem = itr.next();
//                              }
//
//                              return result;
//}