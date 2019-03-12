import reftest/module2;

public type Person22 record {
    string name = "";
    int age = 0;
};

public type Employee22 record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

public Person22 person22 = {}; 

public Employee22 employee22 = {
    empNo: 100
};

(Employee22, Person22) pp = (employee22, person22);

function testContentInOtherPkg() {
    int val1 = module2:functionInOtherPkg();
    int val2 = module2:finalInOtherPkg;
    int val3 = module2:intInOtherPkg;
    float val4 = <float> module2:intInOtherPkg;
}

type SMS error <string, map<string>>;

function testBasicErrorVariableWithMapDetails() {
    SMS err1 = error("Error One", { message: "Msg One", detail: "Detail Msg" });
}