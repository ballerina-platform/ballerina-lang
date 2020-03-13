import ballerina/io;

// Note the syntactic difference in the function body from a regular block function body.
// This is equivalent to `function add(int a, int b) returns int { return a + b; }`.
// The static type of the expression should either be the same or a subtype of the return type
// of the function.
function add(int a, int b) returns int => a + b;

public function main() {
    // There is no difference in how you call an expression-bodied function.
    int sum = add(10, 20);
    io:println(sum);

    // Anonymous functions can have expression bodies as well.
    // Here, an expression bodied anonymous function is used to easily map a
    // record to another record type.
    var toEmployee = function (Person p, string pos) returns Employee => {
        name: p.fname + " " + p.lname,
        designation: pos
    };

    Person john = { fname: "John", lname: "Doe", age: 25 };
    Employee johnEmp = toEmployee(john, "Software Engineer");

    io:println(johnEmp);
}

type Person record {|
    string fname;
    string lname;
    int age;
|};

type Employee record {|
    string name;
    string designation;
|};
