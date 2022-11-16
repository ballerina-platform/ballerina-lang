type Employee record {|
    readonly int empId;
    string name;
|}; 

class MyClass {
    int x = 10;
    Employee emp = {empId: x, };
}
