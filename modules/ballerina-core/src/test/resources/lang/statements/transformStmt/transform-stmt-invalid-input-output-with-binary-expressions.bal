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

function binaryExpInTransform() (string, int, string){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    transform {
        string prefix = "Ms.";
        e.name = prefix + p.firstName;
        prefix = "Mr.";
    }
    return e.name;
}
