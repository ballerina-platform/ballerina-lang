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

type Person record {
    string name;
    int age;
};

function testFilter() returns boolean {
    boolean testPassed = true;
    Person[] d = [];
    Person gima = {name: "Gima", age: 100};
    Person mohan = {name: "Mohan", age: 200};
    Person grainier = {name: "Grainier", age: 150};
    Person chiran = {name: "Chiran", age: 75};
    Person sinthuja = {name: "Sinthuja", age: 150};
    d.push(gima);
    d.push(mohan);
    d.push(grainier);
    d.push(chiran);
    d.push(sinthuja);
    Person[] r = d.clone();
    foreach var s in r {

    }
    stream<Person> sss =  d.toStream();
    stream<Person> xxx = sss.filter(function (Person p) returns boolean {
        return p.age > 100 && p.name != "Chiran";
    });

    var x = <record {Person value;}>xxx.next();
    testPassed = testPassed && x.value == mohan;

    x = <record {Person value;}>xxx.next();
    testPassed = testPassed && x.value == grainier;

    x = <record {Person value;}>xxx.next();
    testPassed = testPassed && x.value == sinthuja;

    return testPassed;
}
