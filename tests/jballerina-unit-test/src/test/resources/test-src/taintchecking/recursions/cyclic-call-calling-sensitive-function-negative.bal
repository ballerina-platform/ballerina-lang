public function main (string... args) {
    string ex = f1(args[0]);
}

int i = 10;
function f1 (string inputData) returns (string) {
    string data = inputData + ":";
    if (i > 0) {
        i -= 1;
        data = data + f2(data);
    }
    secureFunction(data, data);
    return data;
}

function f2 (string inputData) returns string {
    i -= 1;
    string ex = f1(inputData);
    return ex;
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}
