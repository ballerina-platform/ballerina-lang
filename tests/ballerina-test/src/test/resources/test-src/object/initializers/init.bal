
public type employee object {
    public {
        int age;
        string name;
        string address;
    }

    public new (int age = 10, name) {
        self.age = age;
    }

    public function getAge();

};

public function  employee::getAge() {
    self.age = 12;
}

// Struct with private initializer
public type student object {
    public {
        int age = 20;
        string name;
        string address;
    }

    public function getAge();

};
