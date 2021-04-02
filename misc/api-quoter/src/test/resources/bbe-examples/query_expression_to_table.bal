import ballerina/io;

type Student record {
    readonly id;
    string firstName;
    string lastName;
    int intakeYear;
    float gpa;
};

type Report record {
    readonly id;
    string name;
    string degree;
    int graduationYear;
};

// This is a `table` type with `Report` members uniquely identified by their `id` field.
type ReportTable table<Report> key(id);

public function main() {
    Student s1 = { id: 1, firstName: "Michelle", lastName: "Sadler", 
                   intakeYear: 1990, gpa: 3.5 };
    Student s2 = { id: 2, firstName: "Ranjan", lastName: "Fonseka", 
                   intakeYear: 2001, gpa: 1.9 };
    Student s3 = { id: 3, firstName: "Martin", lastName: "Guthrie", 
                   intakeYear: 2002, gpa: 3.7 };
    Student s4 = { id: 4, firstName: "George", lastName: "Fernando", 
                   intakeYear: 2005, gpa: 4.0 };

    Student[] studentList = [s1, s2, s3, s4];

    // The `query expression` starts with `table`.
    // The key specifier `key(id)` specifies the key sequence of the constructed `table`.
    // The result of the `query expression` is a `table`.
    ReportTable|error reportTable =
        table key(id) from var student in studentList
        // The `where` clause provides a way to perform conditional execution and works similarly to an `if` condition.
        // It can refer to variables bound by the from clause.
        // When the `where` condition evaluates to false, the current iteration is skipped.
        where student.gpa >= 2.0
        // The `let` clause binds the variables.
        let string degreeName = "Bachelor of Medicine",
        int graduationYear = calGraduationYear(student.intakeYear)
        // The `limit` clause limits the number of output items.
        limit 2
        // The `select` clause is evaluated for each iteration.
        // During the construction of a `table`, each emitted value from the `select` clause is added as a new member.
        select {
            id: student.id,
            name: student.firstName + " " + student.lastName,
            degree: degreeName,
            graduationYear: graduationYear
        };

    io:println(reportTable);

    Student[] duplicateStdList = [s1, s2, s1];

    // Defines an `error` to handle a key conflict.
    error onConflictError = error("Key Conflict",
                                  message = "cannot insert report");

    ReportTable|error result =
        table key(id) from var student in duplicateStdList
        select {
            id: student.id,
            name: student.firstName + " " + student.lastName,
            degree: "Bachelor of Medicine",
            graduationYear: calGraduationYear(student.intakeYear)
        } 
        // The `on conflict` clause gets executed when `select` emits a row
        // that has the same key as a row that it emitted earlier.
        // It gives an `onConflictError` error if there is a key conflict.
        on conflict onConflictError;

    io:println(result);
}

function calGraduationYear(int year) returns int {
    return year + 5;
}
