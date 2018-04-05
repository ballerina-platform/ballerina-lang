package init;

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

function <person p> getAge() {
    p.age = 12;
}

function testStructInitializerInSamePackage1() returns (int, string){
    person p = {name:"Peter"};
    return (p.age, p.name);
}

function testStructInitializerInAnotherPackage() returns (int, string){
    inp:employee e = {name:"Peter"};
    return (e.age, e.name);
}

// testStructInitializerOrder

type employee {
    int age = 20;
    string name = "A";
}

function <employee p> employee() {
    p.age = 30;
    p.name = p.name + "B";
}

function testStructInitializerOrder() returns (int, string){
    employee p = {age: 40};
    return (p.age, p.name);
}
