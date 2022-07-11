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

import ballerina/lang.runtime;

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type FullName record {|
   string firstName;
   string lastName;
|};

type Department record {|
   string name;
|};

type Employee record {|
   string firstName;
   string lastName;
   string deptAccess;
|};


function testSimpleQueryAction() returns FullName[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x =  from var person in personList
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            };

    return nameList;
}

function testSimpleQueryAction2() returns int{

    int[] intList = [1, 2, 3];
    int count = 0;

    error? x = from var value in intList
            do {
                count += value;
            };

    return count;
}

function testSimpleQueryActionWithRecordVariable() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var { firstName: nm1, lastName: nm2, age: a } in personList
            do {
                FullName fullName = {firstName: nm1, lastName: nm2};
                nameList[nameList.length()] = fullName;
            };

    return  nameList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var { firstName, lastName, age } in personList
            do {
                FullName fullName = {firstName: firstName, lastName: lastName};
                nameList[nameList.length()] = fullName;
            };

    return  nameList;
}

function testSimpleSelectQueryWithLetClause() returns  FullName[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var person in personList
            let int twiceAge  = (person.age * 2)
            do {
                if(twiceAge < 50) {
                    FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                    nameList[nameList.length()] = fullName;
                }

            };
    return  nameList;
}

function testSimpleSelectQueryWithWhereClause() returns  FullName[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var person in personList
            where (person.age * 2) < 50
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            };
    return  nameList;
}

function testSimpleSelectQueryWithMultipleFromClauses() returns  Employee[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];
    Employee[] employeeList = [];

    error? x = from var person in personList
            from var dept in deptList
            let string hrDepartment = "Human Resource"
            do {
                if(dept.name == "HR") {
                    Employee employee = {firstName: person.firstName, lastName: person.lastName, deptAccess: hrDepartment};
                    employeeList[employeeList.length()] = employee;
                }
            };
    return  employeeList;
}

function testQueryExpressionIteratingOverXMLInFromInQueryAction() returns float {
    xml<xml:Element> bookstore = xml `<bookstore>
                                          <book category="cooking">
                                              <title lang="en">Everyday Italian</title>
                                              <price>30.00</price>
                                          </book>
                                          <book category="children">
                                              <title lang="en">Harry Potter</title>
                                              <price>29.99</price>
                                          </book>
                                          <book category="web">
                                              <title lang="en">XQuery Kick Start</title>
                                              <price>49.99</price>
                                          </book>
                                          <book category="web" cover="paperback">
                                              <title lang="en">Learning XML</title>
                                              <price>39.95</price>
                                          </book>
                                      </bookstore>`;

    float total = 0;
    error? res = from xml:Element price in bookstore/<book>/**/<price>
              do {
                  var p = price/*;
                  if (p is xml:Text) {
                      var i = float:fromString(p.toString());
                      if (i is float) {
                          total += i;
                      }
                  }
              };
    return total;
}

type Record record {|
    int? i;
    string? j;
|};

function testTypeTestInWhereClause() {
    int?[] v = [1, 2, (), 3];
    int total = 0;
    error? result = from var i in v
                    where i is int
                    do {
                        total += i;
                    };
    assertEquality((), result);
    assertEquality(6, total);

    float[] u = [10.5, 20.5, 30.5];
    float x = 0;
    result = from var i in v
             from float j in u
             where i is int
             do {
                 x += <float>i * j;
             };
    assertEquality((), result);
    assertEquality(369.0, x);

    (string|int)[] w = [10, 20, "A", 40, "B"];
    total = 0;
    result = from var i in v
             from int j in (from var k in w where k is int select k)
             where i is int && j > 10
             do {
                 total += i * j;
             };
    assertEquality((), result);
    assertEquality(360, total);

    total = 0;
    result = from var i in v
             where i is int
             where i is 1|2|3
             do {
                 total += i;
             };
    assertEquality((), result);
    assertEquality(6, total);

    Record r1 = {i: 1, j: ()};
    Record r2 = {i: 1, j: "A"};
    Record r3 = {i: 1, j: "C"};

    Record[] recordList = [r1, r2, r3];
    (string|float)[] y = ["X", 30.5, 40.5, "Y", 10.5, "Z", 20.5];
    x = 0;

    result = from var {i, j} in recordList
             from float k in (from var m in y where m is float select m)
             where i is int
             where j is string
             do {
                 x += <float>i * k;
             };
    assertEquality((), result);
    assertEquality(204.0, x);
}

function testTypeNarrowingVarDefinedWithLet() {
    map<error> errorMap = {};
    error? res1 = from int i in 1 ... 2
        let error|string errorOrStr = getErrorOrString()
        where errorOrStr is error
        do {
            errorMap["1"] = errorOrStr;
        };
    error? er1 = errorMap["1"];
    assertEquality((), res1);
    assertEquality(er1 is error, true);

    error? res2 = from int i in 1 ... 2
            let error|string errorOrStr = getErrorOrString()
            do {
                if (errorOrStr is error) {
                  errorMap["2"] = errorOrStr;
                }
            };
    assertEquality((), res2);
    error? er2 = errorMap["2"];
    assertEquality(er2 is error, true);
}

function getErrorOrString() returns error|string {
    return error("Dummy error");
}

function testWildcardBindingPatternInQueryAction1() {
    int[] x = [1, 2, 3];

    int m = 0;

    error? res = from int _ in x
        do {
            m += 1;
        };

    res = from var _ in x
        do {
            m += 1;
        };

    assertEquality(6, m);
    assertEquality(true, res is ());
}

function testWildcardBindingPatternInQueryAction2() {
    map<boolean> x = {
        a: true,
        b: false
    };

    int m = 0;

    error? res = from boolean _ in x
        do {
            m += 1;
        };

    res = from var _ in x
        do {
            m += 1;
        };

    assertEquality(4, m);
    assertEquality(true, res is ());
}

function testQueryActionWithAsyncCalls() returns error? {
    int sum = 0;
    check from var x in [1, 2, 3]
    do {
         runtime:sleep(0.1);
         sum = sum + x;
    };
    assertEquality(sum, 6);
}

class IterableWithError {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|int value;|}|error?;
    } {
        return object {
            public isolated function next() returns record {|int value;|}|error? {
                return error("Custom error thrown.");
            }
        };
    }
}

function testErrorHandlingWithinQueryAction() {
    error? res1 = from int v in 1 ... 2
        do {
            _ = check getErrorOrString();
        };
    assertTrue(res1 is error);

    IterableWithError itr = new IterableWithError();
    (int|error)[] arr = [];
    error? res2 = from var item in itr
        do {
            arr.push(item);
        };
    assertTrue(res2 is error);

    error? res3 = from var i in 1 ... 2
        from var item in itr
        do {
            arr.push(item);
        };
    assertTrue(res3 is error);

    assertTrue(throwErrorFromQueryAction() is error);

    error? res4 = ();
    do {
        check from int i in 1 ... 2
            do {
                _ = check getErrorOrString();
            };
    } on fail var e {
        res4 = e;
    }
    assertTrue(res4 is error);

    assertTrue(failFromQueryAction() is error);
}

function throwErrorFromQueryAction() returns error? {
    check from int v in 1 ... 2
        do {
            _ = check getErrorOrString();
        };
}

function failFromQueryAction() returns error? {
    //when failed; error returned to invocation not to the result assignment
    error? res = from int v in 1 ... 2
        do {
            fail error("Custom Error");
        };
}

function testReturnStmtWithinQueryAction() {
    assertEquality("Dummy string", returnString());
    assertEquality("Dummy string", returnStringOrError());
    assertEquality("World", testReachabilityWithQueryAction());
//    should enable when issues/35383 is fixed
//    assertEquality((), testNilReturnWithinQueryAction());
}

function returnString() returns string {
    error? res = from int i in 1...3
       do {
         return "Dummy string";
       };
    return "Should not reach here";
}

function returnStringOrError() returns string|error {
    check from int i in 1 ... 3
        do {
            if (3 + 2) == 5 {
                return "Dummy string";
            }
        };
    //checking return statement breaks the loop
    panic error("Return statement should brake the loop and return");
}

function testReachabilityWithQueryAction() returns string {
    string?[] stringArray = [(), (), ()];

    error? unionResult = from var item in stringArray
        where item is string
        do {
            if 5 + 5 == 10 { //to avoid unreachable error at final return
                return "Hello";
            }
        };
    return "World";
}

//function testNilReturnWithinQueryAction() returns string? {
//    int count = 0;
//    error? res = from int i in 1 ... 3
//        do {
//            count = count + 1;
//            if 5 + 5 == 10 { //to avoid unreachable error
//                return;
//            }
//        };
//    //checking return statement breaks the loop
//    panic error("Return statement should brake the loop and return");
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

function testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction() returns error? {
    float sum = 0.0;

    check from var {email, problemId, score} in events
        join var {user, teamId} in team
        on email equals user
        where teamId == 2
        do {
            sum += score;
        };
    assertEquality(167.0, sum);
}

function testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction2() returns error? {
    float sum = 0.0;

    check from var ev in (from var {email, problemId, score} in events where score > 75.5 select {email, score})
        join var {us, ti} in (from var {user: us, teamId: ti} in team select {us, ti})
        on ev.email equals us
        where ti == 2
        do {
            sum += ev.score;
        };
    assertEquality(95.0, sum);
}

function assertTrue (any|error actual) {
    return assertEquality(true, actual);
}

function testQueryExpWithinQueryAction() returns error? {
    int[][] data = [[1, 2], [2, 3, 4]];
    int sumOfEven = 0;
    check from int[] arr in data
        do {
            int[] evenNumbers = from int i in arr
                where i % 2 == 0
                select i;
            check from int i in evenNumbers
                do {
                    sumOfEven += i;
                };
        };
    assertEquality(8, sumOfEven);
}

function returnErrorOrNil1() returns error? {
    return ();
}

function foo1() returns string|error? {
    check from int _ in [1, 3, 5]
    do {        
        check returnErrorOrNil1();
        return "str1";
    };
    return "str2";
}

function returnErrorOrNil2() returns error? {
    return error("New error");
}

function foo2() returns string|error? {
    check from int _ in [1, 3, 5]
    do {        
        check returnErrorOrNil2();
        return "str1";
    };
    return "str2";
}

function foo3() returns string|error? {
    check from int _ in []
    do {        
        check returnErrorOrNil1();
        return "str1";
    };
    return "str2";
}

function foo4() returns string|error? {
    check from int _ in []
    do {        
        check returnErrorOrNil2();
        return "str1";
    };
    return "str2";
}

function testQueryActionWithDoClauseContainsCheck() {
    string|error? res = foo1();
    assertTrue(res is string && res == "str1");
    res = foo2();
    assertTrue(res is error && res.message() == "New error");
    res = foo3();
    assertTrue(res is string && res == "str2");
    res = foo4();
    assertTrue(res is string && res == "str2");
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
