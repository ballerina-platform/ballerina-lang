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

type DeptPerson record {|
   string fname;
   string lname;
   string? dept;
|};

type Department record {|
   int id;
   string name;
|};

type Person record {|
   int id;
   string fname;
   string lname;
|};

type DeptPersonValue record {|
    DeptPerson value;
|};

function getDeptPersonValue((record {| DeptPerson value; |}|error?)|(record {| DeptPerson value; |}?) returnedVal)
returns DeptPersonValue? {
    var result = returnedVal;
    if (result is DeptPersonValue) {
        return result;
    } else {
        return ();
    }
}

function testSimpleJoinClauseWithRecordVariable() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Department dept in deptList
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    return testPassed;
}

function testSimpleJoinClauseWithRecordVariable2() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join var {id: deptId, name: deptName} in deptList
       on person.id equals deptId
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    return testPassed;
}

function testSimpleJoinClauseWithRecordVariable3() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join var {id, name} in deptList
       on person.id equals id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    return testPassed;
}

function testJoinClauseWithStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    stream<Person> personStream = personList.toStream();
    stream<Department> deptStream = deptList.toStream();

    stream<DeptPerson> deptPersonStream =
       stream from var person in personStream
       join var dept in deptStream
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    record {| DeptPerson value; |}? deptPerson = getDeptPersonValue(deptPersonStream.next());
    testPassed = testPassed && deptPerson?.value?.fname == "Alex" && deptPerson?.value?.lname == "George"
                             && deptPerson?.value?.dept == "HR";

    return testPassed;
}

function testJoinClauseWithLimit() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Department dept in deptList
       on person.id equals dept.id
       limit 1
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 1;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    return testPassed;
}

function testOuterJoinClauseWithRecordVariable() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};
    Person p3 = {id: 3, fname: "Grainier", lname: "Perera"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var dept in deptList
       on person.id equals dept?.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 3;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    dp = deptPersonList[2];
    testPassed = testPassed && dp.fname == "Grainier" && dp.lname == "Perera" && dp.dept is ();
    return testPassed;
}

function testOuterJoinClauseWithRecordVariable2() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};
    Person p3 = {id: 3, fname: "Grainier", lname: "Perera"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var {id: deptId, name: deptName} in deptList
       on person.id equals deptId
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 3;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    dp = deptPersonList[2];
    testPassed = testPassed && dp.fname == "Grainier" && dp.lname == "Perera" && dp.dept is ();
    return testPassed;
}

function testOuterJoinClauseWithRecordVariable3() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};
    Person p3 = {id: 3, fname: "Grainier", lname: "Perera"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var {id,name} in deptList
       on person.id equals id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 3;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    dp = deptPersonList[2];
    testPassed = testPassed && dp.fname == "Grainier" && dp.lname == "Perera" && dp.dept is ();
    return testPassed;
}

function testOuterJoinClauseWithStream() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    stream<Person> personStream = personList.toStream();
    stream<Department> deptStream = deptList.toStream();

    stream<DeptPerson> deptPersonStream =
       stream from var person in personStream
       outer join var dept in deptStream
       on person.id equals dept?.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };

    boolean testPassed = true;
    record {| DeptPerson value; |}? deptPerson = getDeptPersonValue(deptPersonStream.next());
    testPassed = testPassed && deptPerson?.value?.fname == "Alex" && deptPerson?.value?.lname == "George"
                             && deptPerson?.value?.dept == "HR";
    return testPassed;
}

function testOuterJoinClauseWithLimit() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var dept in deptList
       on person.id equals dept?.id
       limit 2
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept?.name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "Operations";
    return testPassed;
}

function testSimpleJoinClauseWithLetAndEquals() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on deptName equals name
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "HR";
    return testPassed;
}

function testSimpleJoinClauseWithFunctionInAnEquals() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on deptName equals getDeptName(id)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    boolean testPassed = true;
    DeptPerson dp;
    any res = deptPersonList;
    testPassed = testPassed && res is DeptPerson[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && deptPersonList.length() == 2;
    dp = deptPersonList[0];
    testPassed = testPassed && dp.fname == "Alex" && dp.lname == "George" && dp.dept == "HR";
    dp = deptPersonList[1];
    testPassed = testPassed && dp.fname == "Ranjan" && dp.lname == "Fonseka" && dp.dept == "HR";
    return testPassed;
}

function getDeptName(int id) returns string {
    if (id == 1) {
        return "HR";
    } else {
        return "Operations";
    }
}

type User record {|
    readonly int id;
    string name;
|};

type Login record {|
    int userId;
    string time;
|};

public function testOuterJoin() {
    table<User> key(id) users = table [
            {id: 1234, name: "Keith"},
            {id: 6789, name: "Anne"}
        ];

    Login[] logins = [
        {userId: 6789, time: "20:10:23"},
        {userId: 1234, time: "10:30:02"},
        {userId: 3987, time: "12:05:00"}
    ];

    string?[] selected = from var login in logins
        outer join var user in users on login.userId equals user?.id
        select (user?.name);

    int?[] ids = [];

    error? e = from Login login in logins
                outer join var user in users on login.userId equals user?.id
                do {
                    ids.push(user?.id);
                };

    string?[] ordered_names = [];
    error? e2 = from Login login in logins
            outer join var user in users on login.userId equals user?.id
            order by user?.id
            do {
                ordered_names.push(user?.name);
            };

    assertEquality("Anne", selected[0]);
    assertEquality("Keith", selected[1]);
    assertEquality(null, selected[2]);
    assertEquality(6789, ids[0]);
    assertEquality(1234, ids[1]);
    assertEquality(null, ids[2]);
    assertEquality("Keith", ordered_names[0]);
    assertEquality("Anne", ordered_names[1]);
    assertEquality(null, ordered_names[2]);
}

public function testJoinClauseWithLargeList() {
    assertEquality(0, getCommonList("t"));
    assertEquality(10000, getCommonList("a"));
}

function getCommonList(string character) returns int {
    string[] barList = ["a", "b", "c"];
    string[] fooList = [];
    foreach int i in 1 ... 10000 {
        fooList.push(character);
    }

    string[] commonList = from string a in fooList
            join string b in barList on a equals b
            select a;
    return commonList.length();
}

type NilableKeyRow record {|
    int id;
    string? key;
|};

type AnydataKeyRow record {|
    int id;
    anydata key;
|};

function innerJoinNilableKeys(NilableKeyRow[] left, NilableKeyRow[] right) returns int[][] {
    return from NilableKeyRow l in left
            join NilableKeyRow r in right on l.key equals r.key
            select [l.id, r.id];
}

function outerJoinNilableKeys(NilableKeyRow[] left, NilableKeyRow[] right) returns (int?)[][] {
    return from NilableKeyRow l in left
            outer join var r in right on l.key equals r?.key
            select [l.id, r?.id];
}

function innerJoinAnydataKeys(anydata leftKey, anydata rightKey) returns int[][] {
    AnydataKeyRow[] left = [{id: 1, key: leftKey}];
    AnydataKeyRow[] right = [{id: 2, key: rightKey}];
    return from AnydataKeyRow l in left
            join AnydataKeyRow r in right on l.key equals r.key
            select [l.id, r.id];
}

function outerJoinAnydataKeys(anydata leftKey, anydata rightKey) returns (int?)[][] {
    AnydataKeyRow[] left = [{id: 1, key: leftKey}];
    AnydataKeyRow[] right = [{id: 2, key: rightKey}];
    return from AnydataKeyRow l in left
            outer join var r in right on l.key equals r?.key
            select [l.id, r?.id];
}

function assertJoinResult((int?)[][] expected, (int?)[][] actual) {
    assertEquality(expected, actual);
}

public function testJoinClauseWithNilKeys() {
    assertJoinResult([], innerJoinNilableKeys([{id: 1, key: ()}], [{id: 2, key: "a"}]));
    assertJoinResult([], innerJoinNilableKeys([{id: 1, key: "a"}], [{id: 2, key: ()}]));
    assertJoinResult([[1, 2]], innerJoinNilableKeys([{id: 1, key: ()}], [{id: 2, key: ()}]));
    // `().toString()` is "", but nil must not match an empty string.
    assertJoinResult([], innerJoinNilableKeys([{id: 1, key: ()}], [{id: 2, key: ""}]));
    assertJoinResult([], innerJoinNilableKeys([{id: 1, key: ""}], [{id: 2, key: ()}]));

    NilableKeyRow[] left = [{id: 1, key: ()}, {id: 3, key: "b"}, {id: 5, key: ()}];
    NilableKeyRow[] right = [{id: 2, key: ()}, {id: 4, key: "b"}, {id: 6, key: ()}];
    assertJoinResult([[1, 2], [1, 6], [3, 4], [5, 2], [5, 6]], innerJoinNilableKeys(left, right));
}

public function testOuterJoinClauseWithNilKeys() {
    assertJoinResult([[1, ()]], outerJoinNilableKeys([{id: 1, key: ()}], [{id: 2, key: "a"}]));
    assertJoinResult([[1, ()]], outerJoinNilableKeys([{id: 1, key: "a"}], [{id: 2, key: ()}]));
    assertJoinResult([[1, 2]], outerJoinNilableKeys([{id: 1, key: ()}], [{id: 2, key: ()}]));

    NilableKeyRow[] left = [{id: 1, key: ()}, {id: 3, key: "b"}, {id: 5, key: "c"}];
    NilableKeyRow[] right = [{id: 2, key: ()}, {id: 4, key: "b"}, {id: 6, key: ()}];
    assertJoinResult([[1, 2], [1, 6], [3, 4], [5, ()]], outerJoinNilableKeys(left, right));
}

public function testJoinClauseWithNilKeysInStream() {
    NilableKeyRow[] left = [{id: 1, key: ()}, {id: 3, key: "b"}, {id: 5, key: "c"}];
    NilableKeyRow[] right = [{id: 2, key: ()}, {id: 4, key: "b"}];

    stream<int[], error?> innerStream = stream from NilableKeyRow l in left
            join NilableKeyRow r in right on l.key equals r.key
            select [l.id, r.id];
    int[][] innerResult = checkpanic from int[] pair in innerStream select pair;
    assertJoinResult([[1, 2], [3, 4]], innerResult);

    stream<(int?)[], error?> outerStream = stream from NilableKeyRow l in left
            outer join var r in right on l.key equals r?.key
            select [l.id, r?.id];
    (int?)[][] outerResult = checkpanic from (int?)[] pair in outerStream select pair;
    assertJoinResult([[1, 2], [3, 4], [5, ()]], outerResult);
}

public function testJoinClauseKeysUseDeepEquality() {
    // Keys that have the same string form, but are not equal.
    assertJoinResult([], innerJoinAnydataKeys(1, "1"));
    assertJoinResult([], innerJoinAnydataKeys(true, "true"));
    assertJoinResult([], innerJoinAnydataKeys(1.5, 1.5d));
    assertJoinResult([], innerJoinAnydataKeys(1, 1.0));
    assertJoinResult([], innerJoinAnydataKeys((), "()"));
    assertJoinResult([], innerJoinAnydataKeys({id: ()}, {id: "null"}));

    // Keys that have different string forms, but are equal.
    assertJoinResult([[1, 2]], innerJoinAnydataKeys(1.0d, 1.00d));
    assertJoinResult([[1, 2]], innerJoinAnydataKeys(-0.0, 0.0));
    assertJoinResult([[1, 2]], innerJoinAnydataKeys(float:NaN, float:NaN));
    assertJoinResult([[1, 2]], innerJoinAnydataKeys({a: 1, b: 2}, {b: 2, a: 1}));

    // Structured keys with nil members.
    assertJoinResult([[1, 2]], innerJoinAnydataKeys([1, ()], [1, ()]));
    assertJoinResult([[1, 2]], innerJoinAnydataKeys({id: ()}, {id: ()}));

    // Deeply nested keys.
    assertJoinResult([[1, 2]], innerJoinAnydataKeys([[[[1, ()]]]], [[[[1, ()]]]]));
    assertJoinResult([], innerJoinAnydataKeys([[[[1]]]], [[[[2]]]]));

    // Cyclic keys.
    anydata[] cyclicLeft = [1];
    cyclicLeft.push(cyclicLeft);
    anydata[] cyclicRight = [1];
    cyclicRight.push(cyclicRight);
    assertJoinResult([[1, 2]], innerJoinAnydataKeys(cyclicLeft, cyclicRight));
}

public function testOuterJoinClauseKeysUseDeepEquality() {
    assertJoinResult([[1, ()]], outerJoinAnydataKeys(1, "1"));
    assertJoinResult([[1, ()]], outerJoinAnydataKeys((), ""));
    assertJoinResult([[1, 2]], outerJoinAnydataKeys(1.0d, 1.00d));
    assertJoinResult([[1, 2]], outerJoinAnydataKeys(-0.0, 0.0));
    assertJoinResult([[1, 2]], outerJoinAnydataKeys({a: 1, b: 2}, {b: 2, a: 1}));
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
