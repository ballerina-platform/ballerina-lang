@Description {value:"Defining Employee struct."}
struct Employee {
    string name;
    int age;
    string address;
}

@Description {value:"Defining Person struct."}
struct Person {
    string first_name;
    string last_name;
    int age;
    string city;
}

@Description{value:"Defining a default transformer for converting from 'Person' type to 'Employee' type."}
transformer <Person p, Employee e> {
  e.name = p.first_name.toLowerCase();
  e.age = p.age;
  e.address = p.city.toLowerCase();
}

@Description{value:"Defining a named transformer for converting from 'Person' type to 'Employee' type."}
transformer <Person p, Employee e> Foo() {
  e.name = p.first_name.toLowerCase();
  e.age = p.age + 2;
  e.address = p.city.toUpperCase();
}

@Description{value:"Defining a named transformer which takes input parameters for converting from 'Person' type to 'Employee' type."}
transformer <Person p, Employee e> Bar(string country) {
  e.name = p.first_name.toLowerCase();
  e.age = p.age;
  e.address = p.city.toLowerCase() + ", " + country;
}

function main (string[] args) {
    //Initialize Person variable person.
    Person person = {first_name:"John", last_name:"Doe", age:30, city:"London"};

    //Using default transformer to convert from type Person to Employee is similar to the conversion syntax.
    Employee employee = <Employee> person;
    println(employee);

    //Named transformer can be explicitly provided inside the conversion syntax, to convert person to employee.
    employee = <Employee, Foo()> person;
    println(employee);

    //Using the named transformer to convert person to empoyee, by passing parameters.
    employee = <Employee, Bar("UK")> person;
    println(employee);
}