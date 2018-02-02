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

function varDefInTransform () (string, int, string) {
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

function varDefInTransformWithInput () (string, int, string) {
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
        string name = getPrefixedName(p.firstName);
        e.address = p.city;
        e.name = name;
        name = "Mrs. Peter";
        e.age = p.age;
    }
    return e.name, e.age, e.address;
}

function getNameWithPrefix (string prefix, string name) (string) {
    return prefix + name;
}

function getPrefixedName (string name) (string) {
    return "Ms." + name;
}
