
public type employee object {

    public int age;
    public string name;
    public string address;


    public new (int a = 10, name) {
        self.age = a;
    }

    public function getAge();

};

function employee::getAge() {
    self.age = 12;
}

// Struct with private initializer
public type student object {

    public int age = 20;
    public string name;
    public string address;


    public function getAge();

};
