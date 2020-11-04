public function main() {
    future<int> f1 = start addition(1, 2);
    map<int> results = wait {f1};

    worker w1 {
        int x = multiply(3,4);
    }
    _ = wait w1;

    _ = start sayHello();      
}

function addition(int x, int y) returns int {
    func1();
    return x + y;
}

function func1() {
    func2();
}

function func2() {
    func3();
}

function func3() {
    Student student = new();
    string name = student -> getName();
    int x = 0;
}

public client class Student {
    public remote function getName() returns string {
        string name = "Praveen";
        return name;
    }
}

function sayHello() {
    string greetings = "Hello Ballerina";
}

function multiply(int x, int y) returns int {
    return x + y;
}
