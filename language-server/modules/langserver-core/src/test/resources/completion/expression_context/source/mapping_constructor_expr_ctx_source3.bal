type Employee record {|
    readonly int empId;
    string name;
|}; 

class TestClass {
    table<Employee> key(empId) employees = table [{}];
}
