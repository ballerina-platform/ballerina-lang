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

// group by <expression>, select <expr with grouping keys>, lhs has the type
function testGroupbyExpressionAndSelectWithGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by name
                        select name; // @output ["Saman", "Kamal"]
}

function testGroupbyExpressionAndSelectWithGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] names = from var {price} in input
                        group by price
                        select price; // @output [11, 12]
}

function testGroupbyExpressionAndSelectWithGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    string[] names = from var {name, price} in input
                        group by name, price
                        select name; // @output ["Saman", "Saman", "Kamal"]
}

function testGroupbyExpressionAndSelectWithGroupingKeys4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11}, 
        {name: "Saman", price1: 12, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21}, 
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by name, price2
                                                    select {name, price: price2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Kamal", price: 21}]
}

function testGroupbyExpressionAndSelectWithGroupingKeys5() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[][] names = from var {name} in input
                        group by name
                        select [name]; // @output [["Saman"], ["Kamal"]]
}

function testGroupbyExpressionAndSelectWithGroupingKeys6() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 11}];
    [string, int][] names = from var {name, price} in input
                        group by name, price
                        select [name, price]; // @output [["Saman", 11], ["Saman", 12], ["Kamal", 11]]
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by name
                        where name == "Kamal"
                        select name; // @output ["Kamal"]
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause2() {
    var input = [
        {name: "Saman", price1: 11, price2: 11}, 
        {name: "Saman", price1: 12, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21}, 
        {name: "Amal", price1: 11, price2: 10}, 
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by name, price2
                                                    where price2 > 20
                                                    select {name, price: price2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Saman", price: 10}]    
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause3() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    where price1 + price2 < 30
                                                    group by name, price2
                                                    select {name, price: price2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Kamal", price: 21}, {name:"Amal", price: 10}]    
}

function getTotal(int... ns) returns int {
    return int:sum(...ns);
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 13},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    where getTotal(price1, price2) < 30
                                                    group by name, price2
                                                    select {name, price: price2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Kamal", price: 21}, {name:"Amal", price: 10}]    
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause5() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by name, price1, price2
                                                    where getTotal(price1, price2) < 30
                                                    select {name, price: price2};
                                                    // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]    
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause6() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by name, price1, price2
                                                    let var total = getTotal(price1, price2)
                                                    where total < 30
                                                    select {name, price: price2};
                                                    // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]    
}

function testGroupbyExpressionAndSelectWithGroupingKeysAndWhereClause7() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by name, price1, price2
                                                    where price1 + price2 < 30
                                                    select {name, price: price2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}, {name:"Saman", price: 11}]    
}

function append(string name) returns string {
    return name + " Kumara";
}

function testGroupbyExpressionAndSelectWithGroupingKeysFromClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    int[][] names = from var {name} in input
                        group by name
                        select from var {price} in input
                                select price; // @output [[11, 12, 11, 12], [11, 12, 11, 12], [11, 12, 11, 12]]
}

function testGroupbyExpressionAndSelectWithGroupingKeysFromClause2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    record {|string name; int price;|}[][] names = from var {name} in input
                                                        group by name
                                                        select from var {price} in input
                                                                select {name, price}; 
                                                    // @output [[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}], 
                                                    //          [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}], 
                                                    //          [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]]    
}

// This will not working due to an existing bug
function testGroupbyExpressionAndSelectWithGroupingKeysFromClause3() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by hotels
                        select from var {name} in hotels
                                group by name
                                select name; // @output [[HotelA], [HotelB]]
}

// This will not waoking due to an existing bug
function testGroupbyExpressionAndSelectWithGroupingKeysFromClause4() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by hotels
                        select from var {name} in hotels
                                where name == "HotelA"
                                group by name
                                select name; // @output [[HotelA]]
}

// This will not waoking due to an existing bug
function testGroupbyExpressionAndSelectWithGroupingKeysFromClause5() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by hotels
                        select from var {name} in hotels
                                group by name
                                where name == "HotelA"
                                select name; // @output [[HotelA]]
}

// This will not waoking due to an existing bug
function testGroupbyExpressionAndSelectWithGroupingKeysFromClause6() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by hotels
                        select from var {name} in hotels
                                group by name
                                let boolean isCorretName = name == "HotelA"
                                where isCorretName
                                select name; // @output [[HotelA]]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithJoinClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] deptPersonList = from var person in personList
                                join var {id, name: deptName} in deptList
                                on person.id equals id
                                group by deptName
                                select deptName; // @output ["HR", "Operations"]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithJoinClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    record {|int id; string deptName;|}[] deptPersons = from var person in personList
                                                            join var {id, name: deptName} in deptList
                                                            on person.id equals id
                                                            group by id, deptName
                                                            select {id, deptName}; // @output [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithJoinClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] persons = from var {id, fname, lname} in personList
                        join var {id: deptId, name: deptName} in deptList
                        on id equals deptId
                        group by lname
                        select lname; // @output ["George", "Fonseka"]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithJoinClause4() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int id; string lname; string deptName;|}[] persons = from var {id, fname, lname} in personList
                                                                    group by id, lname
                                                                    join var {id: deptId, name: deptName} in deptList
                                                                    on id equals deptId
                                                                    select {id, lname, deptName}; 
                                                                    // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithJoinClause5() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int id; string lname; string deptName;|}[] persons = from var {id, fname, lname} in personList
                                                                    group by id, lname
                                                                    join var {id: deptId, name: deptName} in deptList
                                                                    on id equals deptId
                                                                    select {id, lname, deptName}; 
                                                                    // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithOrderbyClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    group by id, lname
                                                    order by id
                                                    select {id, lname}; 
                                                    // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithOrderbyClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    order by id
                                                    group by id, lname
                                                    select {id, lname}; 
                                                    // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithOrderbyClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    order by id, lname
                                                    group by id, lname
                                                    select {id, lname}; 
                                                    // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithLimitClause() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    group by id, lname
                                                    limit 2
                                                    select {id, lname}; 
                                                    // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithTableResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    table<record {|readonly int id; string lname;|}> key(id) t = table key(id) from var {id, fname, lname} in personList
                                                                                    group by id, lname
                                                                                    select {id, lname};
                                                                                    // @output [{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithMapResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    map<string> m = map from var {id, fname, lname} in personList
                        group by id, lname
                        order by id
                        select [id.toString(), lname]; // @output [{"1":"George"}, {"2":"Silva"}, {"3":"Fonseka"}]
}

function testGroupbyExpressionAndSelectWithGroupingKeysWithFromClause() {
    var personList1 = [{id: 1, fname: "Alex"}, {id: 2, fname: "Ranjan"}, {id: 3, fname: "Ranjan"}];
    var personList2 = [{index: 1, lname: "George"}, {index: 2, lname: "Fonseka"}, {index: 3, lname: "Silva"}];
    string[] xx = from var {id, fname} in personList1
                    from var {index, lname} in personList2
                    where id == index
                    group by fname
                    select fname; // @output ["Alex", "Ranjan"]
}    

// group by <var def>, select <expr with non grouping keys>, lhs has the type
function testGroupbyVarDefsAndSelectWithGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by var n = name
                        select n; // @output ["Saman", "Kamal"]
}

function testGroupbyVarDefsAndSelectWithGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[] names = from var {price} in input
                    group by int p = price
                    select p; // @output [11, 12]
}

function testGroupbyVarDefsAndSelectWithGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    string[] names = from var {name, price} in input
                        group by string n = name, int p = price
                        select n; // @output ["Saman", "Saman", "Kamal"]
}

function testGroupbyVarDefsAndSelectWithGroupingKeys4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11}, 
        {name: "Saman", price1: 12, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21}, 
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by name, int p = price2
                                                    select {name, price: p}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Saman", price: 10}, {name: "Kamal", price: 21}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeys5() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[][] names = from var {name} in input
                        group by var n = name
                        select [n]; // @output [["Saman"], ["Kamal"]]
}

function testGroupbyVarDefsAndSelectWithGroupingKeys6() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 11}];
    [string, int][] names = from var {name, price} in input
                        group by string n = name, int p = price
                        select [n, p]; // @output [["Saman", 11], ["Saman", 12], ["Kamal", 11]]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    string[] names = from var {name} in input
                        group by var n = name
                        where n == "Kamal"
                        select n; // @output ["Kamal"]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause2() {
    var input = [
        {name: "Saman", price1: 11, price2: 11}, 
        {name: "Saman", price1: 12, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21}, 
        {name: "Amal", price1: 11, price2: 10}, 
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by var n = name, var p = price2
                                                    where p > 20
                                                    select {n, price: p}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Saman", price: 10}]    
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause3() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    where price1 + price2 < 30
                                                    group by string n = name, price2
                                                    select {n, price: price2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Kamal", price: 21}, {name:"Amal", price: 10}]    
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause4() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 13},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    where getTotal(price1, price2) < 30
                                                    group by name, var p2 = price2
                                                    select {name, price: p2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Kamal", price: 21}, {name:"Amal", price: 10}]    
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause5() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by var n = name, var p1 = price1, int p2 = price2
                                                    where getTotal(p1, p2) < 30
                                                    select {name: n, price: p2};
                                                    // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]    
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause6() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10},
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by string n = name, price1, var p2 = price2
                                                    let var total = getTotal(price1, p2)
                                                    where total < 30
                                                    select {n, price: p2};
                                                    // @output [{name: "Saman", price: 11}, {name:"Amal", price: 10}, {name: "Saman", price: 11}]    
}

function testGroupbyVarDefsAndSelectWithGroupingKeysAndWhereClause7() {
    var input = [
        {name: "Saman", price1: 11, price2: 11},
        {name: "Saman", price1: 22, price2: 10}, 
        {name: "Kamal", price1: 11, price2: 21},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Amal", price1: 11, price2: 10},
        {name: "Saman", price1: 12, price2: 11}];
    record {|string name; int price;|}[] names = from var {name, price1, price2} in input
                                                    group by string n = name, var p1 = price1, int p2 = price2
                                                    where p1 + p2 < 30
                                                    select {name: n, price: p2}; 
                                                    // @output [{name: "Saman", price: 11}, {name: "Amal", price: 10}, {name:"Saman", price: 11}]    
}

function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    int[][] names = from var {name} in input
                        group by string n = name
                        select from var {price} in input
                                select price; // @output [[11, 12, 11, 12], [11, 12, 11, 12], [11, 12, 11, 12]]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Amal", price: 12}];
    record {|string n; int price;|}[][] names = from var {name} in input
                                                        group by var n = name
                                                        select from var {price} in input
                                                                select {n, price}; 
                                                    // @output [[{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Saman", price: 11}, {name: "Saman", price: 12}], 
                                                    //          [{name: "Kamal", price: 11}, {name: "Kamal", price: 12}, {name: "Kamal", price: 11}, {name: "Kamal", price: 12}], 
                                                    //          [{name: "Amal", price: 11}, {name: "Amal", price: 12}, {name: "Amal", price: 11}, {name: "Amal", price: 12}]]    
}

// This will not waoking due to an existing bug
function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause3() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by record {|string name; decimal distance;|} groupedHotels = hotels
                        select from var {name} in hotels
                                group by name
                                select name; // @output [[HotelA], [HotelB]]
}

// This will not waoking due to an existing bug
function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause4() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by var groupedHotels = hotels
                        select from var {name} in hotels
                                where name == "HotelA"
                                group by name
                                select name; // @output [[HotelA]]
}

// This will not waoking due to an existing bug
function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause5() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by record {|string name; decimal distance;|} groupedHotels = hotels
                        select from var {name} in groupedHotels
                                group by name
                                where name == "HotelA"
                                select name; // @output [[HotelA]]
}

// This will not waoking due to an existing bug
function testGroupbyVarDefsAndSelectWithGroupingKeysFromClause6() {
    string[][] names = from var {hotels} in [{town: "Colombo2", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}, 
                                {town: "Colombo3", hotels: [{name: "HotelA", distance: 2}, {name: "HotelB", distance: 0.8}]}]
                        group by var groupedHotels = hotels
                        select from var {name} in groupedHotels
                                group by name
                                let boolean isCorretName = name == "HotelA"
                                where isCorretName
                                select name; // @output [[HotelA]]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithJoinClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] deptPersonList = from var person in personList
                                join var {id, name: deptName} in deptList
                                on person.id equals id
                                group by string n = deptName
                                select n; // @output ["HR", "Operations"]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithJoinClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Kumara"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    record {|int id; string deptName;|}[] deptPersons = from var person in personList
                                                            join var {id, name: deptName} in deptList
                                                            on person.id equals id
                                                            group by var i = id, var n = deptName
                                                            select {id: i, deptName: n}; // @output [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithJoinClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 3, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}, {id: 3, name:"HR"}];

    string[] persons = from var {id, fname, lname} in personList
                        join var {id: deptId, name: deptName} in deptList
                        on id equals deptId
                        group by var ln = lname
                        select ln; // @output ["George", "Fonseka"]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithJoinClause4() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int i; string ln; string deptName;|}[] persons = from var {id, fname, lname} in personList
                                                                    group by int i = id, string ln = lname
                                                                    join var {id: deptId, name: deptName} in deptList
                                                                    on i equals deptId
                                                                    select {i, ln, deptName}; 
                                                                    // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithJoinClause5() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, {id: 2, fname: "Ranjan", lname: "Fonseka"}, {id: 2, fname: "Amal", lname: "Fonseka"}];
    var deptList = [{id: 1, name:"HR"}, {id: 2, name:"Operations"}];

    record {|int id; string lname; string deptName;|}[] persons = from var {id, fname, lname} in personList
                                                                    group by var i = id, var ln = lname
                                                                    join var {id: deptId, name: deptName} in deptList
                                                                    on i equals deptId
                                                                    select {id: i, lname: ln, deptName}; 
                                                                    // @output [{id: 1, lname: "George", name:"HR"}, {id: 2, lname: "Fonseka", name:"Operations"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithOrderbyClause1() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    group by id, var ln = lname
                                                    order by id descending
                                                    select {id, lname: ln}; 
                                                    // @output [{id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}, {id: 1, lname: "George"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithOrderbyClause2() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    order by id
                                                    group by var i = id, lname
                                                    select {id: i, lname}; 
                                                    // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithOrderbyClause3() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int id; string lname;|}[] lnames = from var {id, fname, lname} in personList
                                                    order by id, lname
                                                    group by int i = id, string ln = lname
                                                    select {id: i, lname: ln}; 
                                                    // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}, {id: 3, lname: "Fonseka"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithLimitClause() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    record {|int i; string ln;|}[] lnames = from var {id, fname, lname} in personList
                                                group by int i = id, string ln = lname
                                                limit 2
                                                select {i, ln}; 
                                                // @output [{id: 1, lname: "George"}, {id: 2, lname: "Silva"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithTableResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    table<record {|readonly int i; string ln;|}> key(id) t = table key(id) from var {id, fname, lname} in personList
                                                                                        group by var i = id, var ln = lname
                                                                                        select {i, ln};
                                                                                        // @output [{id: 1, lname: "George"}, {id: 3, lname: "Fonseka"}, {id: 2, lname: "Silva"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithMapResult() {
    var personList = [{id: 1, fname: "Alex", lname: "George"}, 
                        {id: 3, fname: "Ranjan", lname: "Fonseka"}, 
                        {id: 3, fname: "Amal", lname: "Fonseka"},
                        {id: 2, fname: "Amali", lname: "Silva"}];

    map<string> m = map from var {id, fname, lname} in personList
                        group by int i = id, string ln = lname
                        order by i
                        select [i.toString(), ln]; // @output [{"1":"George"}, {"2":"Silva"}, {"3":"Fonseka"}]
}

function testGroupbyVarDefsAndSelectWithGroupingKeysWithFromClause() {
    var personList1 = [{id: 1, fname: "Alex"}, {id: 2, fname: "Ranjan"}, {id: 3, fname: "Ranjan"}];
    var personList2 = [{index: 1, lname: "George"}, {index: 2, lname: "Fonseka"}, {index: 3, lname: "Silva"}];
    string[] xx = from var {id, fname} in personList1
                    from var {index, lname} in personList2
                    where id == index
                    group by var fn = fname
                    select fn; // @output ["Alex", "Ranjan"]
}  

// group by <expression>, select <expr with grouping keys>, lhs has the type
function testGroupbyExpressionAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] names = from var {name, price} in input
                        group by name
                        select [price]; // @output [[11, 12], [11]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys2() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][][] names = from var {name, price} in input
                        group by name
                        select [[price]]; // @output [[[11, 11]], [[12]]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][] names = from var {name, price} in input
                        group by name
                        select [(price)]; // @output [[11, 12], [11]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys4() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][][] names = from var {name, price} in input
                        group by name
                        select [([price])]; // @output [[[11, 11]], [[12]]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys5() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][][] names = from var {name, price} in input
                        group by name
                        select [[((price))]]; // @output [[[11, 11]], [[12]]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys6() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    int[][][] names = from var {name, price} in input
                        group by name
                        select ([([price])]); // @output [[[11, 11]], [[12]]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys7() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}];
    record {int[] price;}[] names = from var {name, price} in input
                        group by name
                        select {price: [price]}; // @output [{price: [11, 12]}, {price: [11]}]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys8() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int][] names = from var {name, price1, price2} in input
                        group by name
                        select [[price1], [price2]]; // @output [[11, 12, 23, 24], [13, 25], [14, 26]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys9() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int][] names = from var {name, price1, price2} in input
                        group by name
                        select [[price1], 34]; // @output [[11, 12, 34], [13, 34], [14, 34]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys10() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int][] names = from var {name, price1, price2} in input
                        group by name
                        select [21, [price1]]; // @output [[21, 11, 12], [21, 13], [21, 14]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys11() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int|string][] names = from var {name, price1, price2} in input
                                group by var _ = true
                                select [[name], [price1], [price2]]; 
                                // @output [["Saman", "Saman", "Kamal", "Amal", 11, 12, 13, 14, 23, 24, 25, 26]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys12() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    record {string[] name; int[] price;}[] names = from var {name, price1, price2} in input
                                                    group by var _ = true
                                                    select {name: [name], price: [price2]}; 
                                                    // @output [{name: ["Saman", "Saman", "Kamal", "Amal"], price: [23, 24, 25, 26]}]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys13() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int|string|int[]][] names = from var {name, price1, price2} in input
                                group by var _ = true
                                select [[name], [[price1]], [price2]]; 
                                // @output [["Saman", "Saman", "Kamal", "Amal", [11, 12, 13, 14], 23, 24, 25, 26]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys14() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int|string|int[]][] names = from var {name, price1, price2} in input
                                group by var _ = true
                                select [([name]), [[price1]], [price2]]; 
                                // @output [["Saman", "Saman", "Kamal", "Amal", [11, 12, 13, 14], 23, 24, 25, 26]]
}

function testGroupbyExpressionAndSelectWithNonGroupingKeys15() {
    var input = [{name: "Saman", price1: 11, price2: 23}, 
                    {name: "Saman", price1: 12, price2: 24}, 
                    {name: "Kamal", price1: 13, price2: 25},
                    {name: "Amal", price1: 14, price2: 26}];
    [int|string|int[]][] names = from var {name, price1, price2} in input
                                group by var _ = true
                                select [[name], [[price1]], [(price2)]]; 
                                // @output [["Saman", "Saman", "Kamal", "Amal", [11, 12, 13, 14], 23, 24, 25, 26]]
}
