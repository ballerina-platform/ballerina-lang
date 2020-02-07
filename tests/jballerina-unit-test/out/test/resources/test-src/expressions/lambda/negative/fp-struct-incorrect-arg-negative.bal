// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    string fname;
    string lname;
    function (string, string) returns (string) getName?;
};

function getFullName (string f, string l) returns (string) {
    return l + ", " + f;
}

function test1() returns [string, string] {
    Person bob = {fname:"bob", lname:"white"};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getName(bob.fname, bob.lname);
    string y = tom.getName(tom.fname, tom);
    return [x, y];
}