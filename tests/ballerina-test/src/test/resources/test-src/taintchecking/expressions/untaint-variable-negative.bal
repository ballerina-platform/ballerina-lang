function main (string... args) {
    string data = args[0];
    string untaintedData = untaint data;
    secureFunction(untaintedData);
    secureFunction(data);
}

function secureFunction (@sensitive string secureIn) {

}
