type Person1 abstract object {
    public int age = 10;
    public string name = "sample name";
};

type Employee1 object {
    public float salary;
};

type Manager1 object {
    *Person1;

    string dpt = "HR";

    // refering a non-abstarct object
    *Employee1;
};

type EmployeeWithSalary abstract object {
    public float salary;
};

type AnotherEmployeeWithSalary abstract object {
    public int salary;
};

type ManagerWithTwoSalaries object {
    *Person1;

    string dpt = "HR";
    *EmployeeWithSalary;
    *AnotherEmployeeWithSalary;
};

// Direct circular reference
type Foo abstract object {
    *Foo;
};

// Indirect circular references
type A abstract object {
    *B;
};

type B abstract object {
    *C;
};

type C abstract object {
    *D;
    *E;
};

type D abstract object {
    *A;
};

type E abstract object {
    *C;
};

// Test errors for unimplemented methods
type Person2 abstract object {
    public int age = 10;
    public string name = "sample name";

    // Unimplemented function at the nested referenced type.
    public function getName(string? title) returns string;
};

type Employee2 abstract object {
    *Person2;
    public float salary;

    // Unimplemented function at the referenced type.
    public function getSalary() returns float;
};

type Manager2 object {
    string dpt = "HR";
    *Employee2;
};
