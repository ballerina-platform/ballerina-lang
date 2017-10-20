struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    boolean retired;
    string address;
}

function operatorsInTransform() (string, int, boolean){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    string prefix = "Ms.";
    transform {
        e.name = prefix + p.firstName;
        p.firstName = "Ann";
        e.retired = (p.age > 60) ? true: false;
        p.age = 45;
    }
    return e.name, e.age, e.retired;
}
