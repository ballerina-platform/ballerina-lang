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

import ballerina/lang.value;

function testGroupByExpressionAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    int[] sum = from var {name, price} in input
                        group by name
                        select sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by var _ = true, name
                        select int:sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        let var x = int:sum(price)
                        select x;
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        let var x = sum(price)
                        select x;
    assertEquality([35, 11], sum);
    var r = from var {name, price} in input
                        group by name
                        select {price: int:sum(price)};
    assertEquality([{price: 35}, {price: 11}], r);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(...[price]);
    assertEquality([35, 11], sum);
    // TODO: Check whether this is supported
    // sum = from var {name, price} in input
    //                     group by name
    //                     select sum(...[price]);
    // assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(...[price]);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(2, ...[price]);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(2, ...[price]);
    assertEquality([39, 15], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(price);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(2, price);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(2, price);
    assertEquality([39, 15], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    int[] sum = from var {name, price} in input
                        group by name
                        select sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
            group by name
            select 2.sum(price);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(...[price]);
    assertEquality([35, 11], sum);
    // TODO: Check whether this is supported
    // sum = from var {name, price} in input
    //                     group by name
    //                     select sum(...[price]);
    // assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(...[price]);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(2, ...[price]);
    assertEquality([37, 13], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(2, ...[price]);
    assertEquality([39, 15], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 3}, {name: "Saman", price: 2}];
    int[] res = from var {name, price} in input
                    group by name
                    select max(2, price);
    assertEquality([12, 11], res);
    res = from var {name, price} in input
            group by name
            select max(20, price);
    assertEquality([20, 20], res);
    res = from var {name, price} in input
            group by name
            select int:max(20, price);
    assertEquality([20, 20], res);
    res = from var {name, price} in input
            group by name
            select 2.max(price);
    assertEquality([12, 11], res);
    res = from var {name, price} in input
            group by name
            select max(2, 3, price);
    assertEquality([12, 11], res);
    res = from var {name, price} in input
            group by name
            select max(2, price);
    assertEquality([12, 11], res);
    res = from var {name, price} in input
            group by name
            select max(20, 30, price);
    assertEquality([30, 30], res);
    res = from var {name, price} in input
            group by name
            select int:max(20, 30, price);
    assertEquality([30, 30], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys4() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    int[] res = from var {name, price1, price2} in input
                    group by name
                    select [price1].length();
    assertEquality([2, 3, 1], res);
    res = from var {name, price1, price2} in input
                    group by name
                    select [price1].length() + [price2].length();
    assertEquality([4, 6, 2], res);
    res = from var {name, price1, price2} in input
                    group by name
                    select [price1].length() + [price1].length();
    assertEquality([4, 6, 2], res);
    res = from var {name, price1, price2} in input
                    group by name
                    let var x = [price1].length() + [price1].length()
                    select x;
    assertEquality([4, 6, 2], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys5() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    int[] res = from var {name, price1, price2} in input
                    group by name
                    select int:sum(int:max(2, price1));
    assertEquality([11, 10, 30], res);
    res = from var {name, price1, price2} in input
                    group by name
                    select int:sum(int:max(2, price2));
    assertEquality([12, 13, 13], res);
    res = from var {name, price1, price2} in input
                    group by name
                    select int:sum(int:max(201, price2));
    assertEquality([201, 201, 201], res);
    res = from var {name, price1, price2} in input
                    group by name
                    select int:sum(int:max(2, price2), int:max(2, price1));
    assertEquality([23, 23, 43], res);
    var xx = from var {name, price1, price2} in input
                    group by name
                    select int:sum(int:max(2, price2), int:max(2, price1));
    assertEquality([23, 23, 43], xx);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys6() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];

    var res = from var {name, price1, price2} in input
                group by var _ = true
                select sum(price1);
    assertEquality([82], res);

    res = from var {name, price1, price2} in input
            group by var _ = true
            select sum(price1) + sum(price2);
    assertEquality([152], res);

    res = from var {name, price1, price2} in input
                group by var _ = true
                select int:sum(...[price1]);
    assertEquality([82], res);

    res = from var {name, price1, price2} in input
                group by var _ = true
                select [price1].length();
    assertEquality([6], res);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9}];
    int[] prices = from var {name, price1, price2} in input
                        group by int n = price1 + price2
                        select sum(price1); // @output [[11, 10], [11, 10], [10]]
    assertEquality([21, 21, 10], prices);
    prices = from var {name, price1, price2} in input
                        group by int n1 = price1 + price2, int n2 = price1 - price2
                        select sum(price1); // @output [[11, 10], [11, 10], [10]]
    assertEquality([11, 11, 10, 10, 10], prices);
    prices = from var {name, price1, price2} in input
                        group by int n1 = price1 + price2, int n2 = price1 - price2
                        where n1 == 22
                        select sum(price1); // @output [[11, 10], [11, 10], [10]]
    assertEquality([11, 10], prices);    
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys2() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    var yy = from var item in input
                let var p1 = item.price1
                let var p2 = item.price2
                group by string name = item.name
                select sum(p1);
    assertEquality([22, 30, 10], yy);

    int[] arr = from var item in input
                    let var p1 = item.price1
                    let var p2 = item.price2
                    group by string name = item.name
                    select sum(p1) + sum(p1);
    assertEquality([44, 60, 20], arr);

    arr = from var item in input
                let var p1 = item.price1
                let var p2 = item.price2
                group by string name = item.name
                select sum(p1) + sum(p2);
    assertEquality([45, 64, 23], arr);

    var x = from var item in input
                let var p1 = item.price1
                group by string name = item.name, var p2 = item.price2
                select sum(p1);
    assertEquality([11, 11, 10, 10, 10, 10], x);
}

function testGroupByExpressionWithOrderBy() {
    var input = [{name: "Saman", price: 18}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 3}, {name: "Amal", price: 2}];
    int[] res = from var {name, price} in input
                    group by name
                    order by name
                    select int:sum(price);
    assertEquality([2, 14, 30], res);
    res = from var {name, price} in input
                    order by price
                    group by name
                    select int:sum(price);
    assertEquality([2, 14, 30], res);
    var xx = from var {name, price} in input
                    order by name
                    group by name
                    select [price];
    assertEquality([2, 14, 30], res);
}

function testGroupByExpressionWithStreamOutput() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    var res1 = stream from var {name, price1, price2} in input
                    group by name
                    select sum(price1);
    record {| int value; |}|error? v = res1.next();
    int[] output = [];
    while (v is record {| int value; |}) {
        output.push(v.value);
        v = res1.next();
    }
    assertEquality([22, 30, 30], output);     
    stream<int> res2 = stream from var {name, price1, price2} in input
                    group by name
                    select sum(price1);
    v = res2.next();
    output = [];
    while (v is record {| int value; |}) {
        output.push(v.value);
        v = res2.next();
    }
    assertEquality([22, 30, 30], output);  
}

function testGroupByExpressionWithTableOutput() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    var res1 = table key(name) from var {name, price1, price2} in input
                                    group by name
                                    select {name: name, prices: sum(price1)};
    assertEquality(res1, table key(name) [
        {name: "Saman", prices: 26},
        {name: "Kamal", prices: 32},
        {name: "Amal", prices: 30}
    ]);

    table<record {|readonly string name; int prices;|}> key(name) res2 = table key(name) from var {name, price1, price2} in input
                                                                                        group by name
                                                                                        select {name: name, prices: sum(price1)};
    assertEquality(res2, table key(name) [
        {name: "Saman", prices: 26},
        {name: "Kamal", prices: 32},
        {name: "Amal", prices: 30}
    ]);
}

function testGroupByExpressionWithMapOutput() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    var res1 = map from var {name, price1, price2} in input
                    group by name
                    select [name, sum(price1)];
    assertEquality(res1, {"Saman": 26, "Kamal": 32, "Amal": 30});

    map<int> res2 = map from var {name, price1, price2} in input
                            group by name
                            select [name, sum(price1)];
    assertEquality(res2, {"Saman": 26, "Kamal": 32, "Amal": 30});
}

function testMultipleGroupByInSameQuery() {
    var input1 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 11, price2: 13},
                    {name: "Amal", price1: 11, price2: 15}];
    
    var x1 = from var {name, price1, price2} in input1
                group by name
                group by var p1 = [price1]
                select string:'join("_", name);
    assertEquality(["Saman_Amal", "Kamal"], x1);

    var x2 = from var {name, price1, price2} in input1
                group by price1
                group by price1
                select int:sum(price1);
    assertEquality([11, 10], x2);

    var x3 = from var {name, price1, price2} in input1
                group by price1
                let var n = string:'join(",", name)
                group by price1
                select string:'join("_", n);
    assertEquality(["Saman,Saman,Amal,Amal", "Kamal,Kamal,Kamal"], x3);

    var input2 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 11},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];    

    var x4 = from var {name, price1, price2} in input2
                group by price1
                group by var p2 = [price2]
                select sum(price1);
    assertEquality([21, 12], x4);

    var x5 = from var {name, price1, price2} in input2
                group by price1
                let var n1 = string:'join("_", name)
                group by var p2 = sum(price2)
                let var n2 = string:'join(" ", n1)
                group by p2
                select string:'join("@", n2);
    assertEquality(["Saman_Saman Kamal_Kamal", "Amal_Amal"], x5);

    var x6 = from var {name, price1, price2} in input2
                group by price1
                let var n = [name]
                group by n
                select string:'join("@", ...n);
    assertEquality(["Saman@Saman", "Kamal@Kamal", "Amal@Amal"], x6);

    var input3 = [{name: "Saman", price1: 11, price2: 12},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];   

    var x7 = from var {name, price1, price2} in input3
                group by price1, price2
                let var n = string:'join("@", name)
                group by var _ = price1 + price2
                select string:'join("_", n);
    assertEquality(["Saman@Saman@Kamal", "Kamal", "Amal", "Amal"], x7);    

    var x8 = from var {name, price1, price2} in input3
                group by price1, price2
                let var s = string:'join("@", name)
                group by var _ = from var {name: n, price1: p1, price2: p2} in input3 group by n select int:sum(p1)
                select string:'join("_", s);
    assertEquality(["Saman@Saman@Kamal_Kamal_Amal_Amal"], x8); 

    string x9 = from var {name, price1, price2} in input3
                    group by var _ = true 
                    let var n = [name]
                    group by var _ = true 
                    select ",".'join(...([n][0]));
    assertEquality("Saman,Saman,Kamal,Kamal,Amal,Amal", x9);   

    string x10 = from var {name, price1, price2} in input3
                    where name == "Saman"
                    group by var _ = true 
                    let var n = [name]
                    group by var _ = true 
                    select ",".'join(...([n][0]));
    assertEquality("Saman,Saman", x10);  
}

type RecOpt record {|
    string name;
    int price1;
    int price2?;
|};

type INTTUPLE typedesc<[int...]>;

function testOptionalFieldInput() returns error? {
    RecOpt[] input1 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 14},
                    {name: "Amali", price1: 14}];

    var x1 = from var {name, price1, price2} in input1
                group by price2
                select string:'join(",", name);
    assertEquality(["Saman", "Saman,Kamal", "Kamal", "Kamal", "Amal,Amali"], x1);

    RecOpt[] input2 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13},
                    {name: "Amal", price1: 14},
                    {name: "Amali", price1: 14}];

    var x2 = from var {name, price1, price2} in input2
                        group by name
                        let var p2 = [price2]
                        where p2.length() > 0
                        let var p3 = check p2.cloneWithType(INTTUPLE)
                        select int:sum(...p3);
    assertEquality([23, 25], x2);

    var x4 = from var {name, price1, price2} in input2
                        group by name
                        let var p2 = [price2]
                        where p2.length() > 0
                        let var p3 = <[int...] & readonly> p2.cloneReadOnly()
                        select int:sum(...p3);
    assertEquality([23, 25], x4);

    var x5 = from var {name, price1, price2} in input2
                        group by name
                        let var p2 = [price2]
                        where p2.length() > 0
                        let [int...] p3 = check p2.cloneReadOnly().ensureType()
                        select int:sum(...p3);
    assertEquality([23, 25], x5);

    var x6 = from var {name, price1, price2} in input2
                        group by name
                        let var p2 = [price2]
                        where p2.length() > 0
                        let var p3 = check p2.cloneReadOnly().ensureType(INTTUPLE)
                        select int:sum(...p3);
    assertEquality([23, 25], x6);

    var x3 = from var {name, price1, price2} in input1
                group by price2
                select sum(price1);    
    assertEquality([11, 24, 10, 13, 28], x3); 
}

function testGroupByExpressionAndSelectWithNonGroupingKeys8() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    decimal[] sum = from var {name, price} in input
                        group by name
                        select avg(price);
    assertEquality([11.66666666666666666666666666666667d, 11d], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:avg(price);
    assertEquality([11.66666666666666666666666666666667d, 11d], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:avg(2, ...[price]);
    assertEquality([9.25d, 6.5d], sum);
    var input1 = [{name: "Saman", price: 11.0}, {name: "Saman", price: 12.0}, {name: "Kamal", price: 11.0}, {name: "Saman", price: 12.0}];
    float[] x = from var {name, price} in input1
                        group by name
                        select 2.0.avg(2, ...[price]);
    assertEquality([7.8, 5.0], x);

    var input2 = [{name: "Saman", err: error("msg1")}, 
                    {name: "Kamal", err: error("msg1")}]; 

    var j = from var {name, err} in input2
                group by var str = err.message()
                select 'join(",", name);
    assertEquality(["Saman,Kamal"], j);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys11() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    decimal[] sum = from var {name, price1, price2} in input
                    group by var _ = true
                    select 5.avg(23, ...[price2]);
    assertEquality([12.25d], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys10() {
    record {|string name; float price;|}[] input1 = [{name: "Saman", price: 1.0}, {name: "Kamal", price: 1.2}, {name: "Kamal", price: 0.9}];
    float[] sum = from var {name, price} in input1
                    group by name
                    select avg(price);
    assertEquality([1.0, 1.05], sum);

    var input2 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    decimal[] sum1 = from var {name, price1, price2} in input2
                    group by var _ = true
                    select 5.avg(23, ...[price2]);
    assertEquality([12.25d], sum1);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys12() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    int[] sum = from var {name, price} in input
                        group by name
                        select count(price);
    assertEquality([3, 1], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:count(price);
    assertEquality([3, 1], sum);
    sum = from var {name, price} in input
                        group by name
                        select value:count(...[price]);
    assertEquality([3, 1], sum);
    sum = from var {name, price} in input
                        group by name
                        select value:count(2, ...[price]);
    assertEquality([4, 2], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.0.count(2, ...[price]);
    assertEquality([5, 3], sum);
}

function testGroupByWithDoClause() {
    var input = [{name: "Saman", price1: 11, price2: 12}, 
                    {name: "Saman", price1: 13, price2: 14}, 
                    {name: "Kamal", price1: 15, price2: 16}, 
                    {name: "Kamal", price1: 17, price2: 18}, 
                    {name: "Saman", price1: 19, price2: 20}];
    int[] arr = []; 
    _ = from var {name, price1, price2} in input
            group by name
            do {
                arr.push(int:sum(price1) + int:sum(price2));
            };
    assertEquality([89, 66], arr);  

    arr = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                arr.push(int:sum(...[price1]) + int:sum(...[price2]));
            };
    assertEquality([89, 66], arr);  

    arr = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    int x = min(100, price1);
                    arr.push(x);
                };
    assertEquality([11, 15], arr);   

    arr = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    int x = sum(price1) + sum(price2);
                    arr.push(x);
                };
    assertEquality([89, 66], arr);     

    arr = [];
    int x = 0;
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    x = sum(price1) + sum(price2);
                    arr.push(x);
                };
    assertEquality([89, 66], arr); 

// Check after https://github.com/ballerina-platform/ballerina-lang/issues/40181
//     input = [{name: "Saman", price1: 11, price2: 12}, 
//                 {name: "Saman", price1: 13, price2: 14}, 
//                 {name: "Kamal", price1: 15, price2: 16}, 
//                 {name: "Kamal", price1: 17, price2: 18}, 
//                 {name: "Saman", price1: 19, price2: 20}];
//     int[] prices = [];
//     _ = from var {name, price1, price2} in input
//                 group by name // name : Saman, price1 : [11, 13, 19], price2 : [12, 14, 20]
//                 do {
//                     foreach int p in 0...sum(price1) {
//                         prices.push(p);
//                     }
//                 };
//     assertEquality([11, 13, 15, 17, 19], prices); 

    arr = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    arr.push(int:sum(sum(price1), sum(price2)));
                };
    assertEquality([89, 66], arr); 
    
    // Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/40200
    // _ = from var {name, price1, price2} in input
    //         group by name 
    //         do {
    //             match max(price1) {
    //                 var a => {
    //                     io:println("a");
    //                 }
    //             }
    //         };

    arr = [];
    _ = from var {name, price1, price2} in input
            group by name // name : Saman, price1 : [11, 13, 19], price2 : [12, 14, 20]
            do {
                record {| int prices; |} r = {prices: sum(price1)};
                arr.push(r.prices);
            };
    assertEquality([43, 32], arr);

    arr = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                int i = 0;
                i += sum(price1);
                arr.push(i);
            };
    assertEquality([43, 32], arr); 

    // Check after https://github.com/ballerina-platform/ballerina-lang/issues/40228
    // _ = from var {name, price1, price2} in input
            // group by name
    //         do {
    //             var obj = object {
    //                 int p1 = sum(price1);
    //             };
    //         }; 

    // Check after https://github.com/ballerina-platform/ballerina-lang/issues/40229
    // _ = from var {name, price1, price2} in input
    //         group by name
    //         do {
    //             _ = let var p1 = sum(price1) in p1;
    //         };      

    int[][] arr1 = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    int[] y = from var _ in input
                                select sum(price1);
                    arr1.push(y);
                };
    assertEquality([[43, 43, 43, 43, 43], [32, 32, 32, 32, 32]], arr1);
}

type INTARR int[];

function testGroupByExpressionAndSelectWithGroupingKeys1() {
    var input = [{id: 1, price: 11}, {id: 1, price: 12}, {id: 2, price: 11}, {id: 1, price: 12}];
    int[] sum = from var {id, price} in input
                    group by id
                    select int:sum(id);
    assertEquality([1, 2], sum);
    var xx = from var {id, price} in input
                group by id
                select int:sum(id);
    assertEquality([1, 2], xx);
    INTARR yy = from var {id, price} in input
                group by id
                select int:sum(id);
    assertEquality([1, 2], yy);
}

function testGroupByExpressionAndSelectWithGroupingKeys2() {
    var input = [{name: "Saman", price: [11, 12]}, 
                    {name: "Saman", price: [11, 12]}, 
                    {name: "Kamal", price: [15, 16]}, 
                    {name: "Kamal", price: [17, 18]}, 
                    {name: "Saman", price: [19, 20]}];
    var x = from var {name, price} in input
                group by name, price 
                select int:sum(...price);
    assertEquality([23, 31, 35, 39], x);
    x = from var {name, price} in input
            group by name, var p = price 
            select int:sum(...p);
    assertEquality([23, 31, 35, 39], x);
}

// Fix this after https://github.com/ballerina-platform/ballerina-lang/issues/40325
// function testLambdaFunction() {
//     var input = [{name: "Saman", price: 1}, {name: "Amal", price: 2}, {name: "Saman", price: 3}];

//     var x = from var {name, price} in input
//                 let var func = function (int x) returns int {return price + x;}
//                 group by var x = func(price)
//                 select [x, [price]];
// }

function testMultipleGroupBy() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    var res1 = from var {name, price1, price2} in input
                group by name
                let var minPrice2 = int:min(200, price2)
                select from var x in [price1]
                          let int y = x + minPrice2
                          group by var _ = true
                          select int:sum(y);
    assertEquality([[44], [57], [23]], res1);

    var res2 = from var {name, price1, price2} in input
                group by name
                let var minPrice2 = int:min(200, price2)
                select (from var x in [price1]
                          let int y = x + minPrice2
                          group by var _ = true
                          select sum(y))[0];
    assertEquality([44, 57, 23], res2);

    var res3 = from var {name, price1, price2} in input
                group by name
                let var minPrice2 = int:min(200, price2)
                let var zz = from var x in [price1] let int y = x + minPrice2 group by var _ = true select sum(y)
                select zz;
    assertEquality([[44], [57], [23]], res3);

    var res4 = from var {name, price1, price2} in input
                group by name
                let var minPrice2 = int:min(200, price2)
                let var zz = (from var x in [price1] let int y = x + minPrice2 group by var _ = true select sum(y))[0]
                select zz;
    assertEquality([44, 57, 23], res4);

    var res5 = from var {name, price1, price2} in input
                group by var xx = (from var x in input group by name select int:sum(price1))
                select sum(price1);
    assertEquality([22, 40], res5);

    var res6 = from var {name, price1, price2} in input
                group by var xx = (from var {name: n, price1: p1, price2: p2} in input group by n select sum(p1))
                select sum(price1);
    assertEquality([62], res6);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys7() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    var res1 = from var {name, price1, price2} in input
                group by var _ = true
                select avg(price1);
    assertEquality([10.33333333333333333333333333333333d], res1);
    var res2 = from var {name, price1, price2} in input
                group by var _ = true
                select avg(price1) + avg(price2);
    assertEquality([22.00000000000000000000000000000000d], res2);
    var res3 = from var {name, price1, price2} in input
                group by var _ = true
                select min(price1) + min(price2);
    assertEquality([19], res3);// TODO: from var {name: namex, price1, price2} group by namex

}

function testEmptyGroups() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    var res1 = from var {name, price1, price2} in input
                where name == "No name"
                group by var _ = true 
                select avg(price1);
    assertEquality([], res1);
    var res2 = from var {name, price1, price2} in input
                where name == "No name"
                group by var _ = true 
                select avg(price1) + avg(price2);   
    assertEquality([], res2);
    decimal[] res3 = from var {name, price1, price2} in input
                        where name == "No name"
                        group by var _ = true 
                        select avg(price1);   
    assertEquality([], res3);    
    string res4 = from var {name, price1, price2} in input
                    where name == "No name"
                    group by var _ = true 
                    select ",".'join(name);   
    assertEquality("", res4);    
    string res5 = from var {name, price1, price2} in input
                    where name == "No name"
                    group by var _ = true 
                    let var n = [name]
                    group by var _ = true 
                    select ",".'join(...([n][0]));
    assertEquality("", res5);   
}

enum Student {
    SAMAN,
    KAMAL,
    AMAL
};

function testEnumInInput() {
    var input = [{name: SAMAN, price1: 11, price2: 12},
                    {name: SAMAN, price1: 11, price2: 14},
                    {name: KAMAL, price1: 12, price2: 12},
                    {name: KAMAL, price1: 12, price2: 14},
                    {name: AMAL, price1: 19, price2: 20}];

    var x1 = from var {name, price1, price2} in input
                group by name
                select sum(price1);
    assertEquality([22, 24, 19], x1);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys9() {
    var input = [
        {name: "Saman", mathematics: 85, science: 70, quarter: "Q1"},
        {name: "Kamal", mathematics: 85, science: 70, quarter: "Q1"},
        {name: "Amal", mathematics: 85, science: 70, quarter: "Q1"},
        {name: "Saman", mathematics: 85, science: 70, quarter: "Q2"},
        {name: "Kamal", mathematics: 85, science: 70, quarter: "Q2"},
        {name: "Amal", mathematics: 85, science: 70, quarter: "Q2"},
        {name: "Saman", mathematics: 85, science: 70, quarter: "Q3"},
        {name: "Kamal", mathematics: 85, science: 70, quarter: "Q3"},
        {name: "Amal", mathematics: 85, science: 70, quarter: "Q3"},
        {name: "Saman", mathematics: 85, science: 70, quarter: "Q4"},
        {name: "Kamal", mathematics: 85, science: 70, quarter: "Q4"},
        {name: "Amal", mathematics: 85, science: 70, quarter: "Q4"}
    ];

    var yearlyAvg = from var {name, mathematics, science} in input
        group by name
        select {name: name, mathematicsAvg: int:avg(mathematics), scienceAvg: avg(science)};
    assertEquality([
        {"name": "Saman", "mathematicsAvg": 85d, "scienceAvg": 70d},
        {"name": "Kamal", "mathematicsAvg": 85d, "scienceAvg": 70d},
        {"name": "Amal", "mathematicsAvg": 85d, "scienceAvg": 70d}
    ], yearlyAvg);

    var quarterlyMin = from var {mathematics, science, quarter} in input
        group by quarter
        select {quarter: quarter, mathematicsMin: min(mathematics), minScience: min(science)};
    assertEquality([
        {"quarter": "Q1", "mathematicsMin": 85, "minScience": 70},
        {"quarter": "Q2", "mathematicsMin": 85, "minScience": 70},
        {"quarter": "Q3", "mathematicsMin": 85, "minScience": 70},
        {"quarter": "Q4", "mathematicsMin": 85, "minScience": 70}
    ], quarterlyMin);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
