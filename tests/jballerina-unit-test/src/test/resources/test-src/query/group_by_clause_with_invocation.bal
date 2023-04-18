// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
//     sum = from var {name, price} in input
//                         group by name
//                         select int:sum(price + 2);
//     assertEquality([35, 11], sum);
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
//     sum = from var {name, price} in input
//                         group by name
//                         select int:sum(price + 2);
//     assertEquality([35, 11], sum);
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

// function testGroupByExpressionAndSelectWithNonGroupingKeys2() {
//     var input = [{name: "Saman", price1: 11, price2: 11},
//                     {name: "Saman", price1: 11, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];

//     int[] sum = from var {name, price1, price2} in input
//                     group by name
//                     select sum(price1 + price2);
//     // int[] sum = from var {name, price1, price2} in input
//     //                 group by name
//     //                 select sum(...[price1] + ...[price2]); // negative test case
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select sum(price1 + price2);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select sum(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select int:sum(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select 5.sum(price1 + price2 + 3);
//     // sum = from var {_, price1, price2} in input
//     //         group by var _ = true
//     //         select int:sum(price1, price2); // error
//     assertEquality([35, 11], sum);
//     // sum = from var {_, price1, price2} in input
//     //         group by var _ = true
//     //         select 5.sum(...[price1], ...[price2]); // error
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select 5.sum(23, ...[price2]);
//     assertEquality([35, 11], sum);
// }

// function testGroupByExpressionAndSelectWithNonGroupingKeys3() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
//     int[] sum = from var {name, price} in input
//                         group by name
//                         select avg(price);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select int:avg(price);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select int:avg(price + 2);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select avg(...[price]);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select int:avg(2, ...[price]);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select 2.0.avg(2, ...[price]);
//     assertEquality([35, 11], sum);
// }

// function testGroupByExpressionAndSelectWithNonGroupingKeys4() {
//     var input = [{name: "Saman", price1: 11, price2: 11},
//                     {name: "Saman", price1: 11, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];

//     int[] sum = from var {name, price1, price2} in input
//                     group by name
//                     select avg(price1 + price2);
//     // int[] sum = from var {name, price1, price2} in input
//     //                 group by name
//     //                 select sum(...[price1] + ...[price2]); // negative test case
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select avg(price1 + price2);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select avg(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select int:avg(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select 5.avg(price1 + price2 + 3);
//     // sum = from var {_, price1, price2} in input
//     //         group by var _ = true
//     //         select int:sum(price1, price2); // error
//     assertEquality([35, 11], sum);
//     // sum = from var {_, price1, price2} in input
//     //         group by var _ = true
//     //         select 5.sum(...[price1], ...[price2]); // error
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select 5.avg(23, ...[price2]);
//     assertEquality([35, 11], sum);
// }

// function testGroupByExpressionAndSelectWithNonGroupingKeys5() {
//     record {|string name; float price;|}[] input1 = [{name: "Saman", price: 1.0}, {name: "Kamal", price: 1.2}, {name: "Kamal", price: 0.9}];
//     float[] sum = from var {name, price} in input1
//                     group by name
//                     select avg(price);
//     assertEquality([35, 11], sum);

//     record {|string name; float price?;|}[] input2 = [{name: "Saman"}, {name: "Kamal"}, {name: "Kamal"}];
//     sum = from var {name, price} in input2
//                 group by name
//                 select avg(price);
//     assertEquality([35, 11], sum);

//     record {|string name; float price?;|}[] input3 = [{name: "Saman"}, {name: "Kamal"}, {name: "Kamal", price: 0.9}];
//     sum = from var {name, price} in input3
//                 group by name
//                 select avg(price);
//     assertEquality([35, 11], sum);
// }

// function testGroupByExpressionAndSelectWithNonGroupingKeys6() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
//     int[] sum = from var {name, price} in input
//                         group by name
//                         select count(price);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select int:count(price);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select int:count(price + 2);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select count(...[price]);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select int:count(2, ...[price]);
//     assertEquality([35, 11], sum);
//     sum = from var {name, price} in input
//                         group by name
//                         select 2.0.count(2, ...[price]);
//     assertEquality([35, 11], sum);
// }

// function testGroupByExpressionAndSelectWithNonGroupingKeys7() {
//     var input = [{name: "Saman", price1: 11, price2: 11},
//                     {name: "Saman", price1: 11, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];

//     int[] sum = from var {name, price1, price2} in input
//                     group by name
//                     select first(price1 + price2);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select last(price1 + price2);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select first(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select int:first(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
//     sum = from var {_, price1, price2} in input
//             group by var _ = true
//             select 5.last(price1 + price2 + 3);
//     assertEquality([35, 11], sum);
// }

// TODO: Add tests with do clause
// TODO: group by var _ = true
// TODO: from va item ..., group by var x = item.price ...

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
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
