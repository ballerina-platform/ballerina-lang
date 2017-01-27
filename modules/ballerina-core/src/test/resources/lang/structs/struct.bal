package samples.struct.test;

public type Department {
    string dptName;
    Person[] employees;
}

public type Person {
    string name;
    map adrs;
    int age;
    Family family;
}

public type Family {
    string spouse;
    int noOfChildren;
    string[] children;
}

public function testCreateStruct() (string, map, int){
    Person emp;
    emp = new Person;
    emp.name = "Jack";
    emp.adrs = {"country":"USA","state":"CA"};
    emp.age = 25;
    return emp.name, emp.adrs, emp.age;
}

public function testStructOfStruct() (string) {

    string country;
    Department dpt;
    Person emp1;
    Person emp2;
    
    dpt = new Department;
    emp1 = new Person;
    
    emp1.name = "Jack";
    emp1.adrs = {"country":"USA","state":"CA"};
    emp1.age = 25;
    dpt.employees = [emp1, emp2];
    
    country = dpt.employees[0].adrs["country"];
    return country;
}

public function testReturnStructAttributes () (string) {
    string country;
    Department dpt;
    Person emp1;
    Person emp2;
    
    dpt = new Department;
    emp1 = new Person;
    
    emp1.name = "Jack";
    emp1.adrs = {"country":"USA","state":"CA"};
    emp1.adrs["street"] = "20";
    emp1.age = 5;
    dpt.employees = [emp1, emp2];
    
    dpt.employees[0].family = new Family;
    dpt.employees[0].family.children[0] = "emily";

    return dpt.employees[0].family.children[0];
}


public function testGetNonInitAttribute() (string) {
    Department dpt;
    return dpt.employees[0].family.children[0];
}

public function testGetNonInitLastAttribute() (Person) {
    Department dpt;
    return dpt.employees[0];
}

public function testSetNonInitAttribute() {
    Person person;
    person.family.spouse = "Jane";
}

public function testSetNonInitLastAttribute() {
    Department dpt;
    dpt.dptName = "HR";
}

public function testExpressionAsIndex() (string) {
    Family family;
    int a;
    int b;
    a = 2;
    b = 5;
    family = new Family;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}


connector TestConnector(string name, map address, int age) {

    Person person;

    action action1(TestConnector testConnector) (Person){
        person = new Person;
        person.name = name;
        person.adrs = address;
        person.age = age;
        return person;
    }
}

function testAction1() (string) {
    test:TestConnector testConnector = new test:TestConnector("Jack", {"country":"USA","state":"CA"}, 30);
    Person person;

    person = test:TestConnector.action1(testConnector);
    return person.name;
}

public function testStructExpressionAsIndex() (string) {
    string country;
    Department dpt;
    Person emp1;
    Person emp2;
    
    dpt = new Department;
    emp1 = new Person;
    
    emp1.name = "Jack";
    emp1.adrs = {"country":"USA","state":"CA"};
    emp1.adrs["street"] = "20";
    emp1.age = 0;
    dpt.employees = [emp1, emp2];
    
    dpt.employees[0].family = new Family;
    dpt.employees[0].family.children[0] = "emily";
    dpt.employees[0].family.noOfChildren = 1;

    return dpt.employees[0].family.children[dpt.employees[0].family.noOfChildren - 1];
}
