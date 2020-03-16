import ballerina/io;

type Student record {
    string firstName;
    string lastName;
    int intakeYear;
    float score;
};

type Report record {
    string name;
    string degree;
    int expectedGradYear;
};

public function main() {

    Student s1 = {firstName: "Alex", lastName: "George", intakeYear: 2020, score: 1.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", intakeYear: 2020, score: 0.9};
    Student s3 = {firstName: "John", lastName: "David", intakeYear: 2022, score: 1.2};

    Student[] studentList = [s1, s2, s3];

    //`from` clause works similarly to a `foreach` statement.
    //It can be used to iterate through any iterable value.
    //`outputStudentList` is the result of the `query` expression
    Report[] reportList = from var student in studentList
    //The `where` clause provides a way to perform conditional execution and works similarly to a `if` condition.
    //It can refer to variables bound by the from clause.
    //When the where condition evaluates to false, the iteration skips following clauses.
    where student.score >= 1
    //The `let` clause binds variables.
    let string degreeName = "Bachelor of Medicine",
    int graduationYear = calGraduationYear(student.intakeYear)
    //The `select` clause is evaluated for each iteration;
    //the result of the query expression is a list(`reportList`) whose members are the result of the select clause.
       select {
              name: student.firstName,
              degree: degreeName,
              expectedGradYear: graduationYear
       };

    foreach var report in reportList {
        io:println(report);
    }
}

function calGraduationYear(int year) returns int {
    return year + 5;
}
