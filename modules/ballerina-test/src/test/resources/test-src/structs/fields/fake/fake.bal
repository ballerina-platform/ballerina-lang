package fake;

public struct FakeEmployee {
    string name;
    int age;
    float salary;
}

public function <FakeEmployee e> setSalary (float sal) {
    e.salary = sal;
}

public function <FakeEmployee e> getSalary () returns (float) {
    return e.salary;
}
