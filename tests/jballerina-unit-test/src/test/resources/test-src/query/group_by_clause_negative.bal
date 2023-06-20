// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/lang.array;

function testSeqVarAsRequiredArg() {
    var input = [{name: "Saman", price: 11}, {name: "Kamal", price: 15}];
    var res = from var {name, price} in input
                group by name
                select array:length(price); // error
}

function testSeqVarInInvalidPositions1() {
    var res1 = from var {name, price} in [{name: "Saman", price: 11}, {name: "Kamal", price: 15}]
                group by name
                select price; // error
    int[] res2 = from var {name, price} in [{name: "Saman", price: 11}, {name: "Kamal", price: 15}]
                    group by name
                    select price; // error
    res1 = from var {name, price} in [{name: "Saman", price: 11}, {name: "Kamal", price: 15}]
            group by name
            let var x = price // error
            select x;
    res1 = from var {name, price} in [{name: "Saman", price: 11}, {name: "Kamal", price: 15}]
            group by name
            select price + 23; // error
    var res3 = from var {name, price1, price2} in [{name: "Saman", price1: 11, price2: 12}, {name: "Kamal", price1: 15, price2: 16}]
                group by name
                select [price1, price2]; // error
    var res4 = from var {name, price1, price2} in [{name: "Saman", price1: 11, price2: 12}, {name: "Kamal", price1: 15, price2: 16}]
                group by name
                select [(price2)]; // error
}

function testSeqVariableInGroupExpr() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var prices = from var {name, price} in input
                        group by name
                        let var p = [(price)] // error: group-expr is not supported
                        select p;                        
}

function testTupleDestructure() {
    _ = from var {name, price} in [{name: "Saman", price: 11}]
                group by name
                do {
                    [int, int...] x = [price]; // error
                };
    _ = from var {name, price} in [{name: "Saman", price: 11}]
                group by name
                do {
                    [int[]] x = [price];
                };
    _ = from var {name, price} in [{name: "Saman", price: 11}]
                group by name
                do {
                    [int, int] x = [price];
                };
}

function testRecordDestructure() {
    _ = from var {name, price} in [{name: "Saman", price: 11}]
                group by name
                do {
                    int[] prices;
                    {prices} = {prices: [price]}; // error
                };
}

function testSeqVarInInvalidPositions2() {
    var input = [{name: "Saman", price1: 11}];
    var xx = from var item in input
                group by string name = item.name
                let var x = item.price1 // error
                select [item];     
}

type RecOpt record {|
    string name;
    int price1;
    int price2?;
|};

function testOptionalInput() {
    RecOpt[] input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 14},
                    {name: "Amali", price1: 14}];

    var _ = from var {name, price1, price2} in input
                group by name
                select int:sum(price2); // error
}

function testSeqVarInInvalidPositions3() {
    var input = [{name: "Saman", price1: 11, price2: 11}];

    var x2 = from var {name, price1, price2} in input
                        group by name
                        select int:sum(price1 + 2);

    var x3 = from var {name, price1, price2} in input
                        group by name
                        select [price1 + 2];
}

function testSeqVarInInvalidPositions4() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    int[] sum = from var {name, price1, price2} in input
                    group by name
                    select int:sum(price1 + price2); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select int:sum(price1 + price2); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select int:sum(price1 + price2 + 3); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select int:sum(price1 + price2 + 3); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select 5.sum(price1 + price2 + 3); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select int:sum(price1, price2); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select 5.sum(...[price1], ...[price2]); // error
    sum = from var {name, price1, price2} in input
            group by var _ = true
            select 5.sum(2, price1, 3, 4); // error
    int[] l = from var {name, price1, price2} in input
                group by var _ = true
                select array:length(price1); // error            
    // TODO: Check this error
    // int[] sum = from var {name, price1, price2} in input
    //         group by var _ = true
    //         select int:min(n = 200, price1); // error            
}

function testTypeMatchInSeqVar() {
    decimal[] sum = from var {name, price} in [{name: "Saman", price: 23}]
                        group by name
                        select int:avg(...[price]); // error
}

function testInvalidGroupingKeys() {
    var input = [{name: "SAMAN", err: error("msg1")}, 
                    {name: "SAMAN", err: error("msg2")}]; 

    var x = from var {name, err} in input
                group by error str = err // error
                select [name];     
    var y = from var {name, err} in input
                group by err // error
                select [name];     
}
