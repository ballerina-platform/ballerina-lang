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

import ballerina/io;

type Teacher record {|
   string firstName;
   string lastName;
|};

type Person record {|
   string firstName;
   string lastName;
   string country;
|};


function startFilterQuery() {

    Person p = {firstName:"Mohan", lastName: "Darshan", country: "LK"};
    Person[] personList = [];
    personList[0] = p;

    Teacher[] x = [];
    foreach var person in personList {
        //if(person.firstName == "ABC"){
        //    x[x.length()] = {
        //              firstName: person.firstName,
        //                lastName: person.lastName
        //           };
        //}
               x[x.length()] = {
                              firstName: person.firstName,
                                lastName: person.lastName
                           };
    }

    Teacher[] y =
            from var person in personList
            where person.firstName == "abc"
            select {
                   firstName: person.firstName,
                   lastName: person.lastName
                   };


    io:println(x);
}