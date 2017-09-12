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

function castAndConversionInTransform() (string, int, string, any){
    Person p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee e = {};
    any ageAny = "";
    any defaultAddress = "New York";
    transform {
        string age = "20";
        e.address, _ = (string) defaultAddress; //unsafe explicit cast
        defaultAddress = p.city;
        e.name = getPrefixedName(p.firstName);
        e.age, _ = <int> age; //unsafe conversion
        age = p.age;
        ageAny = p.age; // implicit cast
    }
    return e.name, e.age, e.address, ageAny;
}
