struct Person {
    string name;
    int age;
}

struct ABCEmployee {
    string name;
    int age;
    private float salary;
}

struct XYZEmployee {
    string name;
    int age;
    private float salary;
}

struct FakeEmployee {
    string name;
    int age;
    float salary;
}

function test1 () (string x, float y) {
    ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    person:Person p = (person:Person)emp;
    x = p.name;
    var temp, _ = (ABCEmployee)p;
    y = temp.getSalary();
    return;
}

function test2 () (string x, float y) {
    ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    any value = emp;
    var p, _ = (person:Person) value;
    x = p.name;
    var temp, _ = (ABCEmployee)p;
    y = temp.getSalary();
    return;
}

function test3()(ABCEmployee, TypeCastError){
    person:Person p = {name:"tom", age:11};
    var emp, err = (ABCEmployee) p;
    return emp, err;
}

function test4()(FakeEmployee, TypeCastError){
    person:Person p = {name:"tom", age:11};
    var emp, err = (FakeEmployee) p;
    return emp, err;
}

function test5 () (FakeEmployee, TypeCastError) {
    ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp, err = (FakeEmployee) emp;
    return temp, err;
}

function test6 () (XYZEmployee, TypeCastError) {
    ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp, err = (XYZEmployee) emp;
    return temp, err;
}

function test7 () (ABCEmployee, TypeCastError) {
    XYZEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp, err = (ABCEmployee) emp;
    return temp, err;
}
