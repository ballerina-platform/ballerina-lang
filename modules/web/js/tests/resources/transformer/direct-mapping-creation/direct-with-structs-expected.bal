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

transformer <Person p, Employee e> {
    e.age = p.age;
    e.name = p.firstName;}