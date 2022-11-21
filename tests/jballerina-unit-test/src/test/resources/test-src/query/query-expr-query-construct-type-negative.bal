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

type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type Customer record {
    readonly int id;
    readonly string name;
    int noOfItems;
};

type CustomerTable table<Customer> key(id, name);

type CustomerKeyLessTable table<Customer>;

type PersonValue record {|
    Person value;
|};

function getPersonValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal)
returns PersonValue? {
    var result = returnedVal;
    if (result is PersonValue) {
        return result;
    } else {
        return ();
    }
}

function testIncompatibleTypeWithReturnStream()  {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonStream = stream from var person in personList
        where person.firstName == "John"
        let int newAge = 34
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: newAge
        };
}

function testIncompatibleTypeWithReturnTable() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    Customer[] customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };
}

function testMissingErrorTypeWithReturnTable() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerTable customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };
}

function testOnConflictClauseWithFunction() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        }
        on conflict condition(customer.name);
}

function condition(string name) returns boolean{
    return name == "Anne";
}

type User record {
    readonly int id;
    readonly string firstName;
    string lastName;
    int age;
};

function testInvalidTypeInSelectWithQueryConstructingMap1() {
    table<User> key(id) users = table [];

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 select user;

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 select [user.id, user];

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 let int[2] arr = [user.id, user.age]
                 select arr;

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 let string[] arr = [user.firstName, user.lastName]
                 select arr;
}

function testInvalidTypeInSelectWithQueryConstructingMap2() {
    User[] users = [];

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 select user;

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 select [user.id, user];

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 let int[2] arr = [user.id, user.age]
                 select arr;

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 let string[] arr = [user.firstName, user.lastName]
                 select arr;

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 let string[3] arr = [user.firstName, user.lastName, " "]
                 select arr;

    var _ = map from var user in users
                 where user.age > 21 && user.age < 60
                 select [user.firstName];
}

function testInvalidStaticTypeWithQueryConstructingMap() {
    table<User> key(id) users1 = table [];
    User[] users2 = [];

    map<int>|error a1 = map from var user in users1
                         where user.age > 21 && user.age < 60
                         select [user.firstName, user.lastName];

    map<User> a2 = map from var user in users1
                     where user.age > 21 && user.age < 60
                     select [user.firstName, user.age];

    map<string> a3 = map from var user in users1
                     where user.age > 21 && user.age < 60
                     let string[2] arr = [user.firstName, user.age]
                     select arr;

    map<int>|error a4 = map from var user in users2
                         where user.age > 21 && user.age < 60
                         select [user.firstName, user.lastName];

    map<string> a5 = map from var user in users2
                     where user.age > 21 && user.age < 60
                     select [user.firstName, user];

    map<User> a6 = map from var user in users2
                     where user.age > 21 && user.age < 60
                     let string[2] arr = [user.firstName, user.lastName]
                     select arr;

    map<string>|error a7 = map from var user in users2
                     where user.age > 21 && user.age < 60
                     let string[2]|[string, int] arr = [user.firstName, user.lastName]
                     select arr;
}

function testInvalidTypeInSelectWithQueryConstructingMap3() {
    table<User> key(id) users1 = table [];
    User[] users2 = [];

    var a1 = map from var user in users2
                     where user.age > 21 && user.age < 60
                     let (int[2]|string[2]) & readonly u = [user.id, user.age]
                     select u;

    var a2 = map from var user in users2
                     where user.age > 21 && user.age < 60
                     let string[2]|int[2]|string arr = [user.firstName, user.lastName]
                     select arr;
}

function testInvalidTypeInSelectWithQueryConstructingMap4() {
    var a = map from string|int a in ["a", "b", 1, 2]
                let [string, int]|[string, string] c = a
                select c;
    map<string> _ = a;
}

function testWithReadonlyContextualTypeForQueryConstructingTables() {
    Customer[] customerList1 = [];

    CustomerTable & readonly|error out1 = table key(id, name) from var customer in customerList1
        select {
            id: customer.id,
            name: customer.name
        };

    CustomerTable & readonly|error out2 = table key(id, name) from var customer in customerList1
        select customer on conflict error("Error");
}

type T readonly & record {
    string[] params;
};

type Type1 int[]|string;

function testWithReadonlyContextualTypeForQueryConstructingLists() {
    T _ = { params: from var s in [1, "b", "c", "abc"] select s };

    Type1[] & readonly _ = from var user in [[1, 2], "a", "b", [-1, int:MAX_VALUE]]
                                    select user;

    map<Type1>[] & readonly _ = from var item in [[1, 2], "a", "b", [-1, int:MAX_VALUE]]
                                    select {item: item};

    xml a = xml `<id> 1 </id> <name> John </name>`;

    xml[] & readonly _ = from var user in a
                                 select user;
}

type Department record {
    string dept;
};

type ImmutableMapOfDept map<Department> & readonly;

type ImmutableMapOfInt map<int> & readonly;

type ErrorOrImmutableMapOfInt ImmutableMapOfInt|error;

function testConstructingInvalidReadonlyMap() {
    int[][2] arr = [[1, 2], [3, 4], [9, 10]];
    map<int[2]> & readonly|error mp1 = map from var item in arr
                                        select [item[0].toString(), item];

    ImmutableMapOfDept|error mp3 = map from var item in ["ABC", "DEF", "XY"]
                                        let Department dept = {dept: item}
                                        select [item, dept];

    map<string> & readonly|error mp4 = map from var item in [["1", 1], ["2", 2], ["3", 3], ["4", 4]]
                                        select item on conflict error("Error");

    [string:Char, int[]][] list = [];
    map<float[]>|error mp5 = map from var item in list select item;

    map<int[]> & readonly|error mp7 = map from var item in list select item;
}

type FooBar1 ("foo"|"bar"|int)[2];
type FooBar2 ("foo"|"bar"|1)[2];
type FooBar3 "foo"|"bar"|1;
type FooBar4 "foo"|"bar"|float;
type FooBar5 "foo"|"bar"|1;

function testQueryConstructingMapsWithoutStringSubtypeKeys() {
    FooBar1[] list1 = [];
    map<FooBar1>|error mp1 = map from var item in list1 select item;

    FooBar2[] list2 = [];
    map<FooBar2>|error mp2 = map from var item in list2 select item;

    FooBar3[][2] list3 = [];
    map<FooBar3>|error mp3 = map from var item in list3 select item;

    FooBar4[][2] list4 = [];
    map<FooBar4>|error mp4 = map from var item in list4 select item;

    FooBar5[][2] list5 = [];
    map<FooBar5>|error mp5 = map from var item in list5 select item;

    [FooBar3, int|float][] list6 = [];
    map<int|float>|error mp6 = map from var item in list6 select item;

    [FooBar4, int|float][] list7 = [];
    map<int|float>|error mp7 = map from var item in list7 select item;

    [FooBar5, int|float][] list8 = [];
    map<int|float>|error mp8 = map from var item in list8 select item;
}

function testConstructingInvalidReadonlyMapWithOnConflict() {
    int[][2] & readonly arr = [[1, 2], [3, 4], [9, 10]];
    error? onConflictError = null;
    map<int[2]> & readonly mp1 = map from var item in arr
                                        select [item[0].toString(), item] on conflict error("Duplicate Key!");

    ImmutableMapOfDept mp3 = map from var item in ["ABC", "DEF", "XY"]
                                        let Department dept = {dept: item}
                                        select [item, dept] on conflict null;

    map<string> & readonly mp4 = map from var item in [["1", 1], ["2", 2], ["3", 3], ["4", 4]]
                                        select item on conflict onConflictError;
}

type CustomerTableKeyless table<Customer>;

function testWithReadonlyContextualTypeForQueryConstructingTablesWithOnConflict() {
    Customer[] & readonly customerList1 = [];
    error? onConflictError = null;

    CustomerTable & readonly out1 = table key(id, name) from var customer in customerList1
        select {
            id: customer.id,
            name: customer.name
        } on conflict onConflictError;

    Customer[] customerList2 = [];
    CustomerTable out2 = table key(id, name) from var customer in customerList2
        select customer on conflict null;

    CustomerTableKeyless & readonly out3 = table key() from var customer in customerList1
        select customer on conflict onConflictError;
}

class EvenNumberGenerator {
    int i = 0;
    public isolated function next() returns record {| int value; |}|error {
        self.i += 2;
        if self.i > 20 {
            return error("Greater than 20!");
        }
        return { value: self.i };
    }
}

type ResultValue record {|
    int value;
|};

type NumberRecord record {|
    readonly int id;
    string value;
|};

function testQueryConstructingMapsAndTablesWithClauseMayCompleteSEarlyWithError() {
    EvenNumberGenerator evenGen = new();
    stream<int, error> evenNumberStream = new(evenGen);

    map<int> map1 = map from var item in evenNumberStream
                        select [item.toBalString(), item];

    table<ResultValue> table1 = table key() from var item in evenNumberStream
                                    select {value: item};

    table<NumberRecord> key(id) table2 = table key(id) from var item in evenNumberStream
                                            select {id: item, value: item.toBalString()};
    // Enable following tests after fixing issue - lang/#36746
    // map<int> map2 = map from var firstNo in [1, 4, 9, 10]
    //                         join var secondNo in evenNumberStream
    //                         on firstNo equals secondNo
    //                         select [secondNo.toBalString(), secondNo];

    // table<NumberRecord> key() table3 = table key() from var firstNo in [1, 4, 9, 10]
    //                         join var secondNo in evenNumberStream
    //                         on firstNo equals secondNo
    //                         select {id: secondNo, value: secondNo.toBalString()};

    // table<NumberRecord> key(id) table4 = table key(id) from var firstNo in [1, 4, 9, 10]
    //                         join var secondNo in evenNumberStream
    //                         on firstNo equals secondNo
    //                         select {id: secondNo, value: secondNo.toBalString()};

    map<int> map3 = map from var firstNo in [1, 4, 9, 10]
                            select [firstNo.toBalString(), firstNo] on conflict error("Error");

    table<NumberRecord> key(id) table6 = table key(id) from var firstNo in [1, 4, 9, 10]
                            select {id: firstNo, value: firstNo.toBalString()} on conflict error("Error");
}

function testQueryConstructingMapsAndTablesWithClausesMayCompleteSEarlyWithError2() {
    EvenNumberGenerator evenGen = new();
    stream<int, error> evenNumberStream = new(evenGen);

    map<int> map1 = map from var item in (from var integer in evenNumberStream select integer)
                        select [item.toBalString(), item];

    table<ResultValue> table1 = table key() from var item in (from var integer in evenNumberStream select integer)
                                    select {value: item};

    table<NumberRecord> key(id) table2 = table key(id) from var item in (from var integer in evenNumberStream select integer)
                                            select {id: item, value: item.toBalString()};

    map<int>|error map2 = map from var item in (map from var firstNo in [1, 4, 4, 10]
                            select [firstNo.toBalString(), firstNo] on conflict error("Error"))
                        select [item.toBalString(), item];

    table<record {| readonly int id; string value; |}>|error table3 = table key() from var item in (table key(id) from var firstNo in [1, 4, 4, 10]
                            select {id: firstNo, value: firstNo.toBalString()} on conflict error("Error"))
                                    select item;
}

public type FooError distinct error;

public type BarError distinct error;

function testIncompatibleAssignments() returns error? {
    stream<int, FooError?> _ = stream from int i in 1 ... 3
        select check getDistinctErrorOrInt();

    stream<int, FooError?> _ = stream from int i in 1 ... 3
        let int _ = check getDistinctErrorOrInt()
        select i;
}

function testCheckExprWithoutEnclEnvErrorReturn()  {
    stream<int, BarError?> _ = stream from int i in 1 ... 3
        select check getDistinctErrorOrInt();
}

function getDistinctErrorOrInt() returns int|BarError {
    return error BarError("Distinct error thrown");
}

function testInvalidTableCtrAssignment() {
    int _ = table []; // error
    int|float _ = table []; // error
    string _ = table [{a: 1, b: 2}]; // error
}
