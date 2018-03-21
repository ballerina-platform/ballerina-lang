struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
}

function varDefInTransform() returns (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, Foo_1()> p;
    return (e.name, e.age, e.address);
}

function varDefInTransformWithInput() returns (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, Foo_2()> p;
    return (e.name, e.age, e.address);
}

function getNameWithPrefix(string prefix, string name) returns (string) {
    return prefix + name;
}

function getPrefixedName(string name) returns (string) {
    return "Ms." + name;
}

transformer <Person p, Employee e> Foo_1() {
    string prefix = "Ms.";
    e.address = p.city;
    e.name = getNameWithPrefix(prefix, p.firstName);
    prefix = "Mrs.";
    e.age = p.age;
}

transformer <Person p, Employee e> Foo_2() {
    string name = getPrefixedName(p.firstName);
    e.address = p.city;
    e.name = name;
    name = "Mrs. Peter";
    e.age = p.age;
}