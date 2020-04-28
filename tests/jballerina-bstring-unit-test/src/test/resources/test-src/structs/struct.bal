type DepartmentSt record {
    string dptName = "";
    PersonSt[] employees = [];
};

type PersonSt record {
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
    FamilySt family = {};
    PersonSt? parent = ();
};

type FamilySt record {
    string spouse = "";
    int noOfChildren = 0;
    string[] children = [];
};

function testCreateStruct () returns [string, map<any>, int] {
    map<any> address = {"country":"USA", "state":"CA"};
    PersonSt emp = {name:"Jack", adrs:address, age:25};
    return [emp.name, emp.adrs, emp.age];
}

function testStructOfStruct () returns (string) {

    map<any> address = {"country":"USA", "state":"CA"};
    PersonSt emp1 = {name:"Jack", adrs:address, age:25};
    PersonSt emp2 = {};
    PersonSt[] emps = [emp1, emp2];
    DepartmentSt dpt = {employees:emps};

    string country = <string> dpt.employees[0].adrs["country"];
    return country;
}

function testReturnStructAttributes () returns (string) {
    map<any> address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    FamilySt fmly = {children:chldrn};
    PersonSt emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    PersonSt emp2 = {};
    PersonSt[] employees = [emp1, emp2];
    DepartmentSt dpt = {employees:employees};

    dpt.employees[0].family.children[0] = "emily";

    return dpt.employees[0].family.children[0];
}

function testExpressionAsIndex () returns (string) {
    FamilySt family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex () returns (string) {
    DepartmentSt dpt = {};
    FamilySt fmly = {};
    fmly.children = [];
    PersonSt emp2 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    PersonSt emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1.adrs["street"] = "20";
    emp1.age = 0;

    dpt.employees = [emp1, emp2];
    dpt.employees[0].family.children[0] = "emily";
    dpt.employees[0].family.noOfChildren = 1;

    return dpt.employees[0].family.children[dpt.employees[0].family.noOfChildren - 1];
}

function testDefaultVal () returns [string, string, int] {
    PersonSt p = {};
    return [p.name, p.lname, p.age];
}

function testNestedFieldDefaultVal () returns [string, string, int] {
    DepartmentSt dpt = {};
    dpt.employees = [];
    dpt.employees[0] = {lname:"Smith"};
    return [dpt.employees[0].name, dpt.employees[0].lname, dpt.employees[0].age];
}

function testNestedStructInit () returns (PersonSt) {
    PersonSt p1 = {name:"aaa", age:25, parent:{name:"bbb", age:50}};
    return p1;
}

type NegativeValTest record {
    int negativeInt = -9;
    int negativeSpaceInt = -8;
    float negativeFloat = -88.234;
    float negativeSpaceFloat = -24.99;
};

function getStructNegativeValues () returns [int, int, float, float] {
    NegativeValTest tmp = {};
    return [tmp.negativeInt, tmp.negativeSpaceInt, tmp.negativeFloat, tmp.negativeSpaceFloat];
}

function getStruct () returns (PersonSt) {
    PersonSt p1 = {name:"aaa", age:25, parent:{name:"bbb", lname:"ccc", age:50}};
    return p1;
}

function testGetNonInitAttribute () returns (string) {
    PersonSt emp1 = {};
    PersonSt emp2 = {};
    PersonSt[] emps = [emp1, emp2];
    DepartmentSt dpt = {dptName:"HR", employees:emps};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitArrayAttribute () returns (string) {
    DepartmentSt dpt = {dptName:"HR"};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitLastAttribute () returns (PersonSt) {
    DepartmentSt dpt = {};
    return dpt.employees[0];
}

function testSetFieldOfNonInitChildStruct () {
    PersonSt person = {name:"Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct () {
    DepartmentSt dpt = {};
    dpt.dptName = "HR";
}
