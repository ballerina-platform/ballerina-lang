package lang.expressions.mappers.custom;

struct Person {
    string name;
    map adrs;
    int age;
}

struct Employee {
    string fname;
    map locations;
    int hoau;
}

typemapper personToEmployee(Person p)(Employee){
    Employee e = {};
    e.fname = p.name;
    e.locations = p.adrs;
    e.hoau = p.age;
    return e;
}
