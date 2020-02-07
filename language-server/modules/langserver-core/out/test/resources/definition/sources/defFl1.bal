public type Person22 record {
    string name = "";
    int age = 0;
};

public type Employee22 record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

Person22 person22 = {}; 

Employee22 employee22 = {
    empNo: 100
};

[Employee22, Person22] pp = [employee22, person22];

type SMS error <string, record {| string message?; error cause?; string...; |}>;

function testBasicErrorVariableWithMapDetails() {
    SMS err1 = error("Error One", message = "Msg One");
}