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
   int teacherId;
|};

function testSimpleQueryExprForStringResult() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string outputNameString =
                from var person in personList
                select person.firstName+" ";

    return outputNameString;
}

type Book record {|
    string title;
    string author;
|};

class BookGenerator {
    int i = 0;

    public isolated function next() returns record {|Book value;|}|error? {
        self.i += 1;
        if (self.i < 0) {
            return error("Error");
        } else if (self.i >= 3) {
            return ();
        }
        return {
            value: {
                title: "Title " + self.i.toString(), author: "Author " + self.i.toString()
            }
        };
    }
}

function testSimpleQueryExprForStringResult2() {
    error? e = trap simpleQueryExprForStringResult();
    assertFalse(e is error);

    e = trap simpleQueryExprForStringResult2();
    assertFalse(e is error);

    e = trap simpleQueryExprForStringResult3();
    assertFalse(e is error);
}

function simpleQueryExprForStringResult() returns error? {
    string:Char chr = "a";
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);
    string strValue = check from Book _ in bookStream
        select chr;
    string expectedValue = "aa";
    assertTrue(strValue is string);
    assertEquality(strValue, expectedValue);
}

function simpleQueryExprForStringResult2() returns error? {
    stream<Book, error?> bookStream = [{ author: "Author 1", title: "Title 1" },
                                        {author: "Author 2", title: "Title 2" }].toStream();
    string strValue = check from Book _ in bookStream
        select <string:Char> "a";
    string expectedValue = "aa";
    assertTrue(strValue is string);
    assertEquality(strValue, expectedValue);
}

function simpleQueryExprForStringResult3() returns error? {
    string:Char chr = "a";
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);
    string:Char[] strValue = check from Book _ in bookStream
        select chr;
    assertTrue(strValue is string:Char[]);
    assertEquality(strValue[0], chr);
    assertEquality(strValue[1], chr);
}

function testQueryExprWithWhereForStringResult() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};
    Person[] personList = [p1, p2, p3];
    string outputNameString =
                from var person in personList
                where person.age >= 30
                select person.firstName+" ";

    return outputNameString;
}

function testQueryExprWithInnerJointForStringResult() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Teacher t1 = {firstName: "Alex", lastName: "Georgian", age: 23, teacherId: 1};
    Teacher t2 = {firstName: "Ranjan", lastName: "Ferdinand", age: 30, teacherId: 2};
    Teacher t3 = {firstName: "John", lastName: "Davidsan", age: 33, teacherId: 3};

    Person[] personList = [p1, p2, p3];
    Teacher[] teacherList = [t1, t2, t3];

    string outputNameString =
                from var person in personList
                join var teacher in teacherList
                on person.firstName equals teacher.firstName
                limit 2
                select person.firstName+" "+(teacher.teacherId).toString()+" ";

    return outputNameString;
}

function testQueryExprWithMultipleFromForStringResult() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Teacher t1 = {firstName: "Alex", lastName: "Georgian", age: 23, teacherId: 1};
    Teacher t2 = {firstName: "Ranjan", lastName: "Ferdinand", age: 30, teacherId: 2};

    Person[] personList = [p1, p2];
    Teacher[] teacherList = [t1, t2];

    string outputNameString =
                from var person in personList
                from var teacher in teacherList
                select person.firstName+" "+(teacher.teacherId).toString()+" ";

    return outputNameString;
}

function testQueryExprWithStreamForStringResult() returns string {
    Teacher t1 = {firstName: "Alex", lastName: "Georgian", age: 23, teacherId: 1};
    Teacher t2 = {firstName: "Alex", lastName: "Ferdinand", age: 34, teacherId: 2};

    Teacher[] teacherList = [t1, t2];

    string outputNameString =
                from var teacher in teacherList.toStream()
                where teacher.firstName == "Alex"
                where teacher.age > 30
                select (teacher.lastName).concat(" ", (teacher.teacherId).toString(), " ");

    return outputNameString;
}

function testSimpleQueryExprForStringOrNilResult() returns string? {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string? outputNameString =
                from var person in personList
                select person.firstName+" ";

    return outputNameString;
}

function testQueryExprWithWhereForStringOrNilResult() returns string? {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string? outputNameString =
                from var person in personList
                where person.age >= 30
                select person.firstName+" ";

    return outputNameString;
}

function testQueryExprWithLimitForStringOrNilResult() returns string? {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string? outputNameString =
                from var person in personList
                where person.age >= 30
                limit 1
                select person.firstName+" ";

    return outputNameString;
}

function testQueryExprWithInnerJointForStringOrNilResult() returns string? {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Teacher t1 = {firstName: "Alex", lastName: "Georgian", age: 23, teacherId: 1};
    Teacher t2 = {firstName: "Ranjan", lastName: "Ferdinand", age: 30, teacherId: 2};
    Teacher t3 = {firstName: "John", lastName: "Davidsan", age: 33, teacherId: 3};

    Person[] personList = [p1, p2, p3];
    Teacher[] teacherList = [t1, t2, t3];

    string? outputNameString =
                from var person in personList
                join var teacher in teacherList
                on person.firstName equals teacher.firstName
                limit 2
                select person.firstName+" "+(teacher.teacherId).toString()+" ";

    return outputNameString;
}

function testQueryExprWithMultipleFromForStringOrNilResult() returns string? {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Teacher t1 = {firstName: "Alex", lastName: "Georgian", age: 23, teacherId: 1};
    Teacher t2 = {firstName: "Ranjan", lastName: "Ferdinand", age: 30, teacherId: 2};

    Person[] personList = [p1, p2];
    Teacher[] teacherList = [t1, t2];

    string? outputNameString =
                from var person in personList
                from var teacher in teacherList
                select person.firstName+" "+(teacher.teacherId).toString()+" ";

    return outputNameString;
}

function testQueryExprWithStreamForStringOrNilResult() returns string? {
    Teacher t1 = {firstName: "Alex", lastName: "Georgian", age: 23, teacherId: 1};
    Teacher t2 = {firstName: "Alex", lastName: "Ferdinand", age: 34, teacherId: 2};

    Teacher[] teacherList = [t1, t2];

    string? outputNameString =
                from var teacher in teacherList.toStream()
                where teacher.firstName == "Alex"
                where teacher.age > 30
                select (teacher.lastName).concat(" ", (teacher.teacherId).toString(), " ");

    return outputNameString;
}

function testQueryExprWithVarForStringResult() returns boolean {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var outputNameString =
                from var person in personList
                where person.age >= 30
                select (person.firstName).concat(" ");

    boolean testPassed = true;
    string s;
    any res = outputNameString;
    testPassed = testPassed && res is string[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && outputNameString.length() == 2;
    s = outputNameString[0];
    testPassed = testPassed && s == "Ranjan ";
    s = outputNameString[1];
    testPassed = testPassed && s == "John ";
    return testPassed;
}

function testQueryExprWithListForStringResult() returns boolean {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string[] outputNameString =
                from var person in personList
                where person.age >= 30
                select (person.firstName).concat(" ");

    boolean testPassed = true;
    string s;
    any res = outputNameString;
    if (res is string[]) {
        testPassed = testPassed && res.length() == 2;
        s = res[0];
        testPassed = testPassed && s == "Ranjan ";
        s = res[1];
        testPassed = testPassed && s == "John ";
    } else {
        testPassed = false;
    }
    return testPassed;
}

function testQueryExprWithUnionTypeForStringResult() returns string|error {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string|error outputNameString =
                from var person in personList
                where person.age >= 30
                select (person.firstName).concat(" ");

    return outputNameString;
}

function testQueryExprWithUnionTypeForStringResult2() returns boolean {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string[]|error outputNameString =
                from var person in personList
                where person.age >= 30
                select (person.firstName).concat(" ");

    boolean testPassed = true;
    string s;
    if (outputNameString is string[]) {
        testPassed = testPassed && outputNameString.length() == 2;
        s = outputNameString[0];
        testPassed = testPassed && s == "Ranjan ";
        s = outputNameString[1];
        testPassed = testPassed && s == "John ";
    } else {
        testPassed = false;
    }
    return testPassed;
}

function testQueryExprWithLimitForStringResult() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string outputNameString =
                from var person in personList
                where person.age >= 30
                limit 1
                select person.firstName+" ";

    return outputNameString;
}

// TODO: related to the issue https://github.com/ballerina-platform/ballerina-lang/issues/43831
// function testQueryExprWithLimitForStringResultV2() returns string {
//    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
//    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
//    Person p3 = {firstName: "John", lastName: "David", age: 33};

//    Person[] personList = [p1, p2, p3];

//    string outputNameString =
//                from var person in personList
//                let int limitValue = 2
//                where person.age >= 30
//                limit limitValue
//                select person.firstName+" ";

//    return outputNameString;
// }

function assertEquality(any|error actual, any|error expected) {
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

function assertTrue(any|error actual) {
    assertEquality(actual, true);
}

function assertFalse(any|error actual) {
    assertEquality(actual, false);
}
