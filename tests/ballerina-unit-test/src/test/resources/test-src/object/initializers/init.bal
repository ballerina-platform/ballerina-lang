public type employee object {

    public int age = 0;
    public string name = "";
    public string address = "";

    public function __init (int a = 10, string name) {
        self.age = a;
        self.name = name;
    }

    public function getAge();

};

public function employee.getAge() {
    self.age = 12;
}

// Struct with private initializer
public type student object {
    public int age = 20;
    public string name = "";
    public string address = "";

    public function getAge();

};

public function student.getAge() {
    self.age = 20;
}
