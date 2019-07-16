import reftest/module2;

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