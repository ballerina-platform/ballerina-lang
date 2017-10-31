package xyz;

public struct XYZEmployee {
    string name;
    int age;
    private float salary;
}

public function <XYZEmployee e> setSalary (float sal) {
    e.salary = sal;
}

public function <XYZEmployee e> getSalary () returns (float) {
    return e.salary;
}
