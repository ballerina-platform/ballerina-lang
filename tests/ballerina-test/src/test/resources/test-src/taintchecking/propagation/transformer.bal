struct Employee {
    string name;
    int age;
    string address;
}

struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
    string street;
}

transformer <Person p, Employee e> {
    e.name = p.firstName + " " + p.lastName;
    e.age = p.age;
    e.address = p.street + "," + p.city.toUpperCase();
}

public function main (string[] args) {
    Person person = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    Employee employee = <Employee>person;
    secureFunction(employee, employee);
    secureFunction(employee.name, employee.name);
    secureFunction(employee.age, employee.age);
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
