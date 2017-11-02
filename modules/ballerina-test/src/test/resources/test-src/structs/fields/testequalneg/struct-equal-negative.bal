package testequalneg;

import abc;
import xyz;
import person;
import fake;

function test1 () (string x, float y) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var p = (person:Person)emp;
    x = p.name;
    var temp = (abc:ABCEmployee)p;
    y = temp.getSalary();
    return;
}

function test2 () (string x, float y) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    any value = emp;
    var p = (person:Person) value;
    x = p.name;
    var temp = (abc:ABCEmployee)p;
    y = temp.getSalary();
    return;
}

function test3()(abc:ABCEmployee){
    person:Person p = {name:"tom", age:11};
    var emp = (abc:ABCEmployee) p;
    return emp;
}

function test4()(fake:FakeEmployee){
    person:Person p = {name:"tom", age:11};
    var emp = (fake:FakeEmployee) p;
    return emp;
}

function test5 () (fake:FakeEmployee) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp = (fake:FakeEmployee) emp;
    return temp;
}

function test6 () (xyz:XYZEmployee) {
    abc:ABCEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp = (xyz:XYZEmployee) emp;
    return temp;
}

function test7 () (abc:ABCEmployee) {
    xyz:XYZEmployee emp = {name:"bob", age:10};
    emp.setSalary(1000.5);
    var temp = (abc:ABCEmployee) emp;
    return temp;
}
