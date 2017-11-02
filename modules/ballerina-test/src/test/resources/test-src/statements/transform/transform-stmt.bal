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

function unnamedTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee> p;
    return e.name, e.age, e.address;
}

function namedTransform() (string, int, string){
    Person p = {firstName:"Jane", lastName:"Doe", age:25, city:"Paris"};
    Employee e = <Employee, Foo()> p;
    return e.name, e.age, e.address;
}

function namedTransformWithParams() (string, int, string){
    Person p = {firstName:"Jack", lastName:"Doe", age:45, city:"NY"};
    Employee e = <Employee, Bar(99)> p;
    return e.name, e.age, e.address;
}

transformer <Person p, Employee e> {
    e.address = p.city;
    e.name = p.firstName;
    e.age = p.age;
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

function functionsInTransform() (string, int, string) {
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, transformerWithFunction()> p;
    return e.name, e.age, e.address;
}

transformer <Person p, Employee e> transformerWithFunction() {
    e.address = p.city;
    e.name = getPrefixedName(p.firstName);
    e.age = p.age;
}

function varInTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = <Employee, transformerWithVar()> p;
    return e.name, e.age, e.address;
}

transformer <Person p, Employee e> transformerWithVar() {
    e.address = p.city;
    var temp = getPrefixedName(p.firstName);
    e.name = temp;
    e.age = p.age;
}

function varDefInTransform() (string, int, string){
    Person p = {firstName:"Jane", lastName:"Doe", age:28, city:"CA"};
    Employee e = <Employee, transformerWithVarDef()> p;
    return e.name, e.age, e.address;
}

transformer <Person p, Employee e> transformerWithVarDef() {
    string prefix = "Ms.";
    e.address = p.city;
    e.name = getNameWithPrefix(prefix, p.firstName);
    e.age = p.age;
}


struct Employee_1 {
    string name;
    int age;
    any anyAge;
    string address;
}

function castAndConversionInTransform() (string, int, string, any){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    any defaultAddress = "New York";
    Employee_1 e = <Employee_1, transformerWithCastAndConversion(defaultAddress)> p;
    return e.name, e.age, e.address, e.anyAge;
}

transformer <Person p, Employee_1 e> transformerWithCastAndConversion(any defaultAddress) {
    string age = "20";
    e.address, _ = (string) defaultAddress; //unsafe explicit cast
    e.name = getPrefixedName(p.firstName);
    e.age, _ = <int> age; //unsafe conversion
    e.anyAge = p.age; // implicit cast
}

function getPrefixedName(string name)(string){
    return "Mr." + name;
}

function getNameWithPrefix(string prefix, string name)(string){
    return prefix + name;
}