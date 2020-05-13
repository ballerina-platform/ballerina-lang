
function testAnonStructAsFuncParam() returns (int) {
    return testAnonStructFunc(10, {k:14, s:"sameera"});
}

function testAnonStructFunc(int i, record {int k = 10; string s = "";} anonSt) returns (int) {
    return anonSt.k + i;
}


function testAnonStructAsLocalVar() returns (int) {
    record {int k = 11; string s = "";} anonSt = {};

    return anonSt.k;
}


record {
    string fname = "";
    string lname = "";
    int age = 0;
} personRec = {};

function testAnonStructAsPkgVar() returns (string) {

    personRec = {fname:"sameera", lname:"jaya"};
    personRec.lname = personRec.lname + "soma";
    personRec.age = 100;
    return personRec.fname + ":" + personRec.lname + ":" + personRec.age.toString();
}

type employee record {
    string fname = "";
    string lname = "";
    int age = 0;
    record {
        string line01 = "";
        string line02 = "";
        string city = "";
        string state = "";
        string zipcode = "";
    } address;

    record {
        string month = "JAN";
        string day = "01";
        string year = "1970";
    } dateOfBirth;
};

function testAnonStructAsStructField() returns (string) {

    employee e = {fname:"sam", lname:"json", age:100,
                     address:{line01:"12 Gemba St APT 134", city:"Los Altos", state:"CA", zipcode:"95123"},
                    dateOfBirth:{}};
    return e.dateOfBirth.month + ":" + e.address.line01 + ":" + e.address["state"] + ":" + e.fname;
}
