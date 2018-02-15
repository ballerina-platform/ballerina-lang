struct Employee {  
    string name;
    int age;
    string address; 
}

struct Person {
    string firstName;
    string lastName;
    int age;
    string address;
}

transformer <Person p, Employee e> TransformPersonToEmployee (string address) {
    e.address = address;}