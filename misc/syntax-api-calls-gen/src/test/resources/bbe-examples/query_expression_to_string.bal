import ballerina/io;

type Student record {
    string firstName;
    string lastName;
    int intakeYear;
    float gpa;
};

public function main() {
    Student s1 = { firstName: "Michelle", lastName: "Sadler", intakeYear: 1990, 
                   gpa: 3.5 };
    Student s2 = { firstName: "Ranjan", lastName: "Fonseka", intakeYear: 2001, 
                   gpa: 1.9 };
    Student s3 = { firstName: "Martin", lastName: "Guthrie", intakeYear: 2002, 
                   gpa: 3.7 };
    Student s4 = { firstName: "George", lastName: "Fernando", intakeYear: 2005, 
                   gpa: 4.0 };

    Student[] studentList = [s1, s2, s3, s4];

    //The `from` clause works similarly to a `foreach` statement.
    //It can be used to iterate any iterable value.
    //The `students` is the concatenated string of the query expression results.
    string students = from var student in studentList
       //The `where` clause provides a way to perform conditional execution and works similarly to an `if` condition.
       //It can refer to variables bound by the from clause.
       //When the `where` condition evaluates to false, the current iteration is skipped.
       where student.gpa >= 2.0
       //The values emitted from `select` clause is concatenated to get the string result of the query statement.
       select student.firstName + " " + student.lastName + "\n";

    io:println(students);
}
