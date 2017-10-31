package abc;

public struct ABCEmployee {
    string name;
    int age;
    private float salary;
}

public function <ABCEmployee e> setSalary (float sal) {
    e.salary = sal;
}

public function <ABCEmployee e> getSalary () returns (float) {
    return e.salary;
}
