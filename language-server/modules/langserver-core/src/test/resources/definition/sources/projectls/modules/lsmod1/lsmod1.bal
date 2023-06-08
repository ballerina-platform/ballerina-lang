import projectls.lsmod2;
import ballerina/module1;

public function hello() {
    mod1Src1Function1();
    error? result = ();
    int resultInt = addNumbers(1, 2);
    lsmod2:mod2Function1();
}

function addNumbers(int a, int b) {
    return a + b;
}

public type MyType anydata;
