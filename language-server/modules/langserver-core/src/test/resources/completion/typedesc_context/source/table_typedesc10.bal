import ballerina/module1;

public const int TEST_CONST = 12;

type Address record {|
    string addressLine1;
    string addressLine2;
    string zipCode;
    string state;
|};

type Employee1 record {
    readonly string firstname;
    readonly string lastname;
    readonly int salary;
    readonly Address address;
    int leaves;
};

type EmployeeId record {
    readonly string firstname;
    readonly string lastname;
};

type Employee2 record {
    *EmployeeId;
    int salary;
};

type Employee3 record {
    readonly record {|
        string first;
        string last;
    |} name;
    int salary;
};

type EmployeeId2 record {|
        string first;
        string last;
|};

type Employee4 record {
    readonly EmployeeId2 name;
    int salary;
};

table<Employee1> key<> 
