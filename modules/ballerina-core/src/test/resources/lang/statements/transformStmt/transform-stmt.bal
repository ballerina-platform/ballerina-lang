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

function oneToOneTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
       e.address = p.city;
       e.name = p.firstName;
       e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function functionsInTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
       e.address = p.city;
       e.name = getPrefixedName(p.firstName);
       e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function varInTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
        e.address = p.city;
        var temp = getPrefixedName(p.firstName);
        e.name = temp;
        e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function varDefInTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
        string prefix = "Ms.";
        e.address = p.city;
        e.name = getNameWithPrefix(prefix, p.firstName);
        e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function castAndConversionInTransform() (string, int, string, any){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    any ageAny = "";
    any defaultAddress = "New York";
    transform {
        string age = "20";
        e.address, _ = (string) defaultAddress; //unsafe explicit cast
        e.name = getPrefixedName(p.firstName);
        e.age, _ = <int> age; //unsafe conversion
        ageAny = p.age; // implicit cast
    }
    return e.name, e.age, e.address, ageAny;
}

function getPrefixedName(string name)(string){
    return "Mr." + name;
}

function getNameWithPrefix(string prefix, string name)(string){
    return prefix + name;
}