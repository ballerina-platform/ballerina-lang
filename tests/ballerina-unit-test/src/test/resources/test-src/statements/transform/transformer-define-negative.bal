struct Person {
    string fname;
    string lname;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
}

function testTransformer() {
    Person p = {fname:"John", lname:"Doe", age:30, city:"London"};

    Employee e1 = <Person, Foo_1()> p;

    Employee e2 = <Person, Foo_1()> p;
    
    Person e3 = <Employee, Foo_1()> p;

    Person e4 = <Person, Foo_1()> p;
    
    Employee e5 = <string, Foo_1()> p;
    
    Employee e6;
    string s;
    e6, s = <Employee, Foo_1()> p;
    
    var e6, s, err = <Employee, Foo_1()> p;
}

function Foo_1() {
    // empty
}

transformer <Person p, Employee e> Foo_1() {
    // empty
}

transformer <Person p, Employee e> Foo_1() {
    // empty
}

transformer <string s1, string s2> Foo_1() {
    // empty
}

transformer <string s1, string s2> Foo_2() {
    // empty
}

transformer <Person p1, Employee e1> {
    // empty
}

transformer <Person p2, Employee e2> {
    // empty
}

transformer <Person p, Employee e1, Employee e2, Employee e3>  Foo_3() {
    // empty
}

transformer <Person p, Employee e> Foo_4() {
    endpoint<testEP> con { }
    
    string s = con -> textAction_1();
    
    return;
    
    try {
    } catch (error err) {
    }
    
    if (true) {
    } else {
    }
    
    while (true) {
    }
    
    Foo_1();

    continue;
}


connector TestConnector() {
    action textAction_1() (string) {
        return "hello";
    }
}

transformer <int a, string s> {
    // empty
}

transformer <TestConnector con, Employee e1> {
    // empty
}

transformer <Person p1, TestConnector con> {
    // empty
}

struct testEP {
}

function <testEP ep> init(string name, struct {} config) {
}

function <testEP ep> start() {
}

function <testEP ep> stop() {
}

function <testEP ep> register(type t) {
}

function <testEP ep> getConnector() returns (TestConnector | null) {
    return null;
}
