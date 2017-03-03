package lang.expressions.mappers.default;
import lang.expressions.mappers.custom;

function testStructMapper() (string){
    custom:Employee emp;
    map address = {"country":"USA","state":"CA"};
    custom:Person per = {name:"Jack", adrs:address, age:25};
    emp = (custom:Employee)per;
    return emp.fname;
}

function testStructMapperLocal() (string){
    Employee emp;
    map address = {"country":"USA","state":"CA"};
    Person per = {name:"Jack", adrs:address, age:25, xxx:"WSO2"};
    emp = (Employee)per;
    return emp.fname;
}

struct Person {
    string name;
    map adrs;
    int age;
    string xxx;
}

struct Employee {
    string fname;
    map locations;
    int hoau;
    string eee;
}

typemapper jsonToXml(json j)(xml) {
  xml x = `<test>hello</test>`;
  return x;
}

typemapper personToEmployee(Person p)(Employee){
    Employee e = {};
    e.fname = "Jill";
    e.locations = p.adrs;
    e.hoau = p.age;
    e.eee = p.xxx;
    return e;
}
