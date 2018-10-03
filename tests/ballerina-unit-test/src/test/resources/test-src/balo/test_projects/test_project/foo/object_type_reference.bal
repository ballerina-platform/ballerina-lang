public type Person1 abstract object {
    public int age = 10;
    public string name = "sample name";

    public function getName(string greeting = "Hi") returns string;
};

public type Employee1 abstract object {
    public float salary;

    public function getBonus(float ratio, int months=12) returns float;
};

public type Manager1 object {
    *Person1;

    string dpt = "HR";

    *Employee1;

    public function getBonus(float ratio, int months=8) returns float {
        return self.salary*ratio*months;
    }

    public new(age=20) {
        name = "John";
        salary = 1000.0;
    }
};

function Manager1::getName(string greeting = "Hello") returns string {
    return greeting + " " + self.name;
}

public type Employee2 abstract object {
    public float salary;
    *Person1;
    public function getBonus(float ratio, int months=12) returns float;
};
