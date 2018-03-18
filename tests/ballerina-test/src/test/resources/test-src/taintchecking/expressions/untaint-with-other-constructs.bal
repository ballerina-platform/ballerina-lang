public string globalvariable = "input4";

function untaintWithAddOperatorInReturn () (string) {
    string data1 = "input1";
    string data2 = "input2";
    return untaint (data1 + data2 + "input3" + globalvariable);
}

function untaintWithAddOperatorWithVariable () (string) {
    string data1 = "input1";
    string data2 = "input2";
    string data3 = data1 + data2 + "input3" + globalvariable;
    return untaint data3;
}

function untaintWithFunctionParam () (string) {
    string data1 = "input1";
    string data2 = "input2";
    return returnInput(untaint (data1 + data2 + "input3" + globalvariable));
}

function untaintWithFunctionReturn () (string) {
    string data1 = "input1";
    string data2 = "input2";
    return untaint returnInput(data1 + data2 + "input3" + globalvariable);
}

function untaintWithReceiver () (string) {
    string data1 = "input1";
    string data2 = "input2";
    return untaint returnInput(data1 + data2 + "input3" + globalvariable).trim();
}

function untaintWithLengthOf () (int) {
    string data1 = "input1";
    string data2 = "input2";
    return untaint lengthof returnInput(data1 + data2 + "input3" + globalvariable);
}

function returnInput (string inputData) (string) {
    return inputData;
}

function secureFunction (@sensitive string secureIn) {

}
