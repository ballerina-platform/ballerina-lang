struct Department {
    string dptName;
    Person[] employees;
}

struct Person {
    string name = "default first name";
    string lname;
    map adrs;
    int age = 999;
    Family family;
    Person parent;
}

struct Family {
    string spouse;
    int noOfChildren;
    string[] children;
}

function testGetNonInitAttribute() (string) {
	Person emp1;
    Person emp2;
    Person[] emps = [emp1, emp2];
    Department dpt = {dptName : "HR" , employees : emps};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitArrayAttribute() (string) {
    Department dpt = {dptName : "HR"};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitLastAttribute() (Person) {
    Department dpt;
    return dpt.employees[0];
}

function testSetFieldOfNonInitChildStruct() {
    Person person = {name : "Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct() {
    Department dpt;
    dpt.dptName = "HR";
}
