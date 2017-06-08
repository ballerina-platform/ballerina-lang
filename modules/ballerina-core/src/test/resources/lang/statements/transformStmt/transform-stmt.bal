struct Person {
    string first_name;
    string last_name;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
}

function oneToOneTransform() (string, int, string){
    Person p = {first_name:"John", last_name:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
       e.address = p.city;
       e.name = p.first_name;
       e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function functionsInTransform() (string, int, string){
    Person p = {first_name:"John", last_name:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
       e.address = p.city;
       e.name = getPrefixedName(p.first_name);
       e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function getPrefixedName(string name)(string){
    return "Mr." + name;
}