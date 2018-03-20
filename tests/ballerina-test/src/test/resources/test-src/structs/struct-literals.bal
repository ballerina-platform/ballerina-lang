struct Department {
    string dptName;
    Person[] employees;
    Person manager;
}

struct Person {
    string name = "default first name";
    string lname;
    map adrs;
    int age = 999;
    Person? child;
}

function testStructLiteral1 () (Department) {
    Department p = {};
    return p;
}

function testStructLiteral2 () (Department) {
    Department p;
    return p;
}

function testStructLiteral3 () (Person) {
    Person p = {};
    return p;
}

function testStructLiteral4 () (Person) {
    Person p;
    return p;
}