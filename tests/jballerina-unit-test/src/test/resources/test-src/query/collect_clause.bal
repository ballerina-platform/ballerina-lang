// Copyright (c) 2023 WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

function testListConstructor() {
    int[] x1 = from var {x} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                collect [x];
    assertEquality([2, 4], x1);
    string[] x2 = from var {x} in [{"x": "2", "y": "3"}, {"x": "4", "y": "5"}]
                    collect [x];
    assertEquality(["2", "4"], x2);
    int[] x3 = from var {x} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                where x == 2
                collect [x];
    assertEquality([2], x3);
    int[][] x4 = from var {x} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                    collect [[x]];
    assertEquality([[2, 4]], x4);
    var x5 = from var {x} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                collect [x];
    assertEquality([2, 4], x5);
    var x6 = from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                collect [...[x], ...[y]];
    assertEquality([2, 4, 3, 5], x6);
    var x7 = from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}, {"x": 6, "y": 7}]
                collect [x].length();
    assertEquality(3, x7);
    var x8 = from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}, {"x": 6, "y": 7}]
                collect [x].filter(n => n > 2);
    assertEquality([4, 6], x8);    
    var x9 = from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}, {"x": 6, "y": 7}]
                collect [x].some(n => n > 2);
    assertEquality(true, x9);
    string[] x10 = from var {x} in [{"x": "2", "y": "3"}, {"x": "4", "y": "5"}, {"x": "2", "y": "3"}]
                    collect [x];
    assertEquality(["2", "4", "2"], x10);    
    var x11 = from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                let var sum = x + y
                collect [sum];
    assertEquality([5, 9], x11);    
    var x12 = from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}]
                let var sum = x + y
                collect [...[sum], ...[x]];
    assertEquality([5, 9, 2, 4], x12);
}

function testInvocation() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    var x1 = from var {name, price1} in input
                collect int:sum(price1);
    assertEquality(88, x1);
    var x2 = from var {name, price1, price2} in input
                collect int:sum(price1) + int:sum(price2);
    assertEquality(158, x2);
    var x3 = from var {name, price1} in input
                collect sum(price1);
    assertEquality(88, x3);
    record { int sum; } x4 = from var {name, price1} in input
                collect {"sum": sum(price1)};
    assertEquality({"sum": 88}, x4);
    var x5 = from var {name, price1} in input
                collect {"sum": sum(price1)};
    assertEquality({"sum": 88}, x5);
    var x6 = from var {name, price1, price2} in input
                collect {"sum": sum(price1) + sum(price2)};
    assertEquality({"sum": 158}, x6);
    var x7 = from var {name, price1} in input
                collect min(200, price1);    
    assertEquality(9, x7);
    var x8 = from var {name, price1} in input
                collect min(-1, price1);    
    assertEquality(-1, x8);
    var x9 = from var {name, price1} in input
                collect [int:sum(price1)];
    assertEquality([88], x9);
    var x10 = from var {name, price} in [{name: "Saman", price: 1}, {name: "Amal", price: 2}, {name: "Saman", price: 3}]
                collect string:'join("_", name);
    assertEquality("Saman_Amal_Saman", x10);
    var x11 = from var {name, price} in [{name: "Saman", price: 1}, {name: "Amal", price: 2}, {name: "Saman", price: 3}]
                collect 'join("_", name);
    assertEquality("Saman_Amal_Saman", x11);
    var x12 = from var {name, price1} in input
                let var p1 = price1
                collect min(-1, p1);    
    assertEquality(-1, x12);
    var x13 = from var {name, price1, price2} in input
                collect int:avg([price2][0], ...[price2].slice(1, [price2].length()));
    assertEquality(11.66666666666666666666666666666667d, x13);
    var x14 = from var {name, price1} in input
                collect min(price1);    
    assertEquality(9, x14);
    var x15 = from var {name, price1} in input
                collect avg(price1);    
    assertEquality(14.66666666666666666666666666666667d, x15);
}

// TODO: Check this after finalizing empty groups issue
function testEmptyGroups() {
    // var input = [{name: "Saman", price1: 11, price2: 11},
    //                 {name: "Saman", price1: 15, price2: 12},
    //                 {name: "Kamal", price1: 10, price2: 13},
    //                 {name: "Kamal", price1: 9, price2: 12},
    //                 {name: "Kamal", price1: 13, price2: 9},
    //                 {name: "Amal", price1: 30, price2: 13}];
    // var x1 = from var {name, price1} in input
    //             where name == "X"
    //             collect [name];
    // assertEquality([], x1);
    // assertEquality(0, x1.length());
    // var x2 = from var {name, price1} in input
    //             where name == "X"
    //             collect int:sum(price1);
    // assertEquality(0, x2);
    // var x3 = from var {name, price1} in input
    //             where name == "X"
    //             collect sum(price1);
    // assertEquality(0, x3);
    // var x4 = from var {name, price1} in input
    //             where name == "X"
    //             collect string:'join(",", name);
    // assertEquality("", x4);    
    // assertEquality(0, x4.length());    
    // string x5 = from var {name, price1} in input
    //             where name == "X"
    //             collect string:'join(",", name);
    // assertEquality("", x5); 
}

function testGroupByAndCollectInSameQuery() {
    var input1 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 11, price2: 13},
                    {name: "Amal", price1: 11, price2: 15}];
    
    var x1 = from var {name, price1, price2} in input1
                group by name
                collect [name];    
    assertEquality(["Saman", "Kamal", "Amal"], x1);

    string[] x2 = from var {name, price1, price2} in input1
                    group by name
                    collect [name];    
    assertEquality(["Saman", "Kamal", "Amal"], x2);

    var x3 = from var {name, price1, price2} in input1
                group by name
                let var p1 = [price1]
                collect [p1];    
    assertEquality([[11, 11], [10, 10, 10], [11, 11]], x3);

    int[][] x4 = from var {name, price1, price2} in input1
                    group by name
                    let var p1 = [price1]
                    collect [p1];    
    assertEquality([[11, 11], [10, 10, 10], [11, 11]], x4);

    var x5 = from var {name, price1, price2} in input1
                group by name
                collect string:'join("_", name);
    assertEquality("Saman_Kamal_Amal", x5);

    var x6 = from var {name, price1, price2} in input1
                group by price1
                group by price1
                collect int:sum(price1);
    assertEquality(21, x6);

    var input2 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 11},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];    

    var x7 = from var {name, price1, price2} in input2
                group by price1
                let var sumP2 = sum(price2)
                group by var p2 = [price2]
                let var sumP2P2 = sum(sumP2)
                collect sum(sumP2P2);
    assertEquality(74, x7);

    var x8 = from var {name, price1, price2} in input2
                group by price1
                let var n1 = string:'join("_", name)
                group by var p2 = sum(price2)
                let var n2 = string:'join(" ", n1)
                group by p2
                let var n3 = string:'join("@", n2)
                collect string:'join("&", n3);
    assertEquality("Saman_Saman Kamal_Kamal&Amal_Amal", x8);

    var x9 = from var {name, price1, price2} in input2
                group by price1
                let var n = [name]
                group by n
                let var n1 = string:'join("@", ...n)
                collect string:'join("&", n1);
    assertEquality("Saman@Saman&Kamal@Kamal&Amal@Amal", x9);

    var input3 = [{name: "Saman", price1: 11, price2: 12},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];   

    string x10 = from var {name, price1, price2} in input3
                group by price1, price2
                let var n = string:'join("@", name)
                group by var _ = price1 + price2
                let string n1 = string:'join("_", n)
                collect string:'join("&", n1);
    assertEquality("Saman@Saman@Kamal&Kamal&Amal&Amal", x10); 
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

// TODO: test group by collect combinations
// TODO: multiple collect clause (in same query(error), in different sub queries in main query)
