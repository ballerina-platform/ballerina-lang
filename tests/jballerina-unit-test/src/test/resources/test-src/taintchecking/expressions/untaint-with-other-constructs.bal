string globalvariable = "input4";

function untaintWithAddOperatorInReturn () returns (string) {
    string data1 = "input1";
    string data2 = "input2";
    return <@untainted string> (data1 + data2 + "input3" + globalvariable);
}

function untaintWithAddOperatorWithVariable () returns (string) {
    string data1 = "input1";
    string data2 = "input2";
    string data3 = data1 + data2 + "input3" + globalvariable;
    return <@untainted string> data3;
}

function untaintWithFunctionParam () returns (string) {
    string data1 = "input1";
    string data2 = "input2";
    return returnInput(<@untainted string> (data1 + data2 + "input3" + globalvariable));
}

function untaintWithFunctionReturn () returns (string) {
    string data1 = "input1";
    string data2 = "input2";
    return <@untainted string> returnInput(data1 + data2 + "input3" + globalvariable);
}

function untaintWithReceiver () returns (string) {
    string data1 = "input1";
    string data2 = "input2";
    return <@untainted string> returnInput(data1 + data2 + "input3" + globalvariable).trim();
}

function untaintWithLengthOf () returns (int) {
    string data1 = "input1";
    string data2 = "input2";
    return <@untainted int> returnInput(data1 + data2 + "input3" + globalvariable).length();
}

function returnInput (string inputData) returns (string) {
    return inputData;
}

function secureFunction (@untainted string secureIn) {

}
