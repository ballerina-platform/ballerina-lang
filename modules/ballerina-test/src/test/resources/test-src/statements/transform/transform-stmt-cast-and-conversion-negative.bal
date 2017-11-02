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
    any ageAny;
}

function castAndConversionInTransform() (string, int, string, any){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    any defaultAddress = "New York";
    Employee e = <Employee, Foo(defaultAddress)> p;
    return e.name, e.age, e.address, e.ageAny;
}

function getPrefixedName(string a) (string) {
    return "";
}

transformer <Person p, Employee e> Foo(any defaultAddress) {
    string age = "20";
    e.address, _ = (string) defaultAddress; //unsafe explicit cast
    defaultAddress = p.city;
    e.name = p.firstName;
    e.age, _ = <int> age; //unsafe conversion
    age = p.age;
    e.ageAny = p.age; // implicit cast
}