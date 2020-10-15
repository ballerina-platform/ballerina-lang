public function main() {
    //The `from` clause works similarly to a `foreach` statement.
    //It can be used to iterate any iterable value.
    //The `reportStream` is the result of the `query` expression.
    stream<Report> reportStream = stream from var student in studentList
                                         //The `where` clause provides a way to perform conditional execution and works similarly to an `if` condition.
                                         //It can refer to variables bound by the from clause.
                                         //When the `where` condition evaluates to false, the current iteration is skipped.
                                         where student.gpa >= 2.0
                                         //The `let` clause binds the variables.
                                         let string degreeName = "Bachelor of Medicine", int graduationYear = 
                                         calGraduationYear(student.intakeYear)
                                         //The `limit` clause limits the output items.
                                         limit 2
                                         //The `select` clause is evaluated for each iteration.
                                         //The result of the query expression is a stream (`reportStream`) whose members are the result of the
                                         //`select` clause.
                                         select {
                                             name: student.firstName + " " + student.lastName,
                                             degree: degreeName,
                                             graduationYear: graduationYear
                                         };

    foreach var report in reportStream {
        io:println(report);
    }
}

function calGraduationYear(int year) returns int {
    return year + 5;
}
