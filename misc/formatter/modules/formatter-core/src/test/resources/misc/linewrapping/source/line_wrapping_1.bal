function foo(int count, int count, int count, int count, int count, int count, int count, int count, int count = 999999999) {
}

public type Client client object {
    remote function query(@untainted string|ParameterizedQuery sqlQuery, typedesc<record { }>? rowType = ())
    returns @tainted stream<record { };, Error>;
}

function foo() {
        ReportTable|error reportTable = table key(id) from var student in studentList
                                        where student.gpa >= 2.0
                                        let string degreeName = "Bachelor of Medicine", int graduationYear = calGraduationYear(student.intakeYear), int test = calGraduationYear(student.intakeYear)
                                        select {id: student.id};
}

public function main() {
    string stringVal =  "this is a sample. this is a sample. this is a sample. this is a sample. this is a sample. this is a sample. " + " this is a sample. this is a sample. this is a sample." + "this is a sample. this is a sample. this is a sample. this is a sample. this is a sample. this is a sample. this is a sample." + " this is a sample. this is a sample.";
}
