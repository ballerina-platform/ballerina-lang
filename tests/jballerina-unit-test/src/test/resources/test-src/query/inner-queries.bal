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

import ballerina/lang.'float;
import ballerina/lang.'xml;

type Employee record {|
   int personId;
   int deptId;
|};

type Person record {|
   int id;
   string fname;
   string lname;
|};

type Department record {|
   int id;
   string name;
|};

type DeptPerson record {|
   string fname;
   string lname;
   string? dept;
|};

type DeptPersonValue record {|
    DeptPerson value;
|};

Person[] personList = [
    {id: 1, fname: "Alex", lname: "George"},
    {id: 2, fname: "Ranjan", lname: "Fonseka"},
    {id: 3, fname: "Idris", lname: "Elba"},
    {id: 4, fname: "Dermot", lname: "Crowley"}
];

Department[] deptList = [
    {id: 1, name:"HR"},
    {id: 2, name:"Operations"},
    {id: 3, name:"Engineering"}
];

Employee[] empList = [
    {personId: 1, deptId: 2},
    {personId: 2, deptId: 1},
    {personId: 3, deptId: 3},
    {personId: 4, deptId: 3}
];

function getDeptName(int id) returns string {
    if (id == 1) {
        return "HR";
    } if (id == 2) {
        return "Operations";
    } else {
        return "Engineering";
    }
}

function condition(string name) returns boolean{
    return name == "Alex";
}

function testMultipleJoinClauses() returns boolean {

    DeptPerson[] deptPersonList =
        from var emp in empList
        join Person psn in personList
            on emp.personId equals psn.id
        join Department dept in deptList
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 4;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "Operations";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "HR";
    dp = deptPersonList[2];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "Engineering";
    dp = deptPersonList[3];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "Engineering";
    return testPassed;
}

function testMultipleJoinClausesWithInnerQueries1() returns boolean {

    DeptPerson[] deptPersonList =
        from var emp in (from var e in empList select e)
        join Person psn in (from var p in personList select p)
            on emp.personId equals psn.id
        join Department dept in (from var d in deptList select d)
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 4;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "Operations";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "HR";
    dp = deptPersonList[2];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "Engineering";
    dp = deptPersonList[3];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "Engineering";
    return testPassed;
}

function testMultipleJoinClausesWithInnerQueries2() returns boolean {

    DeptPerson[] deptPersonList =
        from var emp in (stream from var e in empList select e)
        join Person psn in (table key() from var p in personList select p)
            on emp.personId equals psn.id
        join Department dept in (from var d in deptList
                let int deptId = 3
                where d.id == deptId
                where d.name == getDeptName(d.id)
                select d)
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "Engineering";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "Engineering";
    return testPassed;
}

function testMultipleJoinClausesWithInnerQueries3() returns boolean {

    DeptPerson[] deptPersonList = [];

    error? x =
        from var emp in (stream from var e in empList select e)
        join Person psn in (table key() from var p in personList select p)
            on emp.personId equals psn.id
        join Department dept in (from var d in deptList
                let string deptName = "Engineering"
                where d.name == deptName
                select d)
            on emp.deptId equals dept.id
        do {
            DeptPerson dp = {fname : psn.fname, lname : psn.lname, dept : dept.name};
            deptPersonList[deptPersonList.length()] = dp;
        };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "Engineering";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "Engineering";
    return testPassed;
}


function testMultipleJoinClausesWithInnerQueries4() returns boolean {

    DeptPerson[] deptPersonList =
        from var emp in
            (stream from var e in
                (table key() from var e1 in
                    (from var e2 in empList select e2)
                select e1)
             select e)
        join Person psn in (table key() from var p in personList select p)
            on emp.personId equals psn.id
        join Department dept in (from var d in deptList
                let string deptName = "Engineering"
                where d.name == deptName
                select d)
            on emp.deptId equals dept.id
        select {
            fname : psn.fname,
            lname : psn.lname,
            dept : dept.name
        };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Idris" && dp.lname == "Elba" && dp.dept == "Engineering";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Dermot" && dp.lname == "Crowley" && dp.dept == "Engineering";
    return testPassed;
}

function testMultipleJoinClausesWithInnerQueries5() returns boolean {
    xml bookstore = xml `<bookstore>
                        <book category="cooking">
                            <title lang="en">Everyday Italian</title>
                            <author>Giada De Laurentiis</author>
                            <year>2005</year>
                            <price>30.00</price>
                        </book>
                        <book category="children">
                            <title lang="en">Harry Potter</title>
                            <author>J. K. Rowling</author>
                            <year>2005</year>
                            <price>29.99</price>
                        </book>
                        <book category="web">
                            <title lang="en">XQuery Kick Start</title>
                            <author>James McGovern</author>
                            <author>Per Bothner</author>
                            <author>Kurt Cagle</author>
                            <author>James Linn</author>
                            <author>Vaidyanathan Nagarajan</author>
                            <year>2003</year>
                            <price>49.99</price>
                        </book>
                        <book category="web" cover="paperback">
                            <title lang="en">Learning XML</title>
                            <author>Erik T. Ray</author>
                            <year>2003</year>
                            <price>39.95</price>
                        </book>
                    </bookstore>`;

    float total = 0;
    error? res =
        from var price in (from var book in bookstore/<book> select book)/**/<price>
        do {
            var p = (<xml> price)/*;
            if (p is 'xml:Text) {
                var i = 'float:fromString(p.toString());
                if (i is float) {
                    total += i;
                }
            }
        };

    return total == 149.93;
}

function testTypeTestInWhereClause() {
    int?[] v = [1, 2, (), 3];
    (string|int)[] w = [10, 20, "A", 40, "B"];
    int[] result = from var i in v
                   from int j in (from var k in w where k is int select k)
                   where i is int && j > 10
                   where i is 1|2|3
                   select i * j;
    assertEquality(6, result.length());
    assertEquality(20, result[0]);
    assertEquality(40, result[1]);
    assertEquality(40, result[2]);
    assertEquality(80, result[3]);
    assertEquality(60, result[4]);
    assertEquality(120, result[5]);
}

type BddPath record {|
    int[] pos = [1];
|};

public type MappingAlternative record {|
    int[] pos;
|};

function testQueryExpWithinSelectClause1() {
    BddPath[] paths = [{}, {}, {}];

    MappingAlternative[] alts = from var {pos} in paths
        select {
            pos: (from var atom in pos
                select atom)
        };

    MappingAlternative[] expected = [{"pos": [1]}, {"pos": [1]}, {"pos": [1]}];
    assertEquality(expected, alts);
}

function testQueryExpWithinSelectClause2() {
    int[][] data = [[1, 2, 3, 4, 5]];

    (function () returns int[])[] res = from int[] arr in data
         select function() returns int[] {
        int[] evenNumbers = from int i in arr
                 where i % 2 == 0
                 select i;
        return evenNumbers;
    };
    function () returns int[] func = res[0];
    int[] expected = [2, 4];
    assertEquality(expected, func());
}

function testQueryExpWithinQueryAction() returns error? {
    int[][] data = [[2, 3, 4]];
    check from int[] arr in data
        do {
            function () returns int[] func = function() returns int[] {
                int[] evenNumbers = from int i in arr
                    where i % 2 == 0
                    select i;
                return evenNumbers;
            };
            int[] expected = [2, 4];
            assertEquality(expected, func());
        };
}

// todo Should be enabled after fixing https://github.com/ballerina-platform/ballerina-lang/issues/32710
//type A record {|
//    int[] pos;
//|};
//
//function testInnerQuerySymbolVisibility() {
//   A[] res = from var a in 2...4
//        select {
//            pos: (from var b in (from var c in 1...2 where c == a select c)
//                select b)
//        };
//   A[] expected = [{pos: [2]}, {pos: [2]}, {pos: [2]}];
//   assertEquality(expected, res);
//}

type ScoreEvent readonly & record {|
    string email;
    string problemId;
    float score;
|};

type Team readonly & record {|
    string user;
    int teamId;
|};

function testDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction() {
    ScoreEvent[] events = [
        {email: "jake@abc.com", problemId: "12", score: 80.0},
        {email: "anne@abc.com", problemId: "20", score: 95.0},
        {email: "peter@abc.com", problemId: "3", score: 72.0}
    ];

    Team[] team = [
        {user: "jake@abc.com", teamId: 1},
        {user: "anne@abc.com", teamId: 2},
        {user: "peter@abc.com", teamId: 2}
    ];

    json j = from var ev in (from var {email, problemId, score} in events where score > 75.5 select {email, score})
        where ev.score > 85.5
        select {
            email: ev.email
        };
    assertEquality(true, [{email: "anne@abc.com"}] == j);

    j = from var ev in (from var {email, problemId, score} in events where score > 75.5 select {email, score})
        join var {us, ti} in (from var {user: us, teamId: ti} in team select {us, ti})
        on ev.email equals us
        where ev.score > 85.5
        select {
            email: ev.email,
            teamId: ti
        };
    assertEquality(true, [{email: "anne@abc.com", teamId: 2}] == j);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
