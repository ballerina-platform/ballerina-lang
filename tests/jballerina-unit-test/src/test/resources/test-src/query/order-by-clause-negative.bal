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

type Customer record {|
    string name;
    Address address;
|};

type Address record {|
    int unitNo;
    string street;
|};

function testOrderByClauseWithInvalidOrderField() {
    Customer c1 = {name: "James", address: {unitNo: 1, street: "Main Street"}};
    Customer c2 = {name: "Frank", address: {unitNo: 2, street: "Main Street"}};
    Customer c3 = {name: "Nina", address: {unitNo: 3, street: "Palm Grove"}};

    Customer[] customerList = [c1, c2, c3];

    Customer[] opList = from var customer in customerList
        order by address
        select customer;
}

function testOrderByClauseWithComplexTypeFieldInOrderBy() {
    Customer c1 = {name: "James", address: {unitNo: 1, street: "Main Street"}};
    Customer c2 = {name: "Frank", address: {unitNo: 2, street: "Main Street"}};
    Customer c3 = {name: "Nina", address: {unitNo: 3, street: "Palm Grove"}};

    Customer[] customerList = [c1, c2, c3];

    Customer[] opList = from var customer in customerList
        order by customer.address
        select customer;
}

function testOrderByClauseWithArrayTypeFieldInOrderBy() {
    record {|(int|boolean)[] t; string s;|}[] data1 = [];

    _ = from var rec in data1
        order by rec.t descending
        select rec.s;

    record {|int k; [decimal|float, int] arr;|}[] data2 = [];

    _ = from var rec in data2
        order by rec.arr
        select rec.k;
}
