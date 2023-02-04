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

// group by <expression>, select <expr with grouping keys>, lhs has the type
function testGroupByExpressionAndSelectWithGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by name
                        select name; // @output ["Saman", "Kamal"]
    assertEquality(["Saman", "Kamal"], names);
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
    assertEquality([[["Colombo2", "Colombo3"], ["Colombo4"]]], res);
}

// function testGroupByExpressionAndSelectWithGroupingKeys11() {
//     record {|string town; record {|string name; float distance;|}[] hotels;|}[] input = [
//                             {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
//                             {town: "Colombo4", hotels: [{name: "HotelB", distance: 2}]}, 
//                             {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}];
//     [string...] res = from var {town, hotels} in input
//                     group by hotels
//                     select [town]; // @output [["Colombo2", "Colombo3"], ["Colombo4"]]
//     assertEquality([[["Colombo2", "Colombo3"], ["Colombo4"]]], res);
// }

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

// function append(string name) returns string {
//     return name + " Kumara";
// }

function testGroupByExpressionAndSelectWithGroupingKeysFromClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    int[][] prices = from var {name} in input
                        group by name
                        select from var {price} in input
                                select price; // @output [[11, 12, 11, 12], [11, 12, 11, 12], [11, 12, 11, 12]]
    assertEquality([[11, 12, 11, 12], [11, 12, 11, 12], [11, 12, 11, 12]], prices);
}

// function testGroupByExpressionAndSelectWithGroupingKeysFromClause2() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
//     record {|string name; int price;|}[][] res = from var {name} in input
//                                                     group by name
//                                                     select from var {price} in input
//                                                             select {name, price}; 
                                                    // @output [[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}], 
                                                    //          [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}], 
                                                    //          [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]]  
    // assertEquality([[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}],
    //                 [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}],
    //                 [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]], res);                                                      
// }

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

// // group by <var def>, select <expr with grouping keys>, lhs has the type
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

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by var n = name
                        where n == "Kamal"
                        select n; // @output ["Kamal"]
    assertEquality(["Kamal"], names);
}

function testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause2() {
    var input = [
        {name: "Saman", price1: 11, price2: 11}, 
        {name: "Saman", price1: 12, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21}, 
        {name: "Amal", price1: 11, price2: 10}, 
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by var n = name, var p = price2
                                                where p > 20
                                                select {name: n, price: p}; 
                                                // @output [{name: "Kamal", price: 21}]    
    assertEquality([{name: "Kamal", price: 21}], res);
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
    record {|string name; int price;|}[] res = from var {name, price1, price2} in input
                                                group by string n = name, price1, var p2 = price2
                                                let var total = getTotal(price1, p2)
                                                where total < 30
                                                select {name: n, price: p2};
                                                // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]    
    assertEquality([{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}], res);
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

// function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause1() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
//     record {|string n; int price;|}[][] names = from var {name} in input
//                                                         group by var n = name
//                                                         select from var {price} in input
//                                                                 select {n, price}; 
//                                                     // @output [[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}], 
//                                                     //          [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}], 
//                                                     //          [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]]    
// }

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

    string[] res = from var person in personList
                    join var {id, name: deptName} in deptList
                    on person.id equals id
                    group by string n = deptName
                    select n; // @output ["HR", "Operations"]
    assertEquality(["HR", "Operations"], res);
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

// // group by <expression>, select <expr with non grouping keys>, lhs has the type
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
    var prices = from var {name, price} in input
                        group by name
                        let var p = [price]
                        select p; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], prices);
}

// TODO: check unused varialbe check
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
}

// function testGroupbyExpressionAndSelectWithNonGroupingKeys3() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     int[][] prices = from var {name, price} in input
//                         group by name
//                         select [price]; // @output [[11, 12], [11]]
//     assertEquality([[11, 12], [11]], prices);
// }

// function testGroupbyExpressionAndSelectWithNonGroupingKeys2() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     int[][][] names = from var {name, price} in input
//                         group by name
//                         select [[price]]; // @output [[[11, 11]], [[12]]]
// }

function testGroupByExpressionAndSelectWithNonGroupingKeys4() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var prices = from var {name, price} in input
                    group by name
                    select {price: [price]}; // @output [{price: [11, 12]}, {price: [11]}]
    assertEquality([{price: [11, 12]}, {price: [11]}], prices);
}

// function testGroupbyExpressionAndSelectWithNonGroupingKeys4() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     record {int[] price;}[] names = from var {name, price} in input
//                         group by name
//                         select {price: [price]}; // @output [{price: [11, 12]}, {price: [11]}]
// }

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

// function testGroupbyExpressionAndSelectWithNonGroupingKeys8() {
//     var input = [{name: "Saman", price1: 11, price2: 23}, 
//                     {name: "Saman", price1: 12, price2: 24}, 
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int][] names = from var {name, price1, price2} in input
//                         group by name
//                         select [[price1], [price2]]; // @output [[11, 12, 23, 24], [13, 25], [14, 26]]
// }

function testGroupByExpressionAndSelectWithNonGroupingKeys6() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    var res = from var {name, price1, price2} in input
                group by name
                select [[price1], 34]; // @output [[11, 12, 34], [13, 34], [14, 34]]
    assertEquality([[[11,12],34],[[13],34],[[14],34]], res);
}

// function testGroupbyExpressionAndSelectWithNonGroupingKeys9() {
//     var input = [{name: "Saman", price1: 11, price2: 23}, 
//                     {name: "Saman", price1: 12, price2: 24}, 
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int][] names = from var {name, price1, price2} in input
//                         group by name
//                         select [[price1], 34]; // @output [[11, 12, 34], [13, 34], [14, 34]]
// }

// function testGroupbyExpressionAndSelectWithNonGroupingKeys10() {
//     var input = [{name: "Saman", price1: 11, price2: 23}, 
//                     {name: "Saman", price1: 12, price2: 24}, 
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int][] names = from var {name, price1, price2} in input
//                         group by name
//                         select [21, [price1]]; // @output [[21, 11, 12], [21, 13], [21, 14]]
// }

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

// function testGroupbyExpressionAndSelectWithNonGroupingKeys11() {
//     var input = [{name: "Saman", price1: 11, price2: 23}, 
//                     {name: "Saman", price1: 12, price2: 24}, 
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int|string][] names = from var {name, price1, price2} in input
//                                 group by var _ = true
//                                 select [[name], [price1], [price2]]; 
//                                 // @output [["Saman", "Saman", "Kamal", "Amal", 11, 12, 13, 14, 23, 24, 25, 26]]
// }

// function testGroupbyExpressionAndSelectWithNonGroupingKeys12() {
//     var input = [{name: "Saman", price1: 11, price2: 23}, 
//                     {name: "Saman", price1: 12, price2: 24}, 
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     record {string[] name; int[] price;}[] names = from var {name, price1, price2} in input
//                                                     group by var _ = true
//                                                     select {name: [name], price: [price2]}; 
//                                                     // @output [{name: ["Saman", "Saman", "Kamal", "Amal"], price: [23, 24, 25, 26]}]
// }

// function testGroupbyExpressionAndSelectWithNonGroupingKeys13() {
//     var input = [{name: "Saman", price1: 11, price2: 23}, 
//                     {name: "Saman", price1: 12, price2: 24}, 
//                     {name: "Kamal", price1: 13, price2: 25},
//                     {name: "Amal", price1: 14, price2: 26}];
//     [int|string|int[]][] names = from var {name, price1, price2} in input
//                                 group by var _ = true
//                                 select [[name], [[price1]], [price2]]; 
//                                 // @output [["Saman", "Saman", "Kamal", "Amal", [11, 12, 13, 14], 23, 24, 25, 26]]
// }

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

// function testGroupbyExpressionAndSelectWithNonGroupingKeys16() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     int[][] names = from var {name, price} in input
//                         group by name
//                         select [price]; // @output [[11, 12], [11]]
// }
// // Add tests with from var item in input_record_array -> no destructure

// // group by <var def>, select <expr with non grouping keys>, lhs has the type
function testGroupByVarDefsAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    var names = from var {name, price} in input
                        group by string n = name
                        select [price]; // @output [[11, 12], [11]]
    assertEquality([[11, 12], [11]], names);
}

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys1() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     int[][] names = from var {name, price} in input
//                         group by string n = name
//                         select [price]; // @output [[11, 12], [11]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys2() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     int[][] names = from var {name, price} in input
//                         group by string _ = name
//                         select [price]; // @output [[11, 12], [11]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys3() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     int[][] names = from var {name, price} in input
//                         group by var _ = name
//                         select [price]; // @output [[11, 12], [11]]
// }

// // TODO: check this special case
// function testGroupbyVarDefsAndSelectWithNonGroupingKeys4() {
//     var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
//     string[][] names = from var {name, price} in input
//                         group by string n = name 
//                         select [name]; // @output [["Saman", "Saman"], [Kamal]] 
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys5() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9}];
//     string[][] names = from var {name, price} in input
//                         group by int n = price1 + price2 
//                         // n: 22, name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]
//                         // n: 23, name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]
//                         // n: 19, name: [Kamal], price1: [10], price2: [9]
//                         select [name]; // @output [["Saman", "Kamal"], ["Saman", "Kamal"], ["Kamal"]]
// }

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

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys6() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9}];
//     int[][] names = from var {name, price} in input
//                         group by int n = price1 + price2 
//                         // n: 22, name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]
//                         // n: 23, name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]
//                         // n: 19, name: [Kamal], price1: [10], price2: [9]
//                         select [price1]; // @output [[11, 10], [11, 10], [10]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys7() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9}];
//     string[][] names = from var {name, price} in input
//                         group by int n = price1 + price2 
//                         // n: 22, name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]
//                         // n: 23, name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]
//                         // n: 19, name: [Kamal], price1: [10], price2: [9]
//                         select [name]; // @output [[Saman, Kamal], [Saman, Kamal], [Kamal]]
// }

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

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys7() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9}];
//     record {string[] name; int[] price1; int[] price2;}[] res = 
//                         from var {name, price} in input
//                         group by int n = price1 + price2 
//                         // n: 22, name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]
//                         // n: 23, name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]
//                         // n: 19, name: [Kamal], price1: [10], price2: [9]
//                         select {name: [name], price1: [price1], price2: [price2]}; 
//                         // @output [{name: [Saman, Kamal], price1: [11, 10], price2: [11, 12]},
//                         //          {name: [Saman, Kamal], price1: [11, 10], price2: [12, 13]},
//                         //          {name: [Kamal], price1: [10], price2: [9]}]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys8() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13},];
//     [int[], int[]][] res = from var {name, price} in input
//                         group by int n1 = price1 + price2, int n2 = price1 - price2
//                         // n1: 22, n2: 0, name: [Saman], price1: [11], price2: [11]
//                         // n1: 23, n2: -1, name: [Saman], price1: [11], price2: [12]
//                         // n1: 23, n2: -3, name: [Kamal, Amal], price1: [10, 10], price2: [13, 13]
//                         // n1: 22, n2: -2, name: [Kamal], price1: [10], price2: [12]
//                         // n1: 19, n2: 1, name: [Kamal], price1: [10], price2: [9]
//                         select [[price1], [price2]];
//                         // @output [[[11], [11]], [[11], [12]], [[10, 10], [13, 13]], [[10], [12]], [[10], [9]]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeys9() {
//     record {|string town; record {|string name; float distance;|}[] hotels;|}[] input =  [
//                                 {town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
//                                 {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
//     string[] towns = from var {town, hotels} in input
//                         group by hotels
//                         select town; // @output [Colombo2, Colombo3]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeysWhereClause1() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13},];
//     [int[], int[]][] res = from var {name, price} in input
//                         group by int n1 = price1 + price2, int n2 = price1 - price2
//                         where n1 = 22
//                         select [[price1], [price2]];
//                         // @output [[[11], [11]], [[10], [12]]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeysWhereClause2() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];
//     [int[], int[]][] res = from var {name, price} in input
//                         where price1 + price2 > 22
//                         group by int n1 = price1 + price2, int n2 = price1 - price2
//                         select [[price1], [price2]];
//                         // @output [[[11], [12]]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeysWhereClause3() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13},];
//     string[][] names = from var {name, price} in input
//                         where price1 + price2 > 22
//                         group by int n1 = price1 + price2, int n2 = price1 - price2
//                         select [name];
//                         // @output [[Saman], [Kamal, Amal]]
// }

// function testGroupbyVarDefsAndSelectWithNonGroupingKeysFromClause4() {
//     var input = [{name: "Saman", price1: 11, price2: 11}, 
//                     {name: "Saman", price1: 11, price2: 12}, 
//                     {name: "Kamal", price1: 10, price2: 13},
//                     {name: "Kamal", price1: 10, price2: 12},
//                     {name: "Kamal", price1: 10, price2: 9},
//                     {name: "Amal", price1: 10, price2: 13}];
//     int[][][] prices = from var {name, price1, price2} in input
//                         group by string n = name  
//                         // n: Saman, name: [Saman, Saman], price1: [11, 11], price2: [11, 12]
//                         // n: Kamal, name: [Kamal, Kamal, Kamal], price1: [10, 10, 10], price2: [13, 12, 9] 
//                         // n: Amal, name: [Amal], price1: [10], price2: [13]                                      
//                         select from var {price1: p1, price2: p2} in input
//                                 select [[price1], [price2]];
//                         // @output 
//                         // [
//                         // [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]], [[11, 11], [11, 12]],
//                         // [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]], [[10, 10, 10], [13, 12, 9]]
//                         // [[10], [13]], [[10], [13]], [[10], [13]], [[10], [13]], [[10], [13]], [[10], [13]],
//                         // ]
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

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}