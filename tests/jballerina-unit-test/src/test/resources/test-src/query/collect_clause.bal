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
    var x16 = from var {price1} in input
                collect <decimal?> [price1].length() + avg(price1);    
    assertEquality(20.66666666666666666666666666666667d, x16);
}

function testEmptyGroups1() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    var x1 = from var {name, price1} in input
                where name == "X"
                collect [name];
    assertEquality([], x1);
    assertEquality(0, x1.length());
    var x2 = from var {name, price1} in input
                where name == "X"
                collect int:sum(price1);
    assertEquality(0, x2);
    int x3 = from var {name, price1} in input
                where name == "X"
                collect sum(price1);
    assertEquality(0, x3);
    var x4 = from var {name, price1} in input
                where name == "X"
                collect string:'join(",", name);
    assertEquality("", x4);    
    assertEquality(0, x4.length());    
    string x5 = from var {name, price1} in input
                where name == "X"
                collect string:'join(",", name);
    assertEquality("", x5); 
    [int...] x6 = from var {name, price1} in input
                    where name == "X"
                    collect [price1];
    assertEquality([], x6);
    int? x7 = from var {name, price1} in input
                where name == "X"
                collect max(price1);    
    assertEquality((), x7);
    assertEquality(x7 is (), true);
    int x8 = from var {name, price1} in input
                where name == "X"
                collect max(2, price1);    
    assertEquality(2, x8);
    int x9 = from var {name, price1} in input
                where name == "X"
                collect int:max(2);    
    assertEquality(2, x9);
    int x10 = from var {name, price1} in input
                where name == "X"
                collect int:max(2, ...[price1]);    
    assertEquality(2, x10);
    string x11 = from var {name, price1} in input
                where name == "X"
                collect xml:concat(name);   
    assertEquality("", x11);
    decimal? x12 = from var {name, price1} in input
                    where name == "X"
                    collect avg(price1);      
    assertEquality((), x12);
    var x13 = from var {name, price1, price2} in input
                where name == "X"
                collect [price1].push(...[price2]);      
    assertEquality((), x13);

    var input1 = [{name: "Saman", presence: true},
                    {name: "Saman", presence: true},
                    {name: "Kamal", presence: true},
                    {name: "Kamal", presence: true},
                    {name: "Kamal", presence: false},
                    {name: "Amal", presence: true}];
    boolean x14 = from var {name, presence} in input1
                    where name == "X"
                    collect some(presence);      
    assertEquality(false, x14);
    boolean x15 = from var {name, presence} in input1
                    where name == "X"
                    collect every(presence);      
    assertEquality(true, x15);

    var input2 = [{name: "Saman", price1: 11.1d},
                    {name: "Saman", price1: 15.4d},
                    {name: "Kamal", price1: 10.3d},
                    {name: "Kamal", price1: 9.9d},
                    {name: "Kamal", price1: 13.0d},
                    {name: "Amal", price1: 30.1d}];
    decimal? x16 = from var {name, price1} in input2
                    where name == "X"
                    collect avg(price1);
    assertEquality((), x16);
    decimal? x17 = from var {name, price1} in input2
                    where name == "X"
                    collect avg(3d, price1);
    assertEquality(3d, x17);
    string x18 = from var {name, price1} in input2
                    where name == "X"
                    collect concat(name);
    assertEquality("", x18);
    string x19 = from var {name, price1} in input2
                    where name == "X"
                    collect concat("Names :", name);
    assertEquality("Names :", x19);
    string? x20 = from var {name, price1} in input2
                    where name == "X"
                    collect 'join(name);
    assertEquality((), x20);
    assertEquality(x20 is (), true);
    string? x21 = from var {name, price1} in input2
                    where name == "X"
                    collect string:'join("n");
    assertEquality("", x21);
}

function testEmptyGroups2() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];    
    int? x1 = from var {name, price1, price2} in input
                where name == "X"
                collect max(price1) + max(price2);  
    assertEquality((), x1);
    int? x2 = from var {name, price1, price2} in input
                where name == "X"
                collect max(price1) - max(price2);  
    assertEquality((), x2);
    int? x3 = from var {name, price1, price2} in input
                collect max(price1) - max(price2);  
    assertEquality(17, x3);
    record {int? max;} x4 = from var {name, price1, price2} in input
                                where name == "X"
                                collect {max: max(price1)};
    assertEquality({max: null}, x4);
    record {decimal? avg;} x5 = from var {name, price1, price2} in input
                                where name == "X"
                                collect {avg: avg(price1)};
    assertEquality({avg: null}, x5);
    decimal x6 = from var {name, price1, price2} in input
                    collect <decimal> avg(price1);
    assertEquality(14.66666666666666666666666666666667d, x6);
    var x7 = from var {name, price1, price2} in input
                where name == "XX"
                collect from var item in [avg(price1)] select item;
    assertEquality([()], x7);
    decimal?[] x8 = from var {name, price1, price2} in input
                    where name == "XX"
                    collect from var item in [2] select avg(price1);
    assertEquality([()], x8);
    decimal? x9 = from var {name, price1, price2} in input
                    where name == "XX"
                    collect from var item in [2] collect avg(price1);
    assertEquality((), x9);
    decimal?[] x10 = from var {name, price1, price2} in input
                    where name == "XX"
                    collect from var item in [2] let var x = avg(price1) collect [x];
    assertEquality([], x10);
    var x11 = from var {price1} in input
                where price1 == -1
                collect <decimal?> [price1].length() + avg(price1);    
    assertEquality(x11 is (), true);    
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

function testMultipleCollect() {
    var input1 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 11},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];    

    var x1 = from var j in [1, 2]
                    collect (from var item in [3, 4] collect [item]);
    assertEquality([3, 4], x1);
    var x2 = from var j in [1, 2]
                    let var x = from var item in [3, 4] collect [item]
                    collect [x];
    assertEquality([[3, 4], [3, 4]], x2);
    var x3 = from var j in [1, 2]
                    let var x = from var item in [3, 4] collect sum(item)
                    collect sum(x);
    assertEquality(14, x3);
    var x4 = from var {name, price1, price2} in input1
                    let var x = from var item in [3, 4] collect sum(item)
                    collect sum(x) + sum(price1);
    assertEquality(108, x4);
    var x5 = from var {name, price1, price2} in input1
                    collect (from var x in [price1] collect sum(x));
    assertEquality(66, x5);
    var x6 = from var {name, price1, price2} in input1
                    collect sum(price1) + (from var x in [price1] collect sum(x));    
    assertEquality(132, x6);
    // TODO: Enable after https://github.com/ballerina-platform/ballerina-lang/issues/40413
    // var x7 = from var {name, price1, price2} in input1
    //                 collect (from var x in [price1] collect min(x)) + sum(price1);
    var x7 = from var {name, price1, price2} in input1  
                group by var x = from var {price1: p1} in input1 collect sum(p1)
                collect sum(x);
    assertEquality(66, x7);
    var x8 = from var {name, price1, price2} in input1  
                group by var x = from var {price1: p1} in input1 where name == "XX" collect avg(p1)
                collect [x];
    assertEquality([], x8);
    var x9 = from var {price1} in input1
                select from var _ in [price1] collect int:avg(price1);
    assertEquality([11d, 11d, 10d, 10d, 12d, 12d], x9);
    var x10 = from var {price1} in input1
                select (from var p in [price1] where p == -1 collect int:avg(price1));
    assertEquality([11d, 11d, 10d, 10d, 12d, 12d], x10);
    var x11 = from var {price1} in input1
                select (from var p in [price1] where p == -1 collect int:avg(p));
    assertEquality([null, null, null, null, null, null], x11);
    var x12 = from var {price1} in input1
                collect from var p in [price1] collect avg(p);    
    assertEquality(11d, x12);
    var x13 = from var {price1} in input1
                collect (from var _ in [price1] collect [price1]);    
    assertEquality([11, 11, 10, 10, 12, 12], x13);
}

function testDoClause() {
    var input1 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 11},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];  

    int[] arr = [];
    var _ = from var {name, price1, price2} in input1
                do {
                    var x = from var {name: n, price1: p1, price2: p2} in input1
                            collect sum(p1);
                    arr.push(x);
                };   
    assertEquality([66, 66, 66, 66, 66, 66], arr);
}

function testErrorSeq() {
    var input = [{name: "SAMAN", err: error("msg1")}, 
                    {name: "SAMAN", err: error("msg2")}]; 

    var x = from var {name, err} in input
                collect [err];
    assertEquality("msg1", x[0].message());
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
