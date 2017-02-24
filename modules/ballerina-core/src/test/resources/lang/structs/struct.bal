struct Department {
    string dptName;
    Person[] employees;
}

struct Person {
    string name;
    map adrs;
    int age;
    Family family;
}

@doc:Description("Family representation")
@doc:Field("spouse: a spouse")
@doc:Field("noOfChildren: number of children")
@doc:Field("children: list of children")
struct Family {
    string spouse;
    int noOfChildren;
    string[] children;
}

function testCreateStruct() (string, map, int){
    map address = {"country":"USA","state":"CA"};
    Person emp = {name:"Jack", adrs:address, age:25};
    return emp.name, emp.adrs, emp.age;
}

function testStructOfStruct() (string) {

	map address = {"country":"USA","state":"CA"};
	Person emp1 = {name:"Jack", adrs:address, age:25};
    Person emp2;
    Person[] emps = [emp1, emp2];
    Department dpt = {employees:emps};
    
    string country = dpt.employees[0].adrs["country"];
    return country;
}

function testReturnStructAttributes () (string) {
	map address = {"country":"USA","state":"CA"};
	Family fmly= {};
	Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    Person emp2;
    Person[] employees = [emp1, emp2];
    Department dpt = {employees:employees};
    
    dpt.employees[0].family.children[0] = "emily";

    return dpt.employees[0].family.children[0];
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

function testExpressionAsIndex() (string) {
    Family family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex() (string) {
    string country;
    Department dpt= {};
    Family fmly = {};
    Person emp2;
    map address = {"country":"USA","state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1.adrs["street"] = "20";
    emp1.age = 0;
   
    dpt.employees = [emp1, emp2];
    dpt.employees[0].family.children[0] = "emily";
    dpt.employees[0].family.noOfChildren = 1;

    return dpt.employees[0].family.children[dpt.employees[0].family.noOfChildren - 1];
}
