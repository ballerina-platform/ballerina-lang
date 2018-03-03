import initializers as inp;

struct person {
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
    return p.age, p.name;
}

function testStructInitializerInAnotherPackage() returns (int, string){
    inp:employee e = {name:"Peter"};
    return e.age, e.name;
}

