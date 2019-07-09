public function main (string... args) {
    string ex = f1("static data");
}

int i = 10;
function f1 (string inputData) returns (string) {
    string data = inputData + ":";
    if (i > 0) {
        i -= 1;
        data = data + f1(data);
    }
    secureFunction(data, data);
    return data;
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}
