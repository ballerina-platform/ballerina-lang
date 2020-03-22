import ballerina/io;

type Student record {
    string firstName;
    string lastName;
    float score;
};

type FullName record {|
    string firstName;
    string lastName;
|};

public function main() {
    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 0.9};
    Student s3 = {firstName: "John", lastName: "David", score: 1.2};

    Student[] studentList = [s1, s2, s3];

    FullName[] nameList = [];
    //`query-action` works similarly to a `foreach` statement.
    //It can be used to iterate through any iterable value.
    //The result of the query-action is the termination value of the iterable value.
    //It can be either an error or ().
    var e = from var student in studentList
    // The block inside the `do` clause is executed for each iteration.
    do {
        FullName fullName = {firstName: student.firstName, lastName: student.lastName};
        nameList.push(fullName);
    };

    foreach var name in nameList {
        io:println(name);
    }
}
