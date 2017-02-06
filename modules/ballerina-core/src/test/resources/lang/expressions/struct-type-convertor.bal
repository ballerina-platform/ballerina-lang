function testStructConvertor() (Employee){
    Person per;
    Employee emp;
    per = new Person;
    per.name = "Jack";
    per.adrs = {"country":"USA","state":"CA"};
    per.age = 25;
    emp = (Employee)per;
    return emp;
}

type Person {
    string name;
    map adrs;
    int age;
}

type Employee {
    string fname;
    map locations;
    int hoau;
}

typeconvertor personToEmployee(Person p)(Employee){
    Employee e;
    e = new Employee;
    e.fname = p.name;
    e.locations = p.adrs;
    e.hoau = p.age;
    return e;
}