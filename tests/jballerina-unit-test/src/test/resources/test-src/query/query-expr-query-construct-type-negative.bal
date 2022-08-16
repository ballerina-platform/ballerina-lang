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
                     select [user.firstName, user];

    map<string> a3 = map from var user in users1
                     where user.age > 21 && user.age < 60
                     let string[2] arr = [user.firstName, user.lastName]
                     select arr;

    map<int>|error a4 = map from var user in users2
                         where user.age > 21 && user.age < 60
                         select [user.firstName, user.lastName];

    map<User> a5 = map from var user in users2
                     where user.age > 21 && user.age < 60
                     select [user.firstName, user];

    map<string> a6 = map from var user in users2
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
