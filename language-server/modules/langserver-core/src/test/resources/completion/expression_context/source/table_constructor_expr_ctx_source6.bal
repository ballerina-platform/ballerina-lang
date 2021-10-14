
type EmployeeId record {
    readonly string firstname;
    readonly string lastname;
    readonly string
};

type Employee record {
    *EmployeeId;
    readonly int salary;
    int numOfLeaves;
};

table<Employee> key<EmployeeId> t4 = table key() [];
