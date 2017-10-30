struct Person {
    string name;
    int age;
    int id;
}

struct Employee {
    string name;
    int age;
    int id;
    float salary;
    int empCode;
}

struct Student {
    string name;
    int age;
    int id;
    float gpa;
}

function bar() {
    foo();
}

function foo() {
    Person p = {name:"supun", age:28};
    string s;
    
    Employee emp1;
    emp1 = <Employee> p;
    println(emp1);

    Employee emp2 = <Employee; Foo(20, 45)> p;
    println(emp2);
}

transformer <Person p, Employee e> {
    e.name = p.name;
    e.age = p.age;
}

transformer <Person p, Employee e> Foo (int id, int empCode) {
    println(id);
    id.foo();
    e.name = p.name;
    e.age = p.age;
    e.id = id;
    e.empCode = empCode;
}

transformer <Person p, Student s>  Bar (int id, int empCode) {
    s.name = p.name;
    s.age = p.age;
    s.id = id;
}