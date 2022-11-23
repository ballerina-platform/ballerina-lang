// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testGroupbyExpressionAndSelectWithGroupingKeysNegative1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] names = from var {name} in input
                    group by name
                    select name; // @output ["Saman", "Kamal"]
}

function testGroupbyExpressionAndSelectWithGroupingKeysNegative2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 11}];
    string[][] names = from var {name, price} in input
                        group by name, price
                        select [name, price]; // @output [["Saman", 11], ["Saman", 12], ["Kamal", 11]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] names = from var {name, price} in input
                        group by name
                        select [price]; // @error
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] names = from var {name, price} in input
                        group by name
                        select price; // @error
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var names = from var {name, price} in input
                        group by name
                        select [price]; // @error not supported lhs var
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative4() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[][] names = from var {name, price} in input
                        group by name
                        select [price]; // @error
}


function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative5() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    record {int[] price;} names = from var {name, price} in input
                                    group by name
                                    select {price: price}; // @error
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative6() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int][] names = from var {name, price1, price2} in input
                        group by name
                        select [price1, price2]; // @error
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative7() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    record {int[] price1; int[] price2;} names = from var {name, price1, price2} in input
                                                    group by name
                                                    select {price1, price2}; // @error
}

function testGroupbyExpressionAndSelectWithNonGroupingKeysNegative8() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] names = from var {name, price} in input
                        group by name
                        select [price, 101]; // @error
}

function testExpressionInGroupbySelectWithGroupingKeysWithTableResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    table<record {|readonly int id; string lname;|}> key(id) t = table key(id) from var {id, fname, lname} in personList
                                                                                    group by var i = id, var ln = lname // error
                                                                                    select {id: i, lname: ln};
}