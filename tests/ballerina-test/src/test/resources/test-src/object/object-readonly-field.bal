import org.bar;

@readonly public int globalInt = 10;

public type Employee object {
   public {
       @readonly int age = 10,
       float salary = 50.5,
       string name = "john";
   }

   private {
       int id = 50;
       string ssn = "aaa";
   }
};

public function createEmployee() returns (Employee) {
    return new Employee();
}

public function testReadOnlyObjFieldAccessInSamePackage() returns (int, int, string) {
    Employee e = createEmployee();
    int age1 = e.age;

    e.age = 44;
    e.salary = 400.5;

    int age2 = e.age;
    return (age1, age2, e.name);
}


public function testReadOnlyGlobalVarAccessInSamePackage() returns (int, int, int) {
    int v1 = globalInt;
    globalInt = 30;
    int v2 = globalInt;
    return (v1, v2, globalInt);
}
