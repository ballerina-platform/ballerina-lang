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

// Disabled due to #25585
//function testQueryExprWithLimitForStringResultV2() returns string {
//    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
//    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
//    Person p3 = {firstName: "John", lastName: "David", age: 33};
//
//    Person[] personList = [p1, p2, p3];
//
//    string outputNameString =
//                from var person in personList
//                let int limitValue = 2
//                where person.age >= 30
//                limit limitValue
//                select person.firstName+" ";
//
//    return outputNameString;
//}
