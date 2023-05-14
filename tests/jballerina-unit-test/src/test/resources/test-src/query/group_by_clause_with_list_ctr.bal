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

function testGroupByExpressionAndSelectWithGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by name
                        select name; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names);

    names = [];
    names = from var {name} in input
                group by name, var _ = 2
                select name; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names);

    var x = from var {name} in input
                group by var _ = true
                select [name]; // @output ["Saman", "Saman", "Kamal"]
    assertEquality([["Saman", "Saman", "Kamal"]], x);

    var y = from var {name} in input
                group by var _ = 2
                select [name]; // @output ["Saman", "Saman", "Kamal"]
    assertEquality([["Saman", "Saman", "Kamal"]], y);
}

function testGroupByExpressionAndSelectWithGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] prices = from var {price} in input
                        group by price
                        select price; // @output [11, 12]
    assertEquality([11, 12], prices);
}

function testGroupByExpressionAndSelectWithGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    string[] names = from var {name, price} in input
                        group by name, price
                        select name; // @output ["Saman", "Saman", "Kamal"]
    assertEquality(["Saman", "Saman", "Kamal"], names);
}

function testGroupByExpressionAndSelectWithGroupingKeys4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 12, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                    group by name, price2
                                                    select {name, price: price2};
                                                    // @output [{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Kamal", price: 21}]
    assertEquality([{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Kamal", price: 21}], res);

    var x = from var {name, price1, price2} in input
                group by var _ = true
                select {price: [price2]};
    assertEquality([{"price": [11, 10, 21, 11]}], x);
}

function testGroupByExpressionAndSelectWithGroupingKeys5() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[][] names = from var {name} in input
                        group by name
                        select [name]; // @output [["Saman"], ["Kamal"]]
    assertEquality([["Saman"], ["Kamal"]], names);
}

function testGroupByExpressionAndSelectWithGroupingKeys6() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 11}];
    [string, int][] res = from var {name, price} in input
                            group by name, price
                            select [name, price]; // @output [["Saman", 11], ["Saman", 12], ["Kamal", 11]]
    assertEquality([["Saman", 11], ["Saman", 12], ["Kamal", 11]], res);
}

function testGroupByExpressionAndSelectWithGroupingKeys7() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input = [
                                {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    int[] count = from var {town, hotels} in input
                    group by hotels
                    select hotels.length(); // @output [2]
    assertEquality([2], count);
}

function testGroupByExpressionAndSelectWithGroupingKeys8() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input = [
                                {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                                {town: "Colombo4", hotels: [{name: "HotelA", distance: 2}]},
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    int[] count = from var {town, hotels} in input
                    group by hotels
                    select hotels.length(); // @output [2, 1]
    assertEquality([2, 1], count);

    count = from var {town, hotels} in input
                    group by var _ = true
                    select [hotels].length(); // @output [2, 1]
    assertEquality([3], count);
}

function testGroupByExpressionAndSelectWithGroupingKeys9() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input = [
                            {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                            {town: "Colombo4", hotels: [{name: "HotelB", distance: 2}]},
                            {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    string[] res = from var {town, hotels} in input
                    group by hotels
                    select hotels[0].name; // @output ["HotelA", "HotelB"]
    assertEquality(["HotelA", "HotelB"], res);
}

function testGroupByExpressionAndSelectWithGroupingKeys10() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input = [
                            {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                            {town: "Colombo4", hotels: [{name: "HotelB", distance: 2}]},
                            {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    var res = from var {town, hotels} in input
                group by hotels
                select [town]; // @output [["Colombo2", "Colombo3"], ["Colombo4"]]
    assertEquality([["Colombo2", "Colombo3"], ["Colombo4"]], res);
}

function testGroupByExpressionAndSelectWithGroupingKeys11() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                            group by name
                            select name; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names);
}

type STR string;
type STRINT string|int;

function testGroupByExpressionAndSelectWithGroupingKeys12() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    STR[] names1 = from var {name} in input
                            group by name
                            select name; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names1);

    STRINT[] names2 = from var {name} in input
                            group by name
                            select name; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names2);
}

function testGroupByExpressionAndSelectWithGroupingKeys13() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input = [
                            {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                            {town: "Colombo4", hotels: [{name: "HotelB", distance: 2}]},
                            {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    [string...][] res = from var {town, hotels} in input
                            group by hotels
                            select [town]; // @output [["Colombo2", "Colombo3"], ["Colombo4"]]
    assertEquality([["Colombo2", "Colombo3"], ["Colombo4"]], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by name
                        where name == "Kamal"
                        select name; // @output ["Kamal"]
    assertEquality(["Kamal"], names);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause2() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 12, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by name, price2
                                                where price2 > 20
                                                select {name, price: price2}; // @output [{"name":"Kamal", "price":21}]
    assertEquality([{name: "Kamal", price: 21}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause3() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 12, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by name, price2
                                                where price2 < 20
                                                select {name, price: price2}; // @output [{"name":"Kamal", "price":21}]
    assertEquality([{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Amal", price: 10}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                where price1 + price2 < 30
                                                group by name, price2
                                                select {name, price: price2};
                                                // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}]
    assertEquality([{name: "Saman", price: 11}, {name: "Amal", price: 10}], res);
}

function getTotal(int... ns) returns int {
    return int:sum(...ns);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause5() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 13},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                where getTotal(price1, price2) < 30
                                                group by name, price2
                                                select {name, price: price2};
                                                // @output [{name: "Saman", price: 11}, {name: "Kamal",price: 13}, {name: "Amal", price: 10}]
    assertEquality([{name: "Saman", price: 11}, {name: "Kamal",price: 13}, {name: "Amal", price: 10}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause6() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by name, price1, price2
                                                where getTotal(price1, price2) < 30
                                                select {name, price: price2};
                                                // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause7() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by name, price1, price2
                                                let var total = getTotal(price1, price2)
                                                where total < 30
                                                select {name, price: price2};
                                                // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause8() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by name, price1, price2
                                                where price1 + price2 < 30
                                                select {name, price: price2};
                                                // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}, {name:"Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name: "Amal", price: 10}, {name:"Saman", price: 11}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause9() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string|int name; int|string price;|}[] res1 = from var {name, price1, price2} in input
                                                            where price1 + price2 < 30
                                                            group by name, price2
                                                            select {name, price: price2};
                                                            // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}]
    assertEquality([{name: "Saman", price: 11}, {name: "Amal", price: 10}], res1);
    record {|STRINT name; STRINT price;|}[] res2 = from var {name, price1, price2} in input
                                                            where price1 + price2 < 30
                                                            group by name, price2
                                                            select {name, price: price2};
                                                            // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}]
    assertEquality([{name: "Saman", price: 11}, {name: "Amal", price: 10}], res2);
}

function append(string name) returns string {
    return name + " Kumara";
}

function testGroupByExpressionAndSelectWithGroupingKeysFromClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    int[][] prices = from var {name} in input
                        group by name
                        select from var {price} in input
                                select price; // @output [[11, 12, 11, 12], [11, 12, 11, 12], [11, 12, 11, 12]]
    assertEquality([[11, 12, 11, 12], [11, 12, 11, 12], [11, 12, 11, 12]], prices);
}

function testGroupByExpressionAndSelectWithGroupingKeysFromClause2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    record {|string name; int price;|}[][] res = from var {name} in input
                                                    group by name
                                                    select from var {price} in input
                                                            select {name, price};
                                                    // @output [[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}],
                                                    //          [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}],
                                                    //          [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]]
    assertEquality([[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}],
                    [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}],
                    [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]], res);
}

// // This will not working due to an existing bug
// function testGroupbyExpressionAndSelectWithGroupingKeysFromClause3() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by hotels
//                         select from var {name} in hotels
//                                 group by name
//                                 select name; // @output [[HotelA], [HotelB]]
// }

// // This will not waoking due to an existing bug
// function testGroupbyExpressionAndSelectWithGroupingKeysFromClause4() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by hotels
//                         select from var {name} in hotels
//                                 where name == "HotelA"
//                                 group by name
//                                 select name; // @output [[HotelA]]
// }

// // This will not waoking due to an existing bug
// function testGroupbyExpressionAndSelectWithGroupingKeysFromClause5() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by hotels
//                         select from var {name} in hotels
//                                 group by name
//                                 where name == "HotelA"
//                                 select name; // @output [[HotelA]]
// }

// // This will not waoking due to an existing bug
// function testGroupbyExpressionAndSelectWithGroupingKeysFromClause6() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by hotels
//                         select from var {name} in hotels
//                                 group by name
//                                 let boolean isCorretName = name == "HotelA"
//                                 where isCorretName
//                                 select name; // @output [[HotelA]]
// }

function testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] deptPersonList = from var person in personList
                                join var {id, name: deptName} in deptList
                                on person.id equals id
                                group by deptName
                                select deptName; // @output ["HR", "Operations"]
    assertEquality(["HR", "Operations"], deptPersonList);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    record {|int id; string deptName;|}[] res = from var person in personList
                                                            join var {id, name: deptName} in deptList
                                                            on person.id equals id
                                                            group by id, deptName
                                                            select {id, deptName}; // @output [{id: 1, deptName: "HR"}, {id: 2, deptName: "Operations"}, {id: 3, deptName: "HR"}]
    assertEquality([{id: 1, deptName: "HR"}, {id: 2, deptName: "Operations"}, {id: 3, deptName: "HR"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] persons = from var {id, fname, lname} in personList
                        join var {id: deptId, name: deptName} in deptList
                        on id equals deptId
                        group by lname
                        select lname; // @output ["George", "Fonseka"]
    assertEquality(["George", "Fonseka"], persons);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause4() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int id; string lname; string deptName;|}[] res = from var {id, fname, lname} in personList
                                                                    group by id, lname
                                                                    join var {id: deptId, name: deptName} in deptList
                                                                    on id equals deptId
                                                                    select {id, lname, deptName};
                                                                    // @output [{id: 1, lname: "George", deptName: "HR"}, {id: 2, lname: "Fonseka", deptName: "Operations"}]
    assertEquality([{id: 1, lname: "George", deptName: "HR"}, {id: 2, lname: "Fonseka", deptName: "Operations"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause5() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int id; string lname; string deptName;|}[] res = from var {id, fname, lname} in personList
                                                                group by id, lname
                                                                join var {id: deptId, name: deptName} in deptList
                                                                on id equals deptId
                                                                select {id, lname, deptName};
                                                                // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
    assertEquality([{id: 1, lname: "George", deptName: "HR"}, {id: 2, lname: "Fonseka", deptName: "Operations"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                group by id, lname
                                                order by id
                                                select {id, lname};
                                                // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
    assertEquality([{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                order by id
                                                group by id, lname
                                                select {id, lname};
                                                // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
    assertEquality([{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                order by id, lname
                                                group by id, lname
                                                select {id, lname};
                                                // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
    assertEquality([{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithLimitClause() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                group by id, lname
                                                limit 2
                                                select {id, lname};
                                                // @output [{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}]
    assertEquality([{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}], res);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithTableResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    table<record {|readonly int id; string lname;|}> key(id) t = table key(id) from var {id, fname, lname} in personList
                                                                                group by id, lname
                                                                                select {id, lname};
                                                                                // @output [{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}]
    assertEquality(table key(id) [{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}], t);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithMapResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    map<string> m = map from var {id, fname, lname} in personList
                        group by id, lname
                        order by id
                        select [id.toString(), lname]; // @output [{"1":"George"}, {"2":"Silva"}, {"3":"Fonseka"}]
    assertEquality({"1":"George", "2":"Silva", "3":"Fonseka"}, m);
}

function testGroupByExpressionAndSelectWithGroupingKeysWithFromClause() {
    var personList1 = [{id: 1, fname: "Alex"}, {id: 2, fname: "Ranjan"}, {id: 3, fname: "Ranjan"}];
    var personList2 = [{index: 1, lname: "George"}, {index: 2, lname: "Fonseka"}, {index: 3, lname: "Silva"}];
    string[] res = from var {id, fname} in personList1
                    from var {index, lname} in personList2
                    where id == index
                    group by fname
                    select fname; // @output ["Alex", "Ranjan"]
    assertEquality(["Alex", "Ranjan"], res);
}

// group by <var def>, select <expr with grouping keys>, lhs has the type
function testGroupByVarDefsAndSelectWithGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by var n = name
                        select n; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] prices = from var {price} in input
                    group by int p = price
                    select p; // @output [11, 12]
    assertEquality([11, 12], prices);
}

function testGroupByVarDefsAndSelectWithGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    string[] names = from var {name, price} in input
                        group by string n = name, int p = price
                        select n; // @output ["Saman", "Saman", "Kamal"]
    assertEquality(["Saman", "Saman", "Kamal"], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeys4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 12, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                    group by name, int p = price2
                                                    select {name, price: p};
                                                    // @output [{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Kamal", price: 21}]
    assertEquality([{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Kamal", price: 21}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeys5() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[][] names = from var {name} in input
                        group by var n = name
                        select [n]; // @output [["Saman"], ["Kamal"]]
    assertEquality([["Saman"], ["Kamal"]], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeys6() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 11}];
    [string, int][] res = from var {name, price} in input
                        group by string n = name, int p = price
                        select [n, p]; // @output [["Saman", 11], ["Saman", 12], ["Kamal", 11]]
    assertEquality([["Saman", 11], ["Saman", 12], ["Kamal", 11]], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeys7() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input =  [
                            {town: "Colombo4", hotels: [{name: "HotelA", distance: 2}]},
                            {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                            {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    string[] names = from var {town, hotels} in input
                        group by record {|string name; float distance;|} hotel = hotels[0]
                        select hotel.name; // @output ["HotelA"]
    assertEquality(["HotelA"], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeys8() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input =  [
                            {town: "Colombo4", hotels: [{name: "HotelA", distance: 2}]},
                            {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                            {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    string[] names = from var {town, hotels} in input
                        group by var hotel = hotels[0]
                        select hotel.name; // @output ["HotelA"]}
    assertEquality(["HotelA"], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeys9() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    var res = from var {name, price1, price2} in input
                group by int n1 = price1 + price2, int n2 = price1 - price2
                select [[n1], [n2]]; // @output [[[22], [0]], [[23], [-1]], [[23], [-3]], [[22], [-2]], [[19], [1]]]
    assertEquality([[[22], [0]], [[23], [-1]], [[23], [-3]], [[22], [-2]], [[19], [1]]], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeys10() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    [int...][][] res = from var {name, price1, price2} in input
                        group by int n1 = price1 + price2, int n2 = price1 - price2
                        select [[n1], [n2]]; // @output [[[22], [0]], [[23], [-1]], [[23], [-3]], [[22], [-2]], [[19], [1]]]
    assertEquality([[[22], [0]], [[23], [-1]], [[23], [-3]], [[22], [-2]], [[19], [1]]], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeys11() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 11}, {name: "Kamal", price: 13},
                    {name: "Kamal", price: 14}, {name: "Kamal", price: 15}, {name: "Amal", price: 16}];
    string[] names1 = from var item in input
                        group by string name = item.name
                        select name;
    assertEquality(["Saman", "Kamal", "Amal"], names1);
    var names2 = from var item in input
                    group by var name = item.name
                    select name;
    assertEquality(["Saman", "Kamal", "Amal"], names2);
    var prices = from var item in input
                    group by var name = item.name, int price = item.price
                    select price;
    assertEquality([11, 13, 14, 15, 16], prices);
    int[] arr = from var item in [10, 10, 12]
                    group by item
                    select item;
    assertEquality([10, 12], arr);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause1() {
    record {|string name; int price;|}[] input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names1 = from var {name} in input
                        group by var n = name
                        where n == "Kamal"
                        select n; // @output ["Kamal"]
    assertEquality(["Kamal"], names1);

    string[] names2 = from var {name} in input
                        group by string n = name
                        where n == "Kamal"
                        select n; // @output ["Kamal"]
    assertEquality(["Kamal"], names2);

    string[] names3 = from var {name} in input
                        group by STR n = name
                        where n == "Kamal"
                        select n; // @output ["Kamal"]
    assertEquality(["Kamal"], names3);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause2() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 12, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res1 = from var {name, price1, price2} in input
                                                group by var n = name, var p = price2
                                                where p > 20
                                                select {name: n, price: p};
                                                // @output [{name: "Kamal", price: 21}]
    assertEquality([{name: "Kamal", price: 21}], res1);
    record {|string name; int price;|}[] res2 = from var {name, price1, price2} in input
                                                group by var n = name, int p = price2
                                                where p > 20
                                                select {name: n, price: p};
                                                // @output [{name: "Kamal", price: 21}]
    assertEquality([{name: "Kamal", price: 21}], res2);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause3() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                where price1 + price2 < 30
                                                group by string n = name, price2
                                                select {name: n, price: price2};
                                                // @output [{name: "Saman", price: 11}, {name: "Kamal", price: 21}, {name:"Amal", price: 10}]
    assertEquality([{name: "Saman", price :11}, {name: "Amal", price:10}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 13},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                where getTotal(price1, price2) < 30
                                                group by name, var p2 = price2
                                                select {name, price: p2};
                                                // @output [{name: "Saman", price: 11},{name: "Kamal", price: 13}, {name: "Amal", price: 10}]
    assertEquality([{name: "Saman", price: 11}, {name: "Kamal", price: 13}, {name: "Amal", price: 10}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause5() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by var n = name, var p1 = price1, int p2 = price2
                                                where getTotal(p1, p2) < 30
                                                select {name: n, price: p2};
                                                // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause6() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res1 = from var {name, price1, price2} in input
                                                group by string n = name, price1, var p2 = price2
                                                let var total = getTotal(price1, p2)
                                                where total < 30
                                                select {name: n, price: p2};
                                                // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}], res1);
    record {|string name; int price;|}[] res2 = from var {name, price1, price2} in input
                                                group by string n = name, price1, var p2 = price2
                                                let int total = getTotal(price1, p2)
                                                where total < 30
                                                select {name: n, price: p2};
                                                // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}], res2);
    record {|string name; int price;|}[] res3 = from var {name, price1, price2} in input
                                                group by string n = name, price1, var p2 = price2
                                                let int total = getTotal(price1, p2)
                                                where total < -3
                                                select {name: n, price: p2};
                                                // @output []
    assertEquality([], res3);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause7() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by string n = name, var p1 = price1, int p2 = price2
                                                where p1 + p2 < 30
                                                select {name: n, price: p2};
                                                // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}, {name:"Saman", price: 11}]
    assertEquality([{name: "Saman", price: 11}, {name: "Amal", price: 10}, {name:"Saman", price: 11}], res);
}

function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    record {|string n; int price;|}[][] names = from var {name} in input
                                                        group by var n = name
                                                        select from var {price} in input
                                                                select {n, price};
    assertEquality([[{"n":"Saman", "price":11}, {"n":"Saman", "price":12}, {"n":"Saman", "price":11}, {"n":"Saman", "price":12}],
                    [{"n":"Kamal", "price":11}, {"n":"Kamal", "price":12}, {"n":"Kamal", "price":11}, {"n":"Kamal", "price":12}],
                    [{"n":"Amal", "price":11}, {"n":"Amal", "price":12}, {"n":"Amal", "price":11}, {"n":"Amal", "price":12}]], names);
                                                    // @output [[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}],
                                                    //          [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}],
                                                    //          [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]]
}

// // This will not waoking due to an existing bug
// function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause3() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by record {|string name; decimal distance;|} groupedHotels = hotels
//                         select from var {name} in hotels
//                                 group by name
//                                 select name; // @output [[HotelA], [HotelB]]
// }

// // This will not waoking due to an existing bug
// function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause4() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by var groupedHotels = hotels
//                         select from var {name} in hotels
//                                 where name == "HotelA"
//                                 group by name
//                                 select name; // @output [[HotelA]]
// }

// // This will not waoking due to an existing bug
// function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause5() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by record {|string name; decimal distance;|} groupedHotels = hotels
//                         select from var {name} in groupedHotels
//                                 group by name
//                                 where name == "HotelA"
//                                 select name; // @output [[HotelA]]
// }

// // This will not waoking due to an existing bug
// function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause6() {
//     string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//                         group by var groupedHotels = hotels
//                         select from var {name} in groupedHotels
//                                 group by name
//                                 let boolean isCorretName = name == "HotelA"
//                                 where isCorretName
//                                 select name; // @output [[HotelA]]
// }

function testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] res1 = from var person in personList
                    join var {id, name: deptName} in deptList
                    on person.id equals id
                    group by string n = deptName
                    select n; // @output ["HR", "Operations"]
    assertEquality(["HR", "Operations"], res1);
    (string|int)[] res2 = from var person in personList
                    join var {id, name: deptName} in deptList
                    on person.id equals id
                    group by string|int n = deptName
                    select n; // @output ["HR", "Operations"]
    assertEquality(["HR", "Operations"], res2);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    record {|int id; string deptName;|}[] res = from var person in personList
                                                    join var {id, name: deptName} in deptList
                                                    on person.id equals id
                                                    group by var i = id, var n = deptName
                                                    select {id: i, deptName: n}; // @output [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}]
    assertEquality([{id: 1, deptName: "HR"}, {id: 2, deptName: "Operations"}, {id: 3, deptName: "HR"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] names = from var {id, fname, lname} in personList
                        join var {id: deptId, name: deptName} in deptList
                        on id equals deptId
                        group by var ln = lname
                        select ln; // @output ["George", "Fonseka"]
    assertEquality(["George", "Fonseka"], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause4() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int i; string ln; string deptName;|}[] res = from var {id, fname, lname} in personList
                                                            group by int i = id, string ln = lname
                                                            join var {id: deptId, name: deptName} in deptList
                                                            on i equals deptId
                                                            select {i, ln, deptName};
                                                            // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
    assertEquality([{i: 1, ln: "George", deptName: "HR"}, {i: 2, ln: "Fonseka", deptName: "Operations"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause5() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int id; string lname; string deptName;|}[] res = from var {id, fname, lname} in personList
                                                                group by var i = id, var ln = lname
                                                                join var {id: deptId, name: deptName} in deptList
                                                                on i equals deptId
                                                                select {id: i, lname: ln, deptName};
                                                                // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
    assertEquality([{id: 1, lname: "George", deptName: "HR"}, {id: 2, lname: "Fonseka", deptName: "Operations"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithOrderbyClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                group by id, var ln = lname
                                                order by id descending
                                                select {id, lname: ln};
                                                // @output [{id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}, {id: 1, lname: "George"}]
    assertEquality([{id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}, {id: 1, lname: "George"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithOrderbyClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                order by id
                                                group by var i = id, lname
                                                select {id: i, lname};
                                                // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
    assertEquality([{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithOrderbyClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] res = from var {id, fname, lname} in personList
                                                order by id, lname
                                                group by int i = id, string ln = lname
                                                select {id: i, lname: ln};
                                                // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
    assertEquality([{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithLimitClause() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int i; string ln;|}[] res = from var {id, fname, lname} in personList
                                            group by int i = id, string ln = lname
                                            limit 2
                                            select {i, ln};
                                            // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}]
    assertEquality([{i: 1, ln: "George"}, {i: 3, ln: "Fonseka"}], res);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithTableResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    table<record {|readonly int id; string ln;|}> key(id) t = table key(id) from var {id, fname, lname} in personList
                                                                                        group by var i = id, var ln = lname
                                                                                        select {id: i, ln};
                                                                                        // @output [{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}]
    assertEquality(table key(id) [{id: 1, ln: "George"}, {id: 3, ln: "Fonseka"}, {id: 2, ln: "Silva"}], t);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithMapResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"},
                        {id: 3, fname: "Ranjan", lname: "Fonseka"},
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    map<string> m = map from var {id, fname, lname} in personList
                        group by int i = id, string ln = lname
                        order by i
                        select [i.toString(), ln]; // @output {"1":"George", "2":"Silva", "3":"Fonseka"}
    assertEquality({"1":"George", "2":"Silva", "3":"Fonseka"}, m);
}

function testGroupByVarDefsAndSelectWithGroupingKeysWithFromClause() {
    var personList1 = [{id: 1, fname: "Alex"}, {id: 2, fname: "Ranjan"}, {id: 3, fname: "Ranjan"}];
    var personList2 = [{index: 1, lname: "George"}, {index: 2, lname: "Fonseka"}, {index: 3, lname: "Silva"}];
    string[] res = from var {id, fname} in personList1
                    from var {index, lname} in personList2
                    where id == index
                    group by var fn = fname
                    select fn; // @output ["Alex", "Ranjan"]
    assertEquality(["Alex", "Ranjan"], res);
}

// group by <expression>, select <expr with non grouping keys>, lhs has the type
function testGroupByExpressionAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] prices = from var {name, price} in input
                        group by name
                        let var p = [price]
                        select p; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var prices1 = from var {name, price} in input
                        group by name
                        let var p = [price]
                        select p; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices1);

    var prices2 = from var {name, price} in input
                        group by var _ = true
                        let var p = [price]
                        select p;
    assertEquality([[11, 12, 11]], prices2);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][][] prices1 = from var {name, price} in input
                            group by name
                            let var p = [[price]]
                            select p; // @output [[[11, 12]], [[11]]]
    assertEquality([[[11, 12]], [[11]]], prices1);

    [[int...]][] prices2 = from var {name, price} in input
                            group by name
                            let var p = [[price]]
                            select p; // @output [[[11, 12]], [[11]]]
    assertEquality([[[11, 12]], [[11]]], prices2);

    var prices3 = from var {name, price} in input
                    group by name
                    select [[price]]; // @output [[[11, 12]], [[11]]]
    assertEquality([[[11, 12]], [[11]]], prices3);

    var prices4 = from var {name, price} in input
                    group by name
                    select [([price])]; // @output [[[11, 12]], [[11]]]
    assertEquality([[[11, 12]], [[11]]], prices4);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys4() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var prices = from var {name, price} in input
                    group by name
                    select {price: [price]}; // @output [{price: [11, 12]}, {price: [11]}]
    assertEquality([{price: [11, 12]}, {price: [11]}], prices);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys5() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    var res = from var {name, price1, price2} in input
                        group by name
                        select [[price1], [price2]]; // @output [[11, 12, 23, 24], [13, 25], [14, 26]]
    assertEquality([[[11, 12], [23, 24]], [[13], [25]], [[14], [26]]], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys6() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    var res = from var {name, price1, price2} in input
                group by name
                select [[price1], 34]; // @output [[11, 12, 34], [13, 34], [14, 34]]
    assertEquality([[[11, 12], 34], [[13], 34], [[14], 34]], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys7() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    var res = from var {name, price1, price2} in input
                group by var _ = true
                select [[name], [price1], [price2]];
                // @output [["Saman", "Saman", "Kamal", "Amal", 11, 12, 13, 14, 23, 24, 25, 26]]
    assertEquality([[["Saman", "Saman", "Kamal", "Amal"], [11, 12, 13, 14], [23, 24, 25, 26]]], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys8() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] prices = from var {name, price} in input
                        group by name
                        select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys9() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    [int...][] prices1 = from var {name, price} in input
                            group by name
                            select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices1);
    [(int|string)...][] prices2 = from var {name, price} in input
                                    group by name
                                    select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices2);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys10() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][][] res1 = from var {name, price} in input
                        group by name
                        select [[price]]; // @output [[[11, 11]], [[12]]]
    assertEquality([[[11, 12]], [[11]]], res1);
    [int...][][] res2 = from var {name, price} in input
                        group by name
                        select [[price]]; // @output [[[11, 11]], [[12]]]
    assertEquality([[[11, 12]], [[11]]], res2);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys11() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    record {int[] price;}[] res1 = from var {name, price} in input
                                    group by name
                                    select {price: [price]}; // @output [{price: [11, 12]}, {price: [11]}]
    assertEquality([{price: [11, 12]}, {price: [11]}], res1);
    record {[int...] price;}[] res2 = from var {name, price} in input
                                        group by name
                                        select {price: [price]}; // @output [{price: [11, 12]}, {price: [11]}]
    assertEquality([{price: [11, 12]}, {price: [11]}], res2);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys12() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int...][][] prices = from var {name, price1, price2} in input
                            group by name
                            select [[price1], [price2]]; // @output [[[11, 12], [23, 24]], [[13], [25]], [[14], [26]]]
    assertEquality([[[11, 12], [23, 24]], [[13], [25]], [[14], [26]]], prices);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys13() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int[], int][] res = from var {name, price1, price2} in input
                            group by name
                            select [[price1], 34]; // @output [[[11, 12], 34], [[13], 34], [[14], 34]]
    assertEquality([[[11, 12], 34], [[13], 34], [[14], 34]], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys14() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int, [int...]][] res = from var {name, price1, price2} in input
                        group by name
                        select [21, [price1]]; // @output [[21, [11, 12]], [21, [13]], [21, [14]]]
    assertEquality([[21, [11, 12]], [21, [13]], [21, [14]]], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys15() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    (int|string)[][][] res = from var {name, price1, price2} in input
                                group by var _ = true
                                select [[name], [price1], [price2]];
                                // @output [[["Saman", "Saman", "Kamal", "Amal"], [11, 12, 13, 14[], [23, 24, 25, 26]]]
    assertEquality([[["Saman", "Saman", "Kamal", "Amal"], [11, 12, 13, 14], [23, 24, 25, 26]]], res);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys16() {
    var input = [{name: "Saman", price1: 11, price2: 23},
                    {name: "Saman", price1: 12, price2: 24},
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    record {string[] name; int[] price;}[] res = from var {name, price1, price2} in input
                                                    group by var _ = true
                                                    select {name: [name], price: [price2]};
                                                    // @output [{name: ["Saman", "Saman", "Kamal", "Amal"], price: [23, 24, 25, 26]}]
    assertEquality([{name: ["Saman", "Saman", "Kamal", "Amal"], price: [23, 24, 25, 26]}], res);
}

// There is a existing bug #39519
// function testGroupByExpressionAndSelectWithNonGroupingKeys17() {
//     var input = [{name: "Saman", price1: 11, price2: 23},
//                     {name: "Saman", price1: 12, price2: 24},
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int[]|string[]|int[][]][] res = from var {name, price1, price2} in input
//                                 group by var _ = true
//                                 select [[name], [[price1]], [price2]];
//                                 // @output [[["Saman", "Saman", "Kamal", "Amal"], [[11, 12, 13, 14]], [23, 24, 25, 26]]]
//     assertEquality([[["Saman", "Saman", "Kamal", "Amal"], [[11, 12, 13, 14]], [23, 24, 25, 26]]], res);
// }

// There is a existing bug #39519
// function testGroupbyExpressionAndSelectWithNonGroupingKeys14() {
//     var input = [{name: "Saman", price1: 11, price2: 23},
//                     {name: "Saman", price1: 12, price2: 24},
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int|string|int[]][] names = from var {name, price1, price2} in input
//                                 group by var _ = true
//                                 select [([name]), [[price1]], [price2]];
//                                 // @output [["Saman", "Saman", "Kamal", "Amal", [11, 12, 13, 14], 23, 24, 25, 26]]
// }

// There is a existing bug #39519
// function testGroupbyExpressionAndSelectWithNonGroupingKeys15() {
//     var input = [{name: "Saman", price1: 11, price2: 23},
//                     {name: "Saman", price1: 12, price2: 24},
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int|string|int[]][] names = from var {name, price1, price2} in input
//                                 group by var _ = true
//                                 select [[name], [[price1]], [(price2)]];
//                                 // @output [["Saman", "Saman", "Kamal", "Amal", [11, 12, 13, 14], 23, 24, 25, 26]]
// }

function testGroupByExpressionAndSelectWithNonGroupingKeys17() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] prices = from var {name, price} in input
                        group by name
                        select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys18() {
    var input = [{name: "Saman", price1: [11, 21]},
                    {name: "Kamal", price1: [12]},
                    {name: "Saman", price1: [13]},
                    {name: "Amal", price1: [10]}];
    int[][][] prices = from var {name, price1} in input
                    group by name
                    select [price1];
    assertEquality([[[11, 21], [13]], [[12]], [[10]]], prices);

    var prices1 = from var {name, price1} in input
                    group by name
                    select [price1];
    assertEquality([[[11, 21], [13]], [[12]], [[10]]], prices1);
}

type Rec record {|
    string name;
    int[] price1;
|};

function testGroupByExpressionAndSelectWithNonGroupingKeys19() {
    Rec[] input = [{name: "Saman", price1: [11, 21]},
                    {name: "Kamal", price1: [12]},
                    {name: "Saman", price1: [13]},
                    {name: "Amal", price1: [10]}];
    int[][][] prices = from var {name, price1} in input
                    group by name
                    select [price1];
    assertEquality([[[11, 21], [13]], [[12]], [[10]]], prices);

    var prices1 = from var {name, price1} in input
                    group by name
                    select [price1];
    assertEquality([[[11, 21], [13]], [[12]], [[10]]], prices1);
}

// group by <var def>, select <expr with non grouping keys>, lhs has the type
function testGroupByVarDefsAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var prices = from var {name, price} in input
                        group by string n = name
                        select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys2() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9}];
    var names = from var {name, price1, price2} in input
                    group by int n = price1 + price2
                    select [name]; // @output [["Saman", "Kamal"], ["Saman", "Kamal"], ["Kamal"]]
    assertEquality([["Saman", "Kamal"], ["Saman", "Kamal"], ["Kamal"]], names);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys3() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9}];
    var res = from var {name, price1, price2} in input
                group by int n = price1 + price2
                select {name: [name], price1: [price1], price2: [price2]};
                // @output [{name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]},
                //          {name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]},
                //          {name: [Kamal], price1: [10], price2: [9]}]
    assertEquality([{name: ["Saman", "Kamal"], price1: [11, 10], price2: [11, 12]},
                    {name: ["Saman", "Kamal"], price1: [11, 10], price2: [12, 13]},
                    {name: ["Kamal"], price1: [10], price2: [9]}], res);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys4() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] prices = from var {name, price} in input
                        group by string n = name
                        select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
    prices = from var {name, price} in input
                group by string _ = name
                select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
    prices = from var {name, price} in input
                group by var _ = name
                select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
    string[][] names = from var {name, price} in input
                        group by string n = name
                        select [name]; // @output [["Saman", "Saman"], [Kamal]]
    assertEquality([["Saman", "Saman"], ["Kamal"]] , names);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys5() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9}];
    int[][] prices = from var {name, price1, price2} in input
                        group by int n = price1 + price2
                        select [price1]; // @output [[11, 10], [11, 10], [10]]
    assertEquality([[11, 10], [11, 10], [10]], prices);
    string[][] names = from var {name, price1, price2} in input
                        group by int n = price1 + price2
                        select [name]; // @output [[Saman, Kamal], [Saman, Kamal], [Kamal]]
    assertEquality([["Saman", "Kamal"], ["Saman", "Kamal"], ["Kamal"]], names);

    var arr = from var {name, price1, price2} in input
                    group by var _ = true, price1
                    select [name];
    assertEquality([["Saman", "Saman"], ["Kamal", "Kamal", "Kamal"]], arr);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys6() {
    var input = [{name: "Saman", price1: 11, price2: 11}, // 22
                    {name: "Saman", price1: 11, price2: 12}, // 23
                    {name: "Kamal", price1: 10, price2: 13}, // 23
                    {name: "Kamal", price1: 10, price2: 12}, // 22
                    {name: "Kamal", price1: 10, price2: 9}]; // 19
    record {string[] name; int[] price1; int[] price2;}[] res =
                        from var {name, price1, price2} in input
                        group by int n = price1 + price2
                        select {name: [name], price1: [price1], price2: [price2]};
                        // @output [{name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]},
                        //          {name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]},
                        //          {name: [Kamal], price1: [10], price2: [9]}]
    assertEquality([{name: ["Saman", "Kamal"], price1: [11, 10], price2: [11, 12]},
                    {name: ["Saman", "Kamal"], price1: [11, 10], price2: [12, 13]},
                    {name: ["Kamal"], price1: [10], price2: [9]}], res);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys7() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    [int[], int[]][] res = from var {name, price1, price2} in input
                            group by int n1 = price1 + price2, int n2 = price1 - price2
                            select [[price1], [price2]];
                            // @output [[[11], [11]], [[11], [12]], [[10, 10], [13, 13]], [[10], [12]], [[10], [9]]]
    assertEquality([[[11], [11]], [[11], [12]], [[10, 10], [13, 13]], [[10], [12]], [[10], [9]]], res);

    var res1 = from var {name, price1, price2} in input
                group by var x = price1 + price2
                select [name];
    assertEquality([["Saman", "Kamal"], ["Saman", "Kamal", "Amal"], ["Kamal"]], res1);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys8() {
    record {|string town; record {|string name; float distance;|}[] hotels;|}[] input =  [
                                {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]},
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
    [string...][] towns = from var {town, hotels} in input
                            group by hotels
                            select [town]; // @output [Colombo2, Colombo3]
    assertEquality([["Colombo2", "Colombo3"]], towns);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeys9() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    var xx = from var item in input
                group by string name = item.name
                select [item];
    assertEquality([[{"name":"Saman", "price1":11, "price2":11}, {"name":"Saman", "price1":11, "price2":12}],
    [{"name":"Kamal", "price1":10, "price2":13}, {"name":"Kamal", "price1":10, "price2":12}, {"name":"Kamal", "price1":10, "price2":9}],
    [{"name":"Amal", "price1":10, "price2":13}]], xx);

    var yy = from var item in input
                let var p1 = item.price1
                let var p2 = item.price2
                group by string name = item.name
                select [p1];
    assertEquality([[11, 11], [10, 10, 10], [10]], yy);

    var zz = from var item in input
                let var p1 = item.price1
                let var p2 = item.price2
                group by string name = item.name
                select [[p1], [p2]];
    assertEquality([[[11, 11], [11, 12]], [[10, 10, 10], [13, 12, 9]], [[10], [13]]], zz);

    var x = from var item in input
                let var p1 = item.price1
                group by string name = item.name, var p2 = item.price2
                select [p1];
    assertEquality([[11], [11], [10], [10], [10], [10]], x);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeysWhereClause1() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    [int[], int[]][] res = from var {name, price1, price2} in input
                        group by int n1 = price1 + price2, int n2 = price1 - price2
                        where n1 == 22
                        select [[price1], [price2]];
                        // @output [[[11], [11]], [[10], [12]]]
    assertEquality([[[11], [11]], [[10], [12]]], res);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeysWhereClause2() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    [int[], int[]][] res = from var {name, price1, price2} in input
                        where price1 + price2 > 22
                        group by int n1 = price1 + price2, int n2 = price1 - price2
                        select [[price1], [price2]];
                        // @output [[[11], [12]], [[10, 10], [13, 13]]]
    assertEquality([[[11], [12]], [[10, 10], [13, 13]]], res);
}

function testGroupByVarDefsAndSelectWithNonGroupingKeysWhereClause3() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];
    string[][] res = from var {name, price1, price2} in input
                        where price1 + price2 > 22
                        group by int n1 = price1 + price2, int n2 = price1 - price2
                        select [name];
                        // @output [[Saman], [Kamal, Amal]]
    assertEquality([["Saman"], ["Kamal", "Amal"]], res);
}

// Enable after https://github.com/ballerina-platform/ballerina-lang/issues/40242
// function testGroupbyVarDefsAndSelectWithNonGroupingKeysFromClause4() {
//     var input = [{name: "Saman", price1: 11, price2: 11},
//                     {name: "Saman", price1: 11, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];
//     int[][][] prices = from var {name, price1, price2} in input
//                         group by string n = name
//                         select from var {price1: p1, price2: p2} in input
//                                 select [[price1], [price2]];
//                         assertEquality([
//                         [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]],
//                         [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]]
//                         [[10], [13]], [[10], [13]], [[10], [13]], [[10], [13]], [[10], [13]], [[10], [13]],
//                         ], res);
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeysFromClause5() {
//     var input = [{name: "Saman", price1: 11, price2: 11},
//                     {name: "Saman", price1: 11, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];
//     int[][] prices = from var {name, price1, price2} in input
//                         group by string n = name
//                         select from var p1 in [price1]
//                                 select p1;
//                         // @output
//                         // [[11, 11], [10, 10, 10], [10]]
// }

function testGroupByExpressionWithStreamOutput() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    var res1 = stream from var {name, price1, price2} in input
                    group by name
                    select [price1];
    record {| int[] value; |}|error? v = res1.next();
    int[][] output = [];
    while (v is record {| int[] value; |}) {
        output.push(v.value);
        v = res1.next();
    }
    assertEquality([[11, 15], [10, 9, 13], [30]], output);     
    stream<int[]> res2 = stream from var {name, price1, price2} in input
                    group by name
                    select [price1];
    v = res2.next();
    output = [];
    while (v is record {| int[] value; |}) {
        output.push(v.value);
        v = res2.next();
    }
    assertEquality([[11, 15], [10, 9, 13], [30]], output);  
}

function testGroupByExpressionWithStringOutput1() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 30, price2: 13}];
    string res = from var {name, price1, price2} in input
                    group by name
                    select name;
    assertEquality("SamanKamalAmal", res);
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
                                    select {name: name, prices: [price1]};
    assertEquality(res1, table key(name) [
        {name: "Saman", prices: [11, 15]},
        {name: "Kamal", prices: [10, 9, 13]},
        {name: "Amal", prices: [30]}
    ]);
    table<record {|readonly string name; int[] prices;|}> key(name) res2 = table key(name) from var {name, price1, price2} in input
                                                                                        group by name
                                                                                        select {name: name, prices: [price1]};
    assertEquality(res2, table key(name) [
        {name: "Saman", prices: [11, 15]},
        {name: "Kamal", prices: [10, 9, 13]},
        {name: "Amal", prices: [30]}
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
                                    select [name, [price1]];
    assertEquality(res1, {"Saman": [11, 15], "Kamal": [10, 9, 13], "Amal": [30]});
    map<int[]> res2 = map from var {name, price1, price2} in input
                            group by name
                            select [name, [price1]];
    assertEquality(res2, {"Saman": [11, 15], "Kamal": [10, 9, 13], "Amal": [30]});
}

type INTARR int[];
type INTARRARR INTARR[];

function testGroupByWithDoClause() {
    var input = [{name: "Saman", price1: 11, price2: 12}, 
                    {name: "Saman", price1: 13, price2: 14}, 
                    {name: "Kamal", price1: 15, price2: 16}, 
                    {name: "Kamal", price1: 17, price2: 18}, 
                    {name: "Saman", price1: 19, price2: 20}];
    int[] lengths = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    lengths.push(array:length([price1]) + array:length([price2]));
                };
    assertEquality([6, 4], lengths);

    lengths = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    int x = [price1].length() + [price2].length();
                    lengths.push(x);
                };
    assertEquality([6, 4], lengths);

    lengths = [];
    int x = 0;
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    x = [price1].length() + [price2].length();
                    lengths.push(x);
                };
    assertEquality([6, 4], lengths);

    string[] names = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    names.push(name);
                }; 
    assertEquality(["Saman", "Kamal"], names);

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
//                     foreach int p in [price1] {
//                         prices.push(p);
//                     }
//                 };
//     assertEquality([11, 13, 15, 17, 19], prices);

    input = [{name: "Saman", price1: 11, price2: 12}, 
                {name: "Saman", price1: 13, price2: 14}, 
                {name: "Kamal", price1: 25, price2: 16}, 
                {name: "Kamal", price1: 27, price2: 18}, 
                {name: "Saman", price1: 19, price2: 20}];
    boolean[] res = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    boolean b = [price1].some(someFunc);
                    res.push(b);
                };
    assertEquality([false, true], res);

    int[] arr = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    [price1].forEach(function (int i) {
                        arr.push(i);
                    });
                };
    assertEquality([11, 13, 19, 25, 27], arr);

    // This is working in the latest version

    int[][] arr1 = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    int[] p = from var p1 in [price1]
                                select p1;
                    arr1.push(p);
                };
    assertEquality([[11, 13, 19], [25, 27]], arr1);

    arr = [];
    _ = from var {name, price1, price2} in input
                group by name 
                do {
                    from var p1 in [price1]
                        do {
                            arr.push(p1 + [price2].length());
                        };
                };
    assertEquality([14, 16, 22, 27, 29], arr);

    arr1 = [];
    _ = from var {name, price1, price2} in input
            group by name
            let var p = {prices: [price1]}
            do {
                int[] prices;
                {prices} = p;
                arr1.push(prices);
            };
    assertEquality([[11, 13, 19], [25, 27]], arr1);

    arr1 = [];
    _ = from var {name, price1, price2} in input
            group by name 
            do {
                if ([price1].length() > 0) {
                    arr1.push([price1]);
                }
            };    
    assertEquality([[11, 13, 19], [25, 27]], arr1);

    arr1 = [];
    _ = from var {name, price1, price2} in input
            group by name 
            do {
                arr1.push([price1]);
            };    
    assertEquality([[11, 13, 19], [25, 27]], arr1);

    // Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/40200
    // _ = from var {name, price1, price2} in input
    //         group by name 
    //         do {
    //             match [price1] {
    //                 [...var a] => {
    //                     io:println("a");
    //                 }
    //             }
    //         };
    arr = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                while [price1].length() < 0 {
                    arr.push(100);
                }
            };
    assertEquality([], arr);    

    assertEquality([11, 13, 19], foo1());
    assertEquality([11, 13, 19], foo2());

    arr1 = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                int[] xx = [price2];
                arr1.push(xx);
            };
    assertEquality([[12, 14, 20], [16, 18]], arr1);

    INTARRARR arr2 = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                int[] xx = [price2];
                arr2.push(xx);
            };
    assertEquality([[12, 14, 20], [16, 18]], arr2);

    arr1 = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                [int...] xx = [price2];
                arr1.push(xx);
            };
    assertEquality([[12, 14, 20], [16, 18]], arr1);

    arr = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                [int...] xx = [price2];
                arr.push(xx[0]);
            };
    assertEquality([12, 16], arr);

    arr = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                arr.push([price2][0]);
            };
    assertEquality([12, 16], arr);

    // Check after https://github.com/ballerina-platform/ballerina-lang/issues/40216
    // Rec[] input = [{name: "Saman", price1: [11, 12]}, 
    //                 {name: "Saman", price1: [19, 20]}];
    // int[] arr = [];
    // _ = from var {name, price1} in input
    //         group by name
    //         do {
    //             function () returns [int...] func = function () returns [int...] => [...price1];
    //         };

    arr1 = [];
    _ = from var {name, price1, price2} in input
            group by name // name : Saman, price1 : [11, 13, 19], price2 : [12, 14, 20]
            do {
                record {| int[] prices; |} r = {prices: [price1]};
                arr1.push(r.prices);
            };
    assertEquality([[11, 13, 19], [25, 27]], arr1);

    arr = [];
    _ = from var {name, price1, price2} in input
            group by name
            do {
                int i = 0;
                i += [price1].length();
                arr.push(i);
            };
    assertEquality([3, 2], arr);

    // Check after https://github.com/ballerina-platform/ballerina-lang/issues/40228
    // _ = from var {name, price1, price2} in input
            // group by name
    //         do {
    //             var obj = object {
    //                 [int...] p1 = [price1];
    //             };
    //         }; 

    // Check after https://github.com/ballerina-platform/ballerina-lang/issues/40229
    // _ = from var {name, price1, price2} in input
    //         group by name
    //         do {
    //             _ = let var p1 = [price1] in p1;
    //         };   

    arr1 = [];
    _ = from var item in input
            group by var n = item.name
            let int[] ar = []
            do {
                foreach var i in [item] {
                    ar.push(i.price1);
                }
                arr1.push(ar);
            };
    assertEquality([[11, 13, 19], [25, 27]], arr1);

    arr1 = [];
    _ = from var item in input
            group by var n = item.name
            do {
                arr1.push(foo3(...[item]));
            };
    assertEquality([[11, 13, 19], [25, 27]], arr1);
}

function foo1() returns int[] {
    var input = [{name: "Saman", price1: 11, price2: 12}, 
                    {name: "Saman", price1: 13, price2: 14}, 
                    {name: "Kamal", price1: 25, price2: 16}, 
                    {name: "Kamal", price1: 27, price2: 18}, 
                    {name: "Saman", price1: 19, price2: 20}];
    int[] arr = [];
    _ = from var {name, price1, price2} in input
                group by name
                do {
                    return [price1];
                };  
    return [];  
}

function foo2() returns [int...] {
    var input = [{name: "Saman", price1: 11, price2: 12}, 
                    {name: "Saman", price1: 13, price2: 14}, 
                    {name: "Kamal", price1: 25, price2: 16}, 
                    {name: "Kamal", price1: 27, price2: 18}, 
                    {name: "Saman", price1: 19, price2: 20}];
    int[] arr = [];
    _ = from var {name, price1, price2} in input
                group by name
                do {
                    return [price1];
                };  
    return [];  
}

type Rec1 record {|
    string name;
    int price1;
    int price2;
|};

function foo3(Rec1... recs) returns int[] {
    int[] arr = [];
    foreach var item in recs {
        arr.push(item.price1);
    }
    return arr;
}

function someFunc(int i) returns boolean {
    return i > 20;
}

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
                        select [y];
    assertEquality([[[22, 22]], [[19, 19, 19]], [[23]]], res1);

    var res2 = from var {name, price1, price2} in input
              group by name
              let var minPrice2 = int:min(200, price2)
              select (from var x in [price1]
                        let int y = x + minPrice2
                        group by var _ = true
                        select [y])[0];
    assertEquality([[22, 22], [19, 19, 19], [23]], res2);

    var res3 = from var {name, price1, price2} in input
              group by name
              let var minPrice2 = int:min(200, price2)
              let var zz = from var x in [price1] let int y = x + minPrice2 group by var _ = true select [y]
              select zz;
    assertEquality([[[22, 22]], [[19, 19, 19]], [[23]]], res3);

    var res4 = from var {name, price1, price2} in input
              group by name
              let var minPrice2 = int:min(200, price2)
              let var zz = (from var x in [price1] let int y = x + minPrice2 group by var _ = true select [y])[0]
              select zz;
    assertEquality([[22, 22], [19, 19, 19], [23]], res4);

    var res5 = from var {name, price1, price2} in input
                group by var xx = (from var x in input group by name select int:sum(price1))
                select [name];
    assertEquality([["Saman", "Saman"], ["Kamal", "Kamal", "Kamal", "Amal"]], res5);

    var res6 = from var {name, price1, price2} in input
                group by var xx = (from var x in input group by var n = x.name select int:sum(price1))
                select [name];
    assertEquality([["Saman", "Saman"], ["Kamal", "Kamal", "Kamal", "Amal"]], res6);

    var res7 = from var {name, price1, price2} in input
                group by var xx = (from var {name: n, price1: p1, price2: p2} in input group by n select sum(p1))
                select [name];
    assertEquality([["Saman", "Saman", "Kamal", "Kamal", "Kamal", "Amal"]], res7);
}

function testOptionalFieldsInInput() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price2: 13}];

    var res1 = from record {|string name; int price1?; int price2;|} rec in input
                group by var n = rec.name
                select n;
    assertEquality(["Saman", "Kamal", "Amal"], res1);

    var res2 = from var rec in input
                group by var n = rec.name
                select n;
    assertEquality(["Saman", "Kamal", "Amal"], res2);
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
                select [name];
    assertEquality([["Saman", "Amal"], ["Kamal"]], x1);

    var x2 = from var {name, price1, price2} in input1
                group by price1
                group by price1
                select price1;
    assertEquality([11, 10], x2);

    var x3 = from var {name, price1, price2} in input1
                group by price1
                let var n = [name]
                group by price1
                select [n];
    assertEquality([[["Saman", "Saman", "Amal", "Amal"]], [["Kamal", "Kamal", "Kamal"]]], x3);

    var input2 = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 11},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];

    var x4 = from var {name, price1, price2} in input2
                group by price1
                let var n = [name]
                group by var p2 = [price2]
                select [n];
    assertEquality([[["Saman", "Saman"], ["Kamal", "Kamal"]], [["Amal", "Amal"]]], x4);

    var x5 = from var {name, price1, price2} in input2
                group by price1
                let var n1 = [name]
                group by var p2 = [price2]
                let var n2 = [n1]
                group by p2
                select [n2];
    assertEquality([[[["Saman", "Saman"], ["Kamal", "Kamal"]]], [[["Amal", "Amal"]]]], x5);

    var x6 = from var {name, price1, price2} in input2
                group by price1
                let var n = [name]
                group by n
                select n;
    assertEquality([["Saman", "Saman"], ["Kamal", "Kamal"], ["Amal", "Amal"]], x6);

    var input3 = [{name: "Saman", price1: 11, price2: 12},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];

    var x7 = from var {name, price1, price2} in input3
                group by price1, price2
                let var n = [name]
                group by var _ = price1 + price2
                select [n];
    assertEquality([[["Saman", "Saman", "Kamal"]], [["Kamal"]], [["Amal"]], [["Amal"]]], x7);

    var x8 = from var {name, price1, price2} in input3
                group by price1, price2
                let var s = [name]
                group by var _ = from var {name: n, price1: p1, price2: p2} in input3 group by var p = p1 + p2 select p
                select [s];
    assertEquality([[["Saman", "Saman", "Kamal"], ["Kamal"], ["Amal"], ["Amal"]]], x8);

    var x9 = from var {name, price1, price2} in input3
                group by price1, price2
                let var s = [name]
                group by var _ = s.length()
                select [s];
    assertEquality([[["Saman", "Saman", "Kamal"]], [["Kamal"], ["Amal"], ["Amal"]]], x9);

    var x10 = from var {name, price1, price2} in input3
                group by price1, price2
                let var s = [name]
                group by var l = s.length()
                select l;
    assertEquality([3, 1], x10);
}

function testMultipleFromClauses() {
    var input = [{name: "Saman", price1: 11, price2: 12},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Amal", price1: 12, price2: 13},
                    {name: "Amal", price1: 12, price2: 15}];
    var x1 = from var {name, price1, price2} in input
                group by name
                let var x = from var p in [price1] select p
                select x;
    assertEquality([[11, 11], [11, 10], [12, 12]], x1);

    var x2 = from var {name, price1, price2} in input
                group by name
                let var x = (from var p in [price1] select p).length()
                select x;
    assertEquality([2, 2, 2], x2);

    var x3 = from var {name, price1, price2} in input
                group by name
                let var x = (from var p in [price1] select p).length() + (from var p in [price2] select p).length()
                select x;
    assertEquality([4, 4, 4], x3);
}

type RecOpt record {|
    string name;
    int price1;
    int price2?;
|};

function testOptionalFieldInput() {
    RecOpt[] input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 15, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 9, price2: 12},
                    {name: "Kamal", price1: 13, price2: 9},
                    {name: "Amal", price1: 14},
                    {name: "Amali", price1: 14}];

    var x1 = from var {name, price1, price2} in input
                group by price2
                select [name];
    assertEquality([["Saman"], ["Saman", "Kamal"], ["Kamal"], ["Kamal"], ["Amal", "Amali"]], x1);

    var x2 = from var {name, price1, price2} in input
                group by price2
                select price2;
    assertEquality([11, 12, 13, 9, null], x2);

    var x3 = from var {name, price1, price2} in input
                group by name
                select [price2];
    assertEquality([[11, 12], [13, 12, 9], [], []], x3);

    var x4 = from var {name, price1, price2} in input
                group by name
                select [price2].length();
    assertEquality([2, 3, 0, 0], x4);

    var x5 = from var {name: n, price1: p1, price2: p2} in input
                group by n
                select [p2].length();
    assertEquality([2, 3, 0, 0], x5);
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
                select name;  
    assertEquality([SAMAN, KAMAL, AMAL], x1);  

    var x2 = from var {name, price1, price2} in input 
                group by name
                select [price1];  
    assertEquality([[11, 11], [12, 12], [19]], x2); 
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
                select [price1];
    assertEquality([], res1);
    var res2 = from var {name, price1, price2} in input
                where name == "No name"
                group by var _ = true 
                select [price2].length();   
    assertEquality([], res2);
    [int...][] res3 = from var {name, price1, price2} in input
                        where name == "No name"
                        group by var _ = true 
                        select [price1];   
    assertEquality([], res3);
}

function testErrorSeq() {
    var input = [{name: "SAMAN", err: error("msg1")}, 
                    {name: "SAMAN", err: error("msg2")}]; 

    var x = from var {name, err} in input
                group by name
                select [err];
    assertEquality("msg1", x[0][0].message());
    
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

// TODO: Add test cases readonly types
// TODO: use a client
// TODO: xml langlib function
// TODO: handle named args
