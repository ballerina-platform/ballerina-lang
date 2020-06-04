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

function testQueryExprWithLimitForStringResult() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string outputNameString =
                from var person in personList
                where person.age >= 30
                select person.firstName+" "
                limit 1;

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
                select person.firstName+" "+(teacher.teacherId).toString()+" "
                limit 2;

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
