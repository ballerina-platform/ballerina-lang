import ballerina/io;

type Student record {
    string firstName;
    string lastName;
    float gpa;
};

type FullName record {|
    string firstName;
    string lastName;
|};

public function main() {
    Student s1 = { firstName: "Michelle", lastName: "Sadler", gpa: 3.5 };
    Student s2 = { firstName: "Ranjan", lastName: "Fonseka", gpa: 1.9 };
    Student s3 = { firstName: "Martin", lastName: "Guthrie", gpa: 3.7 };

    Student[] studentList = [s1, s2, s3];

    FullName[] nameList = [];
    // The `query-action` works similar to a `foreach` statement.
    // It can be used to iterate through any iterable value.
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
