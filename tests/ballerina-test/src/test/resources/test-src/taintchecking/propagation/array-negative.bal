function main (string... args) {
    // tainted array - untainted field.
    string[] data = ["Ballerina", args[0]];
    secureFunction(data[0], data[0]);

    // tainted array - tainted field.
    string[] data1 = ["Ballerina", args[0]];
    secureFunction(data1[1], data1[1]);

    // untainted array - tainted assignment.
    string[] data2 = ["Ballerina", "Colombo"];
    data2[0] = args[0];
    secureFunction(data2[0], data2[0]);

    // untainted array - tainted assignment - replaced with untainted assignment.
    string[] data3 = ["Ballerina", "Colombo"];
    data3[0] = args[0];
    data3[0] = "BallerinaNew";
    secureFunction(data3[0], data3[0]);

    // tainted array of arrays
    string[][] data4 = [["Ballerina", "Colombo"], ["BallerinaNew", args[0]]];
    secureFunction(data4[0][0], data4[0][0]);

    // tainted array of arrays
    string[][] data5 = [["Ballerina", "Colombo"], ["BallerinaNew", "Kandy"]];
    data5[1][1] = args[0];
    secureFunction(data5[0][0], data5[0][0]);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
