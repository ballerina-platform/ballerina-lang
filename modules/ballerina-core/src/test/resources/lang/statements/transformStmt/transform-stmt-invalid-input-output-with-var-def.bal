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

function varDefInTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
        string prefix = "Ms.";
        e.address = p.city;
        e.name = getNameWithPrefix(prefix, p.firstName);
        prefix = "Mrs.";
        e.age = p.age;
    }
    return e.name, e.age, e.address;
}
