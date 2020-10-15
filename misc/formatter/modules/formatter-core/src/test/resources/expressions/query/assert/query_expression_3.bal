public function main() {
    Student s1 = {
        firstName: "Michelle",
        lastName: "Sadler",
        intakeYear: 1990,
        gpa: 3.5
    };

    Student[] studentList = [s1, s2, s3, s4];

    //The `from` clause works similarly to a `foreach` statement.
    //It can be used to iterate any iterable value.
    //The `students` is the concatenated string of the query expression results.
    string students = from var student in studentList
                      //The `where` clause provides a way to perform conditional execution and works similarly to an `if` condition.
                      //It can refer to variables bound by the from clause.
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
                      //The values emitted from `select` clause is concatenated to get the string result of the query statement.
                      select student.firstName + " " + student.lastName + "\n";

    io:println(students);
    foreach var report in reportStream {
        io:println(report);
    }
}
