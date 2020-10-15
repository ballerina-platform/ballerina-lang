public function main() {
    //The `from` clause works similarly to a `foreach` statement.
    //It can be used to iterate any iterable value.
    //The `reportList` is the result of the `query` expression.
    Report[] reportList = from var student in studentList
                          //The `where` clause provides a way to perform conditional execution and works similarly to an `if` condition.
                          //It can refer to variables bound by the `from` clause.
                          //When the `where` condition evaluates to false, the current iteration is skipped.
                          where student.gpa >= 2.0
                          //The `let` clause binds the variables.
                          let string degreeName = "Bachelor of Medicine", int graduationYear = calGraduationYear(student.
                          intakeYear)
                          //The `order by` clause sorts the output items based on the given `order-key` and `order-direction`.
                          //The `order-key` must be an ordered type. The `order-direction` is `ascending` if not specified explicitly.
                          order by student.firstName descending
                          //The `limit` clause limits the output items.
                          limit 2
                          //The `select` clause is evaluated for each iteration.
                          //The result of the query expression is a list (`reportList`) whose members are the result of
                          //the `select` clause.
                          select {
                              name: student.firstName + " " + student.lastName,
                              degree: degreeName,
                              graduationYear: graduationYear
                          };

    foreach var report in reportList {
        io:println(report);
    }
}

function calGraduationYear(int year) returns int {
    return year + 5;
}
