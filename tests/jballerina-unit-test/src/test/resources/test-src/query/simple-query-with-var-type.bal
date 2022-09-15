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

type Teacher record {|
    string firstName;
    string lastName;
    int age;
    string teacherId;
|};

type EmployeeEntity record {
    int id;
    string fname;
    string lname;
    int age;
};

type Employee record {|
    string fname;
    string lname;
    int age;
|};

class NumberGenerator {
    int i = 0;
    public isolated function next() returns record {|int value;|}|error? {
        //closes the stream after 5 events
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }
}

type ResultValue record {|
    Person value;
|};

function getRecordValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal) returns Person? {
   if (returnedVal is ResultValue) {
      return returnedVal.value;
   } else {
      return ();
   }
}

function testSimpleSelectQueryWithSimpleVariable() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithRecordVariable() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, age: a } in personList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   age: a
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
            from var { firstName, lastName, age } in personList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   age: age
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV3() returns Person[] {
    Teacher p1 = {firstName: "Alex", lastName: "George", age: 23, teacherId: "XYZ01"};
    Teacher p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30, teacherId: "ABC01"};
    Teacher p3 = {firstName: "John", lastName: "David", age: 33, teacherId: "ABC10"};

    Teacher[] teacherList = [p1, p2, p3];

    var outputPersonList =
            from var { firstName, lastName, age, teacherId} in teacherList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   age: age
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithWhereClause() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
            from var person in personList
            where person.age >= 30
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };
    return outputPersonList;
}

function testQueryExpressionForPrimitiveType() returns boolean {

    int[] intList = [12, 13, 14, 15, 20, 21, 25];

    var outputIntList =
            from var value in intList
            where value > 20
            select value;

    return outputIntList == [21, 25];
}

function testQueryExpressionWithSelectExpression() returns boolean {

    int[] intList = [1, 2, 3];

    var stringOutput =
            from var value in intList
            select value.toString();

    return stringOutput == ["1", "2", "3"];
}

function testFilteringNullElements() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Person?[] personList = [p1, (), p2];

    var outputPersonList =
                     from var person in personList
                     where (person is Person)
                     select {
                         firstName: person.firstName,
                         lastName: person.lastName,
                         age: person.age
                         };
    return outputPersonList;
}

function testJSONArrayWithArity() returns boolean {
    json[] jdata = [{name: "bob", age: 10}, {name: "tom", age: 16}];
    var val = from var v in jdata
                   select <string> checkpanic v.name;
    return val == ["bob", "tom"];
}

function testArrayWithTuple() returns boolean {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];
    var val = from var [i, v] in arr
                   where i == 3
                   select v;
    return val == ["C"];
}

function testQueryExpressionWithVarType() returns Teacher[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age,
                   teacherId: "TER1200"
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithSpreadOperator() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            select {
                   ...person
            };

    return outputPersonList;
}

function testQueryExpressionWithSpreadOperatorV2() returns Teacher[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonList =
            from var person in personList
            select {
                   ...person,
                   teacherId: "TER1200"
            };

    return outputPersonList;
}

public function testQueryWithStream() returns boolean {
    NumberGenerator numGen = new;
    var numberStream = new stream<int, error?>(numGen);

    var oddNumberList = from var num in numberStream
                        where (num % 2 == 1)
                        select num;
    int[] result = [];
    record {| int value; |}|error? v = oddNumberList.next();
    while (v is record {| int value; |}) {
        result.push(v.value);
        v = oddNumberList.next();
    }
    return result == [1, 3, 5];
}

function testSimpleSelectQueryReturnStream() returns boolean {
    boolean testPassed = true;
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputPersonStream = stream from var person in personList
                             select {
                                 firstName: person.firstName,
                                 lastName: person.lastName,
                                 age: person.age
                             };
    Person? returnedVal = getRecordValue(outputPersonStream.next());
    testPassed = testPassed && (returnedVal is Person) && (returnedVal == p1);

    returnedVal = getRecordValue(outputPersonStream.next());
    testPassed = testPassed && (returnedVal is Person) && (returnedVal == p2);

    returnedVal = getRecordValue(outputPersonStream.next());
    testPassed = testPassed && (returnedVal is Person) && (returnedVal == p3);
    return testPassed;
}

string fname = "";

function testVariableShadowingWithQueryExpressions1() returns boolean {

    EmployeeEntity[] entities = [
            {id: 1232, fname: "Sameera", lname: "Jayasoma", age: 30},
            {id: 1232, fname: "Asanthi", lname: "Kulasinghe", age: 30},
            {id: 1232, fname: "Khiana", lname: "Jayasoma", age: 2}
        ];

    Employee[] records = from var {fname, lname, age} in entities select {fname, lname, age};
    boolean testPassed = true;
    Employee e = records[0];
    testPassed = testPassed && e.fname == "Sameera" && e.lname == "Jayasoma" && e.age == 30;
    e = records[1];
    testPassed = testPassed && e.fname == "Asanthi" && e.lname == "Kulasinghe" && e.age == 30;
    e = records[2];
    testPassed = testPassed && e.fname == "Khiana" && e.lname == "Jayasoma" && e.age == 2;
    return testPassed;
}

function testVariableShadowingWithQueryExpressions2() returns boolean {

    EmployeeEntity[] entities = [
            {id: 1232, fname: "Sameera", lname: "Jayasoma", age: 30},
            {id: 1232, fname: "Asanthi", lname: "Kulasinghe", age: 30},
            {id: 1232, fname: "Khiana", lname: "Jayasoma", age: 2}
        ];

    Employee[] records = from var {fname, lname, age} in entities select {fname, lname, age};
    var lname = 5;
    boolean testPassed = true;
    Employee e = records[0];
    testPassed = testPassed && e.fname == "Sameera" && e.lname == "Jayasoma" && e.age == 30;
    e = records[1];
    testPassed = testPassed && e.fname == "Asanthi" && e.lname == "Kulasinghe" && e.age == 30;
    e = records[2];
    testPassed = testPassed && e.fname == "Khiana" && e.lname == "Jayasoma" && e.age == 2;
    testPassed = testPassed && lname == 5;
    return testPassed;
}

type Student record {
    readonly string name;
    int id;
};

function testSimpleSelectQueryWithTable() {
    table<Student> key(name) t = table [
        {name: "Amy", id: 1234},
        {name: "John", id: 4567}
    ];
    var ids = from var {id} in t select {id};
    table<record {|int id;|}> t2 = ids;
    assertEquality(2, ids.length());
    assertEquality((table [{"id":1234},{"id":4567}]).toString(), t2.toString());
}

type User record {
    readonly int id;
    string firstName;
    string lastName;
    int age;
};

function testQueryConstructingTableWithVar() returns error? {
    User u1 = {id: 1, firstName: "John", lastName: "Doe", age: 25};
    User u2 = {id: 2, firstName: "Anne", lastName: "Frank", age: 30};

    table<User> key(id) users = table [];
    users.add(u1);
    users.add(u2);

    var result1 = check table key(user) from var user in users
                  where user.age > 21 && user.age < 60
                  select {user};

    assertEquality(true, result1 is table<record {| User user; |}> key(user));
    assertEquality({"user": u1}, result1.get(u1));

    User[] userList = [u1, u2];

    var result2 = check table key(user) from var user in userList
                  where user.age > 21 && user.age < 60
                  select {user};

    assertEquality(true, result2 is table<record {| User user; |}> key(user));
    assertEquality({"user": u1}, result2.get(u1));
}

type ScoreEvent readonly & record {|
    string email;
    string problemId;
    float score;
|};

type Team readonly & record {|
    string user;
    int teamId;
|};

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

function testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInFromClause() {
    json j = from var {email, problemId, score} in events
        where score > 85.5
        select {email, score};
    assertEquality([{email: "anne@abc.com", score: 95.0}], j);

    j = from var {email: em, problemId: pi, score: sc} in events
        where sc < 80.0
        select {em, sc};
    assertEquality([{em : "peter@abc.com", sc: 72.0}], j);
}

function testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInJoinClause() {
    json j = from var {email, problemId, score} in events
        join var {user, teamId} in team
        on email equals user
        where teamId == 2 && score > 85.5
        select {email, score};
    assertEquality([{email: "anne@abc.com", score: 95.0}], j);

    j = from var {email: em, problemId: pi, score: sc} in events
        join var {user: us, teamId: ti} in team
        on em equals us
        where sc < 80.0
        select {ti, sc};
    assertEquality([{ti: 2, sc: 72.0}], j);
}

function testUsingAnIntersectionTypeInQueryExpr() {
    json j = from var ev in events
        where ev.score < 80.0
        select {
            email: ev.email,
            score: ev.score
        };
    assertEquality([{email : "peter@abc.com", score: 72.0}], j);

    j = from var ev in events
        join var tm in team
        on ev.email equals tm.user
        where ev.score < 80.0
        select {
            email: ev.email,
            teamId: tm.teamId
        };
    assertEquality([{email : "peter@abc.com", teamId: 2}], j);
}

type ScoreEventType ScoreEvent;

type TeamType Team;

ScoreEventType[] events2 = events;

TeamType[] team2 = team;

function testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInFromClause2() {
    json j = from var {email, problemId, score} in events2
        where score > 85.5
        select {email, score};
    assertEquality([{email: "anne@abc.com", score: 95.0}], j);

    j = from var {email: em, problemId: pi, score: sc} in events2
        where sc < 80.0
        select {em, sc};
    assertEquality([{em : "peter@abc.com", sc: 72.0}], j);
}

function testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInJoinClause2() {
    json j = from var {email, problemId, score} in events2
        join var {user, teamId} in team2
        on email equals user
        where teamId == 2 && score > 85.5
        select {email, score};
    assertEquality([{email: "anne@abc.com", score: 95.0}], j);

    j = from var {email: em, problemId: pi, score: sc} in events2
        join var {user: us, teamId: ti} in team2
        on em equals us
        where sc < 80.0
        select {ti, sc};
    assertEquality([{ti: 2, sc: 72.0}], j);
}

function testUsingAnIntersectionTypeInQueryExpr2() {
    json j = from var ev in events2
        where ev.score < 80.0
        select {
            email: ev.email,
            score: ev.score
        };
    assertEquality([{email : "peter@abc.com", score: 72.0}], j);

    j = from var ev in events2
        join var tm in team2
        on ev.email equals tm.user
        where ev.score < 80.0
        select {
            email: ev.email,
            teamId: tm.teamId
        };
    assertEquality([{email : "peter@abc.com", teamId: 2}], j);
}

function testMethodCallExprsOnQueryExprs() {
    string[] words = ["a", "b", "c"];
    var len = (from string str in words
        select str).length();
    assertEquality(3, len);

    len = (from string str in words
        where str == "a"
        select str).length();
    assertEquality(1, len);

    string word = (from string str in words
        select str).pop();
    assertEquality("c", word);

    int? index = (from string str in words
        select str).indexOf("b");
    assertEquality(1, index);

    boolean isReadonly = (from string str in words
        select str).isReadOnly();
    assertEquality(false, isReadonly);

    word = (from string str in words
        select str).remove(0);
    assertEquality("a", word);

    words = (from string str in words
        select str).filter(w => w is string);
    assertEquality(["a", "b", "c"], words);

    words = (from string str in words
        select str).reverse();
    assertEquality(["c", "b", "a"], words);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
