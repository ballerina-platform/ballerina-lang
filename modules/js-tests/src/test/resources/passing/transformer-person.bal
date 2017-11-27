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

function test() {
    Person p = {firstName:"Jane",lastName:"Doe",age:25,city:"Paris"};
    Employee e = <Employee>p;
    e = <Employee, Foo()>p;
    e = <Employee, Bar(99)> p;
    json<Person> jp;
    jp, _ = <json<Person>> p; //not using transformer
    json<Employee> ej;
    ej, _= <json<Employee>> jp; // using transformer
}

transformer  <Person p  , Employee e>
{

    e.address = p.city;
    e.name = p.firstName;
    e.age = p.age;}

transformer <Person p, Employee e> Foo(){
    e.address = p.city;
    e.name = p.firstName;
    e.age = p.age;}

transformer <Person p, Employee e> Bar(int age) {
    e.address = p.city;
    e.name = p.firstName;
    e.age = age;
}

transformer <json<Person> p, json<Employee> e>
{
    e.address = p.city;
    e.name = p.firstName;
    e.age = p.age;
}
