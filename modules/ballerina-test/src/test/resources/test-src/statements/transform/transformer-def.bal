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
    Person p;
    string s;
    
    Employee emp1;
    emp1 = <Employee> p;
    
    // Employee emp2 = <Student; Foo(20, 45)> p;
}


transformer <Person p, Employee e> {
  e.name = p.name;
  e.age = p.age;
}

transformer <Person p, Employee e> Foo (int id, int empCode) {
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