//  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
//
//  WSO2 LLC. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing,
//  software distributed under the License is distributed on an
//  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  KIND, either express or implied. See the License for the
//  specific language governing permissions and limitations
//  under the License.

import ballerina/test;

type Employee record {|
    int id;
    string name;
|};

function testFrozenAnyArrayElementUpdate() returns error? {
    error? actualError = trap frozenAnyArrayElementUpdate();
    test:assertTrue(actualError is error);
    error e = <error>actualError;
    test:assertEquals(e.message(), "{ballerina/lang.map}InherentTypeViolation");
    test:assertEquals(e.detail()["message"],
        "cannot update 'readonly' field 'name' in record of type '(Employee & readonly)'");
}

function frozenAnyArrayElementUpdate() returns error? {
    Employee e1 = {name: "Em", id: 1000};
    anydata[] i1 = [e1];
    anydata[] i2 = i1.cloneReadOnly();
    Employee e2 = check trap <Employee>i2[0];
    e2["name"] = "Zee";
    return ();
}

function frozenTupleUpdate() {
    Employee e1 = {name: "Em", id: 1000};
    [int, Employee] t1 = [1, e1];
    [int, Employee] t2 = t1.cloneReadOnly();
    Employee e2 = {name: "Zee", id: 1200};
    t2[1] = e2;
}
