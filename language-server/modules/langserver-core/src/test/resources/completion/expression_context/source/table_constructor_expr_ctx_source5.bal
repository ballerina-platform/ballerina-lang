type Employee1 record {
    readonly string firstname;
    readonly string lastname;
    int salary;
};

table<Employee1> key <string> t4 = table key()
