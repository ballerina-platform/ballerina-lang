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

function varInTransform() returns (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, Foo()> p;
    return (e.name, e.age, e.address);
}

function getPrefixedName(string a) returns (string) {
    return "";
}

transformer <Person p, Employee e> Foo() {
    e.address = p.city;
    var temp = getPrefixedName(p.firstName);
    e.name = temp;
    temp = p.firstName;
    e.age = p.age;
}