import ballerina/io;

type Person record {|
    string name;
    int age = 25;
|};

type Employee record {|
    // References the `Person` record.
    *Person;
    string company?;
    string designation;
|};

type Manager record {
    Employee[] team?;
    // References the `Employee` record. Since `Employee` references `Person`,
    // `Manager` will have the fields of `Person` and the additional fields
    // in `Employee`.
    *Employee;
};

public function main() {
    // `Employee` has all the fields of `Person`.
    Employee john = {name: "John Doe", designation: "Software Engineer"};
    Employee jane = {name: "Jane Doe", designation: "UX Engineer"};

    // Type referencing copies the fields including their properties
    // (e.g., type, default value, optional status). As it can be seen
    // by printing an `Employee` record, the optional field company
    // is not included in the record.
    io:println(john);
    io:println(jane);

    Manager mgr = {name: "Mark", age: 35, designation: "Engineering Manager"};
    mgr.team = [john, jane];
    mgr.company = "XYZ Inc.";

    io:println(mgr);
}
