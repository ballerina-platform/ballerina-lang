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
    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5 };
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka",  score: 0.9 };
    Student s3 = {firstName: "John", lastName: "David",  score: 1.2 };

    Student[] studentList = [s1, s2, s3];

    io:println("\nIterate record list and map it to another record type:");
        FullName[] nameList = [];
        //Like query-expression, query-action can be used with any iterable value.
        //Query action has a `do` clause instead of the `select` clause.
        //The result of the query-action is the termination value of the iterator.
        from var student in studentList
        //The block inside the `do` clause is executed for each iteration.
          do {
                FullName fullName = {firstName: student.firstName, lastName: student.lastName};
                nameList[nameList.length()] = fullName;
          }

        io:println(nameList);
}