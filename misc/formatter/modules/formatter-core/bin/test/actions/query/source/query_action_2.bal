public function main() {
    Student[] studentList = [s1, s2, s3];

    FullName[] nameList = [];
    // The result of the `query-action` is the termination value of the iterable value.
    // It can be either an error or `()`.
    var result = from var student in studentList
        // The block inside the `do` clause is executed in each iteration.
        do {
            FullName fullName = { firstName: student.firstName, lastName: student.lastName };
            nameList.push(fullName);
        };

    foreach var name in nameList {
        io:println(name);
    }
}
