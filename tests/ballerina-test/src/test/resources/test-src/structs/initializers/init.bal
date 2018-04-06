package initializers;

public type employee{
    int age = 20;
    string name;
    string address;
};

public function <employee p> employee() {
    p.age = 10;
    p.name = "James";
}

public function <employee p> getAge() {
    p.age = 12;
}

// Struct with private initializer
public type student{
    int age = 20;
    string name;
    string address;
};

function <student p> student() {
    p.age = 10;
    p.name = "James";
}

public function <student p> getAge() {
    p.age = 12;
}

public function newStudent() returns (student) {
    return {};
}