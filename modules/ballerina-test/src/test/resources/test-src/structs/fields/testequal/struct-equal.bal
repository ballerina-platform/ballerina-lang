package testequal;

import abc;
import xyz;
import person;
import fake;

function test1 () (string x, float y) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    person:Person p = (person:Person)emp;
    x = p.name;
    var temp, _ = (abc:ABCEmployee)p;
    y = temp.getSalary();
    return;
}

function test2 () (string x, float y) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    any value = emp;
    var p, _ = (person:Person) value;
    x = p.name;
    var temp, _ = (abc:ABCEmployee)p;
    y = temp.getSalary();
    return;
}

function test3()(abc:ABCEmployee, TypeCastError){
    person:Person p = {name:"tom", age:11};
    var emp, err = (abc:ABCEmployee) p;
    return emp, err;
}

function test4()(fake:FakeEmployee, TypeCastError){
    person:Person p = {name:"tom", age:11};
    var emp, err = (fake:FakeEmployee) p;
    return emp, err;
}

function test5 () (fake:FakeEmployee, TypeCastError) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp, err = (fake:FakeEmployee) emp;
    return temp, err;
}

function test6 () (xyz:XYZEmployee, TypeCastError) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp, err = (xyz:XYZEmployee) emp;
    return temp, err;
}

function test7 () (abc:ABCEmployee, TypeCastError) {
    xyz:XYZEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp, err = (abc:ABCEmployee) emp;
    return temp, err;
}
