public function main (string... args) {
    string ex = f1("static data");
}

int i = 10;
function f2 (string inputData) returns @tainted string {
    i -= 1;
    string ex = taintedReturn() + f1(inputData);
    return ex;
}

function f1 (string inputData) returns (string) {
    string data = inputData + ":";
    if (i > 0) {
        i -= 1;
        data = data + f2(data);
    }
    secureFunction(data, data);
    return data;
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}

function taintedReturn () returns @tainted string {
    return "tainted-data";
}
