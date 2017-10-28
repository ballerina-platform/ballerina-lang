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

struct Strudent {
    string name;
    int age;
    int id;
    float gpa;
}

function foo() {
    int a = 5;
}

transformer <Person p, Employee e> Foo (int id, int empCode) {
  e.name = p.name;
  e.age = p.age;
  e.id = id;
  e.empCode = empCode;
}

transformer <Person p, Strudent s>  Bar (int id, int empCode) {
  s.name = p.name;
  s.age = p.age;
  s.id = id;
}