type Person {
    string firstName;
    string lastName;
    int age;
    string city;
};

type Employee {
    string name;
    int age;
    string address;
};

function unnamedTransform() returns (string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    string s = <string> p;
    return (s);
}

function namedTransform() returns (string, int, string){
    Person p = {firstName:"Jane", lastName:"Doe", age:25, city:"Paris"};
    Employee e = <Employee, Foo()> p;
    return (e.name, e.age, e.address);
}

function namedTransformWithParams() returns (string, int, string){
    Person p = {firstName:"Jack", lastName:"Doe", age:45, city:"NY"};
    Employee e = <Employee, Bar(99)> p;
    return (e.name, e.age, e.address);
}

transformer <Person p, string s> {
    s = p.firstName + p.age + p.city;
}

transformer <Person p, Employee e> Foo() {
    e.address = p.city;
    e.name = p.firstName;
    e.age = p.age;
}

transformer <Person p, Employee e> Bar(int age) {
    e.address = p.city;
    e.name = p.firstName;
    e.age = age;
}

function functionsInTransform() returns (string, int, string) {
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, transformerWithFunction()> p;
    return (e.name, e.age, e.address);
}

transformer <Person p, Employee e> transformerWithFunction() {
    e.address = p.city;
    e.name = getPrefixedName(p.firstName);
    e.age = p.age;
}

function varInTransform() returns (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, transformerWithVar()> p;
    return (e.name, e.age, e.address);
}

transformer <Person p, Employee e> transformerWithVar() {
    e.address = p.city;
    var temp = getPrefixedName(p.firstName);
    e.name = temp;
    e.age = p.age;
}

function varDefInTransform() returns (string, int, string){
    Person p = {firstName:"Jane", lastName:"Doe", age:28, city:"CA"};
    Employee e = <Employee, transformerWithVarDef()> p;
    return (e.name, e.age, e.address);
}

transformer <Person p, Employee e> transformerWithVarDef() {
    string prefix = "Ms.";
    e.address = p.city;
    e.name = getNameWithPrefix(prefix, p.firstName);
    e.age = p.age;
}


type Employee_1 {
    string name;
    int age;
    any anyAge;
    string address;
};

function castAndConversionInTransform() returns (string, int, string, any){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    any defaultAddress = "New York";
    Employee_1 e = <Employee_1, transformerWithCastAndConversion(defaultAddress)> p;
    return (e.name, e.age, e.address, e.anyAge);
}

transformer <Person p, Employee_1 e> transformerWithCastAndConversion(any defaultAddress) {
    string age = "20";
    e.address = <string> defaultAddress; //unsafe explicit cast
    e.name = getPrefixedName(p.firstName);
    e.age =? <int> age; //unsafe conversion
    e.anyAge = p.age; // implicit cast
}

function getPrefixedName(string name) returns (string){
    return "Mr." + name;
}

function getNameWithPrefix(string prefix, string name) returns (string){
    return prefix + name;
}