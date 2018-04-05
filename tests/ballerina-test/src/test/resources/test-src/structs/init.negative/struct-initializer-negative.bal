package init.negative;

import initializers as inp;

type person {
    int age = 20;
    string name;
    string address;
}

function <person p> person() {
    p.age = 10;
    p.name = "Charles";
}

function <person p> getAge() returns (int) {
    p.age = 12;
    return p.age;
}

function testInitializerAsAttachedFunctionInvocation() {
    person p = {};
    int age = p.getAge();
    p.person();
}

function testPrivateInitializerInvocation(){
    inp:student s = {};
}